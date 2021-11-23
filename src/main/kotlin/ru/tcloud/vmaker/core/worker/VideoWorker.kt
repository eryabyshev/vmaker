package ru.tcloud.vmaker.core.worker

import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.editor.Editor
import ru.tcloud.vmaker.core.support.CircleCollection
import java.io.File

@Component
class VideoWorker(private val editor: Editor) {

    enum class Codec(val codec: String) {
        MJPEG("mjpeg"), LIBX264("libx264")
    }

    enum class VideoExtension(val ex: String) {
        MP4("mp4")
    }


    fun makeFadeVideo(resultName: String, file: File, tempFiles: MutableSet<File>): FileDescription {
        println("Start fade video for ${file.name}")
        val duration = editor.getDuration(file)
        val fadeVideo = editor.makeFadeVideo(resultName, file, duration)
        tempFiles.add(fadeVideo)
        println("Start encode video ${file.name}")
        val encodeVideo = editor.encodeVideo(resultName, fadeVideo)
        tempFiles.add(encodeVideo)
        return FileDescription(duration, encodeVideo)
    }

    fun fillVideoToFinallyResult(totalDuration: Int, src: List<FileDescription>): List<File> {
        println("Start fill video to $totalDuration min")
        if(src.isEmpty()) {
            return emptyList()
        }
        var durationNow = totalDuration
        val circleCollection = CircleCollection(src.map { it  })
        val result = mutableListOf<File>()
        while (durationNow >= 0) {
            val next = circleCollection.next()
            durationNow -= next.duration
            result.add(next.file)
        }
        return (if(result.isEmpty()) listOf(src.first().file) else result)
            .apply { println("Finish fill result with result: ${this.size} files in one video") }
    }

    fun concatenationMp4(workDir: File, list: List<File>, tmpFiles: MutableSet<File>): File {
        println("Start video concatenation")
        val concatenationMp4 = editor.concatenationMp4(workDir, list)
        return concatenationMp4
            .apply { println("Concatenation finish") }

    }

    fun doSilent(videoDir: File, concatenation: File, tmpFiles: MutableSet<File>): File {
        println("Remove audio from ${concatenation.name}")
        val doSilent = editor.doSilent(videoDir, concatenation)
        tmpFiles.add(doSilent)
        return doSilent
    }

    fun addAudioOnVideo(file: File, silent: File, workDir: File, tmpFiles: MutableSet<File>): File {
        println("Add audio ${file.name} to ${silent.name}")
        val addAudioOnVideo = editor.addAudioOnVideo(file, silent, workDir)
        tmpFiles.add(addAudioOnVideo)
        return addAudioOnVideo
    }
}