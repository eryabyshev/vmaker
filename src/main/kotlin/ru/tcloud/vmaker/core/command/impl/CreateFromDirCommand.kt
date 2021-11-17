package ru.tcloud.vmaker.core.command.impl

import org.springframework.stereotype.Service
import ru.tcloud.vmaker.core.command.Command
import ru.tcloud.vmaker.core.command.CommandType
import ru.tcloud.vmaker.core.editor.Editor
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.support.CircleCollection
import java.io.File

@Service
class CreateFromDirCommand(private val editor: Editor) : Command {

    private enum class Arguments(val arg: String, val desc: String) {
        W(
            "w", """work dir, have to follow next rule workDir
            |                                                   - video
            |                                                   - music
        """.trimMargin()
        ),
        D("d", "video have to be duration in min"),
        C("c", "clean temp dir after work (true/false), default false"),
        HELP("help", """"${W.arg} - ${W.desc}
            |                ${C.arg} - ${C.desc}
            |                ${D.arg} - ${D.desc}
            |                """
            .trimMargin())
    }
    private val videoDirName = "video"
    private val musicDirName = "music"
    private val dirStructure = setOf(videoDirName, musicDirName)
    private val mp4 = "mp4"
    private val mp3 = "mp3"

    private fun argToEnum(str: String): Map<String, Arguments> {
        return Arguments.values().associateBy { it.desc }
    }

    private data class TmpFilesHolder(val fadeFile: File, val codecFile: File)
    private data class FileDescription(var duration: Int, var tmpFiles: TmpFilesHolder? = null)

    override fun getType() = CommandType.CREATE_FROM_DIR

    override fun run(args: Map<String, String>) {
        if (args.size == 1 && args.containsKey(Arguments.HELP.arg)) {
            println(Arguments.HELP.desc)
            return
        }
        val workDirPath = args[Arguments.W.arg]?: throw VMakerException("You need set -w flag, use -help for more info")
        var duration = (args[Arguments.D.arg]?: throw VMakerException("You need set -d flag, use -help for more info")).toInt() * 60
        val cleanTemp = args[Arguments.C.arg]?: true
        checkWorkDir(workDirPath)
        var workDir = File("${workDirPath.trim()}")
        val videoDir = File("${workDirPath.trim()}/$videoDirName")
        val musicDir = File("${workDirPath.trim()}/$musicDirName")
        val tmpFiles = mutableSetOf<File>()
        try {
            val audiFiles = fillAudioToFinallyResult(duration, musicDir)
            val concatenationMp3 = if(audiFiles.size > 1) editor.concatenationMp3(musicDir, audiFiles, tmpFiles) else audiFiles[0]
            duration = editor.getDuration(concatenationMp3)
            val prepareVideoToConcat = prepareVideoToConcat(workDirPath)
                .onEach { (t, _) -> tmpFiles.add(t) }
            val concatVideo = if(prepareVideoToConcat.map { it.value.duration }.sum() >= duration.toLong()) {
                prepareVideoToConcat.mapNotNull { it.value.tmpFiles?.codecFile }
            } else {
                fillVideoToFinallyResult(duration, prepareVideoToConcat.entries.map { it.value })
            }
            val concatenation = editor.concatenationMp4(videoDir, concatVideo).apply { tmpFiles.add(this) }
            val silent = editor.doSilent(videoDir, concatenation).apply { tmpFiles.add(this) }
            editor.addAudioOnVideo(concatenationMp3, silent, workDir)
        } finally {
            tmpFiles.forEach { it.delete() }
        }
    }

    private fun prepareVideoToConcat(workDirPath: String): Map<File, FileDescription> {
        val videos = File(workDirPath.trim()).listFiles()?.find { it.name == videoDirName } ?: throw VMakerException(Arguments.W.desc)
        var counter = 1
        return videos.listFiles()
            .filter { it.extension == mp4 }.associateWith {
                val duration = editor.getDuration(it)
                val fadeVideo = editor.makeFadeVideo(counter, it, duration)
                val encodeVideo = editor.encodeVideo(counter, fadeVideo)
                counter++
                FileDescription(duration, TmpFilesHolder(fadeVideo, encodeVideo))
            }
    }

    private fun checkWorkDir(workDirPath: String) {
        val dir = File(workDirPath.trim())
        if(!dir.exists()) {
            throw VMakerException("Can not find $workDirPath")
        }
        if(!dir.listFiles()?.mapNotNull { it.name }!!.toSet().containsAll(dirStructure)) {
            throw VMakerException(Arguments.D.desc)
        }
    }

    private fun fillVideoToFinallyResult(totalDuration: Int, src: List<FileDescription>): List<File> {
        var durationNow = totalDuration
        val circleCollection = CircleCollection(src.map { it  })
        val result = mutableListOf<File>()
        while (durationNow >= 0) {
            val next = circleCollection.next()
            durationNow -= next.duration
            result.add(next.tmpFiles?.codecFile?:throw IllegalArgumentException("File can't be null"))
        }
        return result
    }

    private fun fillAudioToFinallyResult(totalDuration: Int, audioDir: File): List<File> {
        val mp3s = audioDir.listFiles()
            .apply {
                if(this == null || this.isEmpty()) {
                    throw VMakerException("Can't find in ${audioDir.absolutePath}")
                }
            }
            ?.filter { it.extension == mp3 }
            ?.associate { it to editor.getDuration(it) }?.entries
        if(mp3s == null || mp3s.isEmpty()) {
            throw VMakerException("$audioDir is doesn't content MP3")
        }
        var durationNow = totalDuration
        val circleCollection = CircleCollection(mp3s)
        val result = mutableListOf<File>()
        while (durationNow >= 0) {
            val next = circleCollection.next()
            durationNow -= next.value
            result.add(next.key)
        }
        return result
    }

    private fun minToSec(min: Int): Int = min * 60

}