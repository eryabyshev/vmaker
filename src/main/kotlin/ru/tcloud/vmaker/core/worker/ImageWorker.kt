package ru.tcloud.vmaker.core.worker

import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.editor.Editor
import ru.tcloud.vmaker.core.worker.ImageWorker.ImageExtension.Companion.jpegList
import ru.tcloud.vmaker.core.worker.VideoWorker.Codec.LIBX264
import ru.tcloud.vmaker.core.worker.VideoWorker.Codec.MJPEG
import java.io.File
import java.util.*

@Component
class ImageWorker(private val editor: Editor) {

    enum class ImageExtension {
        JPG, JPEG, PNG;
        companion object {
            val jpegList = listOf(JPG, JPEG).map { it.name.lowercase(Locale.getDefault()) }
        }
    }

    fun imageToFadeVideo(file: File, name: String, duration: Int, tmpFiles: MutableSet<File>): File {
        println("Start image to fade video")
        println("Phase jpeg -> png")
        val jpegToPng = editor.jpegToPng(file)
        tmpFiles.add(jpegToPng)
        println("Image ${file.name} to video with duration $duration")
        val imageToVideo = editor.imageToVideo(jpegToPng, name, duration)
        tmpFiles.add(imageToVideo)
        println("Start fade video process for ${file.name}")
        val fadeVideo = editor.makeFadeVideo(name, imageToVideo, duration)
        tmpFiles.add(fadeVideo)
        println("Start encode video")
        val encodeVideo = editor.encodeVideoFromImage(name, fadeVideo)
        tmpFiles.add(encodeVideo)
        return encodeVideo
    }

}