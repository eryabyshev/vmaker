package ru.tcloud.vmaker.core.editor

import com.lordcodes.turtle.shellRun
import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.exception.VMakerException
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

    override fun makeFadeVideo(counter: Int, file: File, duration: Int): File {
        println("Start fade video making for $counter video")
        val fadeFile = File("${file.parentFile.absolutePath}/fade$counter.$mp4")
        fadeFile.createNewFile()
        if(!fadeFile.exists()) {
            throw VMakerException("Can't create fade file")
        }
        listOf(
            "-i", file.absolutePath, "-vf",
            "fade=t=in:st=0:d=$fadeDuration,fade=t=out:st=$duration:d=$fadeDuration",
            "-c:a", "copy", fadeFile.absolutePath, "-y"
        ).apply { run("ffmpeg", this) }
        println("Finish fade video making for $counter video")
        return fadeFile
    }

    override fun encodeVideo(counter: Int, file: File): File {
        println("Start codec video making for $counter video")
        val tsVideo = File("${file.parentFile.absolutePath}/tmp$counter.$ts")
        tsVideo.createNewFile()
        if(!tsVideo.exists()) {
            throw VMakerException("Can't create codec file")
        }
        listOf(
            "-i", file.absolutePath, "-c", "copy", "-bsf:v", "h264_mp4toannexb",
            "-f", "mpegts", tsVideo.absolutePath, "-y"
        ).apply { run("ffmpeg", this) }
        println("Finish codec video making for $counter video")
        return tsVideo
    }

    override fun concatenationMp4(workDir: File, list: List<File>): File {
        val result = File("${workDir.absolutePath}/concat.$mp4")
        result.createNewFile()
        if(!result.exists()) {
            throw VMakerException("Can't create ${result.absolutePath}")
        }
        run("ffmpeg", listOf(
            "-i", "concat:${list.joinToString("|")}", "-c", "copy",
            "-bsf:a", "aac_adtstoasc", result.absolutePath, "-y"
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

    override fun concatenationMp3(workDir: File, list: List<File>, tmpFiles: MutableSet<File>): File {
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
        tmpFiles.add(result)
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


    private fun run(cmd: String, args: List<String>): String {
        println("$cmd ${args.joinToString(" ")}")
        return shellRun(cmd, args)
    }


}