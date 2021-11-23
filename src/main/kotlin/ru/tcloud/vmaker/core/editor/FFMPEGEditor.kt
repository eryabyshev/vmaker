package ru.tcloud.vmaker.core.editor

import com.lordcodes.turtle.shellRun
import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.worker.VideoWorker
import ru.tcloud.vmaker.core.worker.VideoWorker.VideoExtension.MP4
import java.io.File
import java.util.*

@Component
class FFMPEGEditor: Editor {

    private val fadeDuration = 2
    private val mp4 = "mp4"
    private val ts = "ts"
    private val mp3 = "mp3"

    override fun getDuration(file: File): Int {
        return "-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 ${file.absolutePath}"
            .split(" ")
            .let {
                run("ffprobe", it).toDouble().toInt()
            }
    }

    override fun makeFadeVideo(name: String, file: File, duration: Int): File {
        val fadeFile = File("${file.parentFile.absolutePath}/fade_$name.$mp4")
        fadeFile.createNewFile()
        if(!fadeFile.exists()) {
            throw VMakerException("Can't create fade file")
        }
        listOf(
            "-i", file.absolutePath, "-vf",
            "fade=t=in:st=0:d=$fadeDuration,fade=t=out:st=$duration:d=$fadeDuration",
            "-c:a", "copy", fadeFile.absolutePath, "-y"
        ).apply { run("ffmpeg", this) }
        return fadeFile
    }

    override fun encodeVideo(name: String, file: File): File {
        val tsVideo = File("${file.parentFile.absolutePath}/encode_$name.$ts")
        tsVideo.createNewFile()
        if(!tsVideo.exists()) {
            throw VMakerException("Can't create codec file")
        }
        listOf(
            "-i", file.absolutePath, "-c", "copy", "-bsf:v", "h264_mp4toannexb",
            "-f", "mpegts", tsVideo.absolutePath, "-y"
        ).apply { run("ffmpeg", this) }
        return tsVideo
    }

    override fun encodeVideoFromImage(name: String, file:File): File {
        val tsVideo = File("${file.parentFile.absolutePath}/encode_$name.$ts")
        tsVideo.createNewFile()
        if(!tsVideo.exists()) {
            throw VMakerException("Can't create codec file")
        }
        listOf("-i", file.absolutePath, "-f", "mpegts", tsVideo.absolutePath, "-y")
            .apply { run("ffmpeg", this) }
        return tsVideo
    }

    override fun concatenationMp4(workDir: File, list: List<File>): File {
        val result = File("${workDir.absolutePath}/concat.$mp4")
        result.createNewFile()
        if(!result.exists()) {
            throw VMakerException("Can't create ${result.absolutePath}")
        }
        val fileList = File("${workDir.absolutePath}/video_list.txt")
        list.mapNotNull { "${it.absolutePath}" }
            .forEach {
                fileList.appendText("file '$it'")
                fileList.appendText("\n")
            }
        run("ffmpeg", listOf(
            "-f", "concat", "-safe", "0", "-i", fileList.absolutePath, "-c", "copy", "-bsf:a", "aac_adtstoasc", result.absolutePath, "-y"
        ))
        return result
    }

    override fun doSilent(workDir: File, file: File, posfix: String): File {
        val result = File("${workDir.absolutePath}/silent$posfix.$mp4")
        result.createNewFile()
        if(!result.exists()) {
            throw VMakerException("Can't create ${result.absolutePath}")
        }
        run("ffmpeg", listOf(
            "-i", file.absolutePath, "-vcodec", "copy", "-an", result.absolutePath, "-y"
        ))
        return result
    }

    override fun concatenationMp3(workDir: File, list: List<File>): File {
        val mp3s = list.filter { it.extension.lowercase(Locale.getDefault()) == mp3 }
        val musicList = File("${workDir.absolutePath}/music_list.txt")
        musicList.createNewFile()
        if(!musicList.exists()) {
            throw VMakerException("Can't create ${musicList.absolutePath}")
        }
        mp3s.mapNotNull { "${it.name}" }
            .forEach {
                musicList.appendText("file '$it'")
                musicList.appendText("\n")
            }

        val result = File("${workDir.absolutePath}/back.$mp3")
        result.createNewFile()
        if(!result.exists()) {
            throw VMakerException("Can't create ${result.absolutePath}")
        }

        run("ffmpeg", listOf(
            "-f", "concat", "-i", musicList.absolutePath, "-c", "copy", result.absolutePath, "-y"
        ))
        musicList.delete()
        return result
    }

    override fun addAudioOnVideo(audio: File, video: File, resultDir: File): File {
        val result = File("${resultDir.absolutePath}/out.$mp4")
        result.createNewFile()
        if(!result.exists()) {
            throw VMakerException("Can't create ${result.absolutePath}")
        }
        run("ffmpeg", listOf(
            "-i", video.absolutePath, "-i", audio.absolutePath, "-map", "0:v", "-map", "1:a", "-c:v", "copy", "-shortest",
            result.absolutePath, "-y"
        ))
        return result
    }

    override fun imageToVideo(file: File, name: String, duration: Int): File {
        val result = File("${file.parentFile.absolutePath}/$name.${MP4.ex}")
        result.createNewFile()
        if(!result.exists()) {
            throw VMakerException("Can't create ${result.absolutePath}")
        }
        run("ffmpeg", listOf(
            "-r", "1/$duration", "-i", file.absolutePath, "-c:v", "png", "-vf", "fps=25", "-pix_fmt", "yuv420p",
            result.absolutePath, "-y"
        ))
        return result
    }

    override fun jpegToPng(file: File): File {
        if(file.extension == "png") {
            return file
        }
        run("mogrify", listOf("-format", "png", file.absolutePath))
        return File("${file.parentFile}/${file.nameWithoutExtension}.png")
    }

    private fun run(cmd: String, args: List<String>): String {
        println("$cmd ${args.joinToString(" ")}")
        return shellRun(cmd, args)
    }


}