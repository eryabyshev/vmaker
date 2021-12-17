package ru.tcloud.vmaker.core.source.pixels

import kotlinx.coroutines.internal.artificialFrame
import org.springframework.stereotype.Service
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.Source
import ru.tcloud.vmaker.core.source.model.Video
import ru.tcloud.vmaker.core.source.pixels.api.PixelsApi
import ru.tcloud.vmaker.core.source.repository.VideoRepository

@Service
class Pixels(
    private val videoRepository: VideoRepository,
    private val pixelsApi: PixelsApi
): Source {

    override suspend fun findVideo(path: String, vararg tags: String): Video {
        var page = 1
        var video: Video?

        while (true) {
            val findVideo = pixelsApi.searchVideo(page, *tags)
            val alreadyUseId = videoRepository.getAlreadyUseId(findVideo.map { it.id }, getSourceType())
            video = findVideo.filter { !alreadyUseId.contains(it.id) }.firstOrNull()
            if(video != null) {
                break
            }
            page++
        }
        video as Video
        pixelsApi.downLoadVideo(video.downLoadId, path)
        videoRepository.save(video)
        return video
    }

    override suspend fun findAudio(path: String, vararg tags: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getSourceType() = MediaSource.PEXELS
}