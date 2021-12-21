package ru.tcloud.vmaker.core.source.pixels

import org.springframework.stereotype.Service
import ru.tcloud.vmaker.configuration.property.SourceProperty
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.Source
import ru.tcloud.vmaker.core.source.model.Video
import ru.tcloud.vmaker.core.source.pixels.api.PixelsApi
import ru.tcloud.vmaker.core.source.repository.VideoRepository

@Service
class Pixels(
    private val videoRepository: VideoRepository,
    private val pixelsApi: PixelsApi,
    private val sourceProperty: SourceProperty,
): Source {

    override suspend fun findVideo(path: String, vararg tags: String): Video {
        var page = 1
        var video: Video? = null
        while (page <= sourceProperty.searchTry) {
            val findVideo = pixelsApi.searchVideo(page, *tags)
            val alreadyUseId = videoRepository.getAlreadyUseId(findVideo.map { it.id }, getSourceType())
            video = findVideo.filter { !alreadyUseId.contains(it.id) }.firstOrNull()
            if(video != null) {
                break
            }
            page++
        }
        if(video == null) {
            throw VMakerException("Can't find proper video")
        }
        pixelsApi.downLoadVideo(video, path)
        videoRepository.save(video)
        return video
    }

    override suspend fun findAudio(path: String, vararg tags: String) {
        TODO("Not yet implemented")
    }

    override fun getSourceType() = MediaSource.PEXELS
}