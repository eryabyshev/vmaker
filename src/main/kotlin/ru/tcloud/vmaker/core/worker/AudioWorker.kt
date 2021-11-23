package ru.tcloud.vmaker.core.worker

import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.editor.Editor
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.support.CircleCollection
import ru.tcloud.vmaker.core.worker.AudioWorker.AudioExtension.*
import java.io.File

@Component
class AudioWorker(private val editor: Editor) {

    enum class AudioExtension {
        MP3
    }

    fun fillAudioToFinallyResult(totalDuration: Int, audioDir: File): List<File> {
        println("Start fill audio to $totalDuration")
        val mp3s = audioDir.listFiles()
            .apply {
                if(this == null || this.isEmpty()) {
                    throw VMakerException("Can't find in ${audioDir.absolutePath}")
                }
            }
            ?.filter { it.extension.equals(MP3.name, true) }
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
        println("Finish fill audio with result: ${result.size} audio files in video")
        return result
    }

    fun concatenationMp3(workDir: File, list: List<File>, tmpFiles: MutableSet<File>): FileDescription {
        println("Start concatenation audio")
        if(list.isEmpty()) {
            throw VMakerException("No audio files to concatenation")
        }
        if(list.size == 1) {
            val duration = editor.getDuration(list[0])
            return FileDescription(duration, list[0])
        }
        val concatenationMp3 = editor.concatenationMp3(workDir, list)
        tmpFiles.add(concatenationMp3)
        File("${workDir.absolutePath}/music_list.txt").apply { tmpFiles.add(this) }
        val duration = editor.getDuration(concatenationMp3)
        println("Finish concatenation audio")
        return FileDescription(duration, concatenationMp3)
    }
}