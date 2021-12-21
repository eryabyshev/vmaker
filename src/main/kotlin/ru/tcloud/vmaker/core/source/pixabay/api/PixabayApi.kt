package ru.tcloud.vmaker.core.source.pixabay.api

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.springframework.stereotype.Component
import ru.tcloud.vmaker.configuration.property.SourceProperty
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.source.MediaSource.PIXABAY
import ru.tcloud.vmaker.core.source.Resource
import ru.tcloud.vmaker.core.source.model.Video
import ru.tcloud.vmaker.core.source.pixabay.api.responce.PixabayVideoSearchResponse
import java.nio.file.Paths
import java.util.*
import java.util.Locale.getDefault

@Component
class PixabayApi(
    private val okHttpClient: OkHttpClient,
    private val sourceProperty: SourceProperty,
    private val mapper: ObjectMapper,
): Resource {

    override suspend fun searchVideo(page: Int, vararg tags: String) = coroutineScope<List<Video>> {
        val httpUrl = "${sourceProperty.pixabay.videoResource}?key=${sourceProperty.pixabay.authorizationToken}&q=${tags.joinToString("+")}&page=$page&per_page=200"
            .toHttpUrl()

        get(httpUrl).use {res ->
            mapper.readValue(res.string(), PixabayVideoSearchResponse::class.java).hits.map {
                Video(it.id.toString(), PIXABAY, Date(), it.videos.large.url, it.user)
            }
        }
    }

    override suspend fun downLoadVideo(video: Video, path: String) = coroutineScope<String> {
        val httpUrl = video.downLoadUrl.toHttpUrl()
        val file = Paths.get(path, "${PIXABAY.name.lowercase(getDefault())}_${video.id}.mp4").toFile()
            .apply { this.createNewFile() }
        get(httpUrl).use {
            file.writeBytes(it.bytes())
            file.absolutePath
        }
    }

    private fun get(httpUrl: HttpUrl): ResponseBody {
        val response =
            Request.Builder().url(httpUrl)
                .build()
                .let { okHttpClient.newCall(it) }
                .execute()
        if(!response.isSuccessful || response.body == null) {
            throw VMakerException("Request to $httpUrl finish with error!")
        }
        return response.body!!
    }
}