package ru.tcloud.vmaker.core.command.impl

import org.springframework.stereotype.Service
import ru.tcloud.vmaker.core.MediaType
import ru.tcloud.vmaker.core.command.Command
import ru.tcloud.vmaker.core.command.CommandType
import ru.tcloud.vmaker.core.command.impl.CreateFromDirCommand.Arguments.*
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.worker.AudioWorker
import ru.tcloud.vmaker.core.worker.FileDescription
import ru.tcloud.vmaker.core.worker.ImageWorker
import ru.tcloud.vmaker.core.worker.VideoWorker
import ru.tcloud.vmaker.core.worker.VideoWorker.VideoExtension.MP4
import java.io.File
import java.util.*

//cfd -w /home/evgeny/Documents/Chillax/video1 -d 30

@Service
class CreateFromDirCommand(
    private val videoWorker: VideoWorker,
    private val audioWorker: AudioWorker,
    private val imageWorker: ImageWorker
) : Command {

    private enum class Arguments(val arg: String, val desc: String) {
        W(
            "w", """work dir, have to follow next rule workDir
            |                                                   - ${MediaType.VIDEO.name.lowercase(Locale.getDefault())}
            |                                                   - ${MediaType.AUDIO.name.lowercase(Locale.getDefault())}
            |                                                   - ${MediaType.IMAGE.name.lowercase(Locale.getDefault())}
        """.trimMargin()
        ),
        D("d", "video have to be duration in min"),
        C("c", "clean temp dir after work (true/false), default $cleanAfterDefaultValue"),
        P("t", "picture to video, use -p 10 where 1- is duration in sec, default ${pictureToVideoDefaultDuration}s"),
        HELP(
            "help", """"${W.arg} - ${W.desc}
            |                ${C.arg} - ${C.desc}
            |                ${D.arg} - ${D.desc}
            |                ${P.arg} - ${P.desc}
            |                """
                .trimMargin()
        )
    }

    private val videoDirName = MediaType.VIDEO.name.lowercase(Locale.getDefault())
    private val musicDirName = MediaType.AUDIO.name.lowercase(Locale.getDefault())
    private val pictureDirName = MediaType.IMAGE.name.lowercase(Locale.getDefault())
    private val dirStructure = setOf(videoDirName, musicDirName)

    override fun getType() = CommandType.CREATE_FROM_DIR

    override fun run(args: Map<String, String>) {
        if (args.size == 1 && args.containsKey(HELP.arg)) {
            println(HELP.desc)
            return
        }
        val workDirPath =
            args[W.arg] ?: throw VMakerException("You need set -${W.arg} flag, use -${HELP.arg} for more info")
        val duration = (args[D.arg]
            ?: throw VMakerException("You need set -${D.arg} flag, use -${HELP.arg} for more info")).toInt() * 60
        val cleanTemp = if (args.containsKey(C.arg)) args[C.arg].toBoolean() else cleanAfterDefaultValue
        val pictureToVideoDuration =
            if (args.containsKey(P.arg)) args[P.arg]?.toInt() else pictureToVideoDefaultDuration

        checkWorkDir(workDirPath)
        val workDir = File(workDirPath.trim())
        val videoDir = File("${workDirPath.trim()}/$videoDirName")
        val musicDir = File("${workDirPath.trim()}/$musicDirName")
        val pictureDir = File("${workDirPath.trim()}/$pictureDirName")
        val tmpFiles = mutableSetOf<File>()
        try {
            val concatenationMp3 = audioWorker
                .fillAudioToFinallyResult(duration, musicDir)
                .let {
                    audioWorker.concatenationMp3(musicDir, it, tmpFiles)
                }
            val fadeVideos = prepareVideoToConcat(
                videoDir,
                pictureDir,
                tmpFiles,
                pictureToVideoDuration ?: pictureToVideoDefaultDuration
            )
            val concatVideo = videoWorker.fillVideoToFinallyResult(concatenationMp3.duration, fadeVideos)
            val concatenation =
                videoWorker.concatenationMp4(videoDir, concatVideo, tmpFiles).apply { tmpFiles.add(this) }
            val silent = videoWorker.doSilent(videoDir, concatenation, tmpFiles).apply { tmpFiles.add(this) }
            videoWorker.addAudioOnVideo(concatenationMp3.file, silent, workDir, tmpFiles)
        } finally {
            if (cleanTemp) {
                tmpFiles.forEach { it.delete() }
            }
        }
    }

    private fun prepareVideoToConcat(
        videoDir: File,
        imageDir: File,
        tmpFiles: MutableSet<File>,
        duration: Int
    ): List<FileDescription> {
        return fromVideo(videoDir, tmpFiles)
            .plus(fromImages(imageDir, tmpFiles, duration))
    }

    private fun fromVideo(videoDir: File, tmpFiles: MutableSet<File>): List<FileDescription> {
        val videos = videoDir.listFiles()?.filter { it.extension == MP4.ex }.orEmpty()
        var counter = 1
        return videos.map {
            videoWorker.makeFadeVideo("$counter", it, tmpFiles)
                .apply { counter++ }
        }
    }

    private fun fromImages(imageDir: File, tmpFiles: MutableSet<File>, duration: Int): List<FileDescription> {
        val img = imageDir.listFiles()
            ?.filter {
                ImageWorker.ImageExtension.values().map { v -> v.name.lowercase(Locale.getDefault()) }
                    .contains(it.extension)
            }.orEmpty()
        var counter = 1
        return img.map {
            FileDescription(duration, imageWorker.imageToFadeVideo(it, "$counter", duration, tmpFiles))
                .apply { counter++ }
        }
    }

    private fun checkWorkDir(workDirPath: String) {
        val dir = File(workDirPath.trim())
        if (!dir.exists()) {
            throw VMakerException("Can not find $workDirPath")
        }
        if (!dir.listFiles()?.mapNotNull { it.name }!!.toSet().containsAll(dirStructure)) {
            throw VMakerException(D.desc)
        }
    }

    companion object {
        internal const val pictureToVideoDefaultDuration = 30
        internal const val cleanAfterDefaultValue = false
    }

}