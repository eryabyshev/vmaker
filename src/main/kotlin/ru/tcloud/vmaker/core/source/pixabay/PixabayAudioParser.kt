package ru.tcloud.vmaker.core.source.pixabay

import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.springframework.stereotype.Service
import ru.tcloud.vmaker.configuration.property.SourceProperty
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.model.Audio
import java.nio.file.Paths
import java.util.*

@Service
class PixabayAudioParser(
    private val sourceProperty: SourceProperty,
    private val okHttpClient: OkHttpClient
) {
    private val driver: WebDriver = ChromeDriver()

    enum class AudioDuration(val param: String) {
        BETWEEN_0_30("duration=0-30"),
        BETWEEN_30_120("duration=30-120"),
        BETWEEN_120_240("duration=120-240"),
        BETWEEN_240_480("duration=240-480"),
        MAX("duration=480-"),
        ;
    }

    fun properDuration() = listOf(AudioDuration.BETWEEN_120_240, AudioDuration.BETWEEN_240_480, AudioDuration.MAX)

    fun findAudio(page: Int, durationQuery: AudioDuration): List<Audio> {
        driver.get("${sourceProperty.pixabay.audioResource}?${durationQuery.param}")
        val jse = driver as JavascriptExecutor
        repeat(page * 10) {
            Thread.sleep(500)
            jse.executeScript("window.scrollBy(0,500)")
        }

        val result = driver.findElements(By.className("track-main"))
            .map {
                val title = it.text.split("\n").toMutableList()
                if (title.isEmpty()) {
                    title.add("")
                }
                if (title.size == 1) {
                    title.add("")
                }
                Audio(
                    id = it.findElement(By.className("duration")).getAttribute("data-track-id"),
                    src = MediaSource.PIXABAY,
                    downloadLink = it.findElement(By.className("download")).getAttribute("href"),
                    name = title[0],
                    author = title[1]
                )

            }
        driver.close()
        return result
    }

    suspend fun downLoadVideo(audio: Audio, path: String) = coroutineScope<String> {
        val httpUrl = audio.downloadLink.toHttpUrl()
        val file =
            Paths.get(path, "${MediaSource.PIXABAY.name.lowercase(Locale.getDefault())}_${audio.id}.mp3").toFile()
                .apply { this.createNewFile() }

        val response =
            Request.Builder().url(httpUrl)
                .build()
                .let { okHttpClient.newCall(it) }
                .execute()
        if (!response.isSuccessful || response.body == null) {
            throw VMakerException("Request to $httpUrl finish with error!")
        }
        response.body!!.use {
            file.writeBytes(it.bytes())
            file.absolutePath
        }
    }
}