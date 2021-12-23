package ru.tcloud.vmaker.core.source

import ru.tcloud.vmaker.configuration.property.SourceProperty
import ru.tcloud.vmaker.core.MediaType
import ru.tcloud.vmaker.core.source.model.Video
import ru.tcloud.vmaker.core.source.repository.PageCounterRepository
import ru.tcloud.vmaker.core.source.repository.VideoRepository

abstract class AbstractSource(
    protected val videoRepository: VideoRepository,
    protected val pageCounterRepository: PageCounterRepository,
    protected val resource: Resource,
    protected val sourceProperty: SourceProperty,
): Source {

    override suspend fun findVideo(path: String, vararg tags: String): Video? {
        var video = videoRepository.getNotUsage(getSourceType()).firstOrNull()
        if (video == null) {
            var tryCount = 0
            while (tryCount <= sourceProperty.searchTry) {
                video = updateVideoCollections(*tags)
                if(video != null) {
                    break
                }
                tryCount++
            }
            if(video == null) {
                return null
            }
        }
        resource.downLoadVideo(video, path)
        videoRepository.update(video)
        return video
    }

    private suspend fun updateVideoCollections(vararg tags: String): Video? {
        val page = pageCounterRepository.getCounter(MediaType.VIDEO, getSourceType())
        return resource.searchVideo(page, *tags)
            .apply { pageCounterRepository.increaseCounter(MediaType.VIDEO, getSourceType()) }
            .apply { videoRepository.add(this) }
            .firstOrNull()
    }


}