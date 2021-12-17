package ru.tcloud.vmaker.core.source.pixels.api

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.springframework.stereotype.Component
import ru.tcloud.vmaker.configuration.property.SourceProperty
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.model.Video
import ru.tcloud.vmaker.core.source.pixels.api.response.VideoSearchResponse
import ru.tcloud.vmaker.core.source.pixels.api.response.Videos
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.util.*

@Component
class PixelsApi(
    private val okHttpClient: OkHttpClient,
    private val sourceProperty: SourceProperty
) {

    suspend fun searchVideo(page: Int = 1, vararg tags: String) = coroutineScope<List<Video>> {
        val httpUrl = "${sourceProperty.pixels.videoResource}$searchPath?query=${tags.joinToString(",")}&page=$page&per_page=85"
            .toHttpUrl()
        mapper.readValue(get(httpUrl).toString(), VideoSearchResponse::class.java)
            .videos.map { Video(it.id.toString(), MediaSource.PEXELS, Instant.now(), it.videoFiles.last().id) }
    }

    suspend fun downLoadVideo(id: Long, path: String) = coroutineScope<String> {
        val httpUrl = "${sourceProperty.pixels.videoResource}/videos/$id".toHttpUrl()
        val file = Paths.get(path, "$id.mp4").toFile()
            .apply { this.createNewFile() }
        file.writeBytes(get(httpUrl).bytes())
        file.absolutePath
    }

    private fun get(httpUrl: HttpUrl): ResponseBody {
        val response =
            Request.Builder().url(httpUrl ?: throw VMakerException("Can't create response to video resource on PIXELS"))
                .header("Authorization", sourceProperty.pixels.authorizationToken)
                .build()
                .let { okHttpClient.newCall(it) }
                .execute()
        if(!response.isSuccessful || response.body == null) {
            throw VMakerException("Request to $httpUrl finish with error!")
        }
        return response.body!!
    }


    companion object {
        const val searchPath = "/search"
        val mapper = ObjectMapper()
    }

}