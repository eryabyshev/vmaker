package ru.tcloud.vmaker.core.source.pixels

import org.springframework.stereotype.Service
import ru.tcloud.vmaker.configuration.property.SourceProperty
import ru.tcloud.vmaker.core.source.AbstractSource
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.pixels.api.PixelsApi
import ru.tcloud.vmaker.core.source.repository.PageCounterRepository
import ru.tcloud.vmaker.core.source.repository.VideoRepository

@Service
class Pixels(
    videoRepository: VideoRepository,
    pageCounterRepository: PageCounterRepository,
    resource: PixelsApi,
    sourceProperty: SourceProperty
) : AbstractSource(videoRepository, pageCounterRepository, resource, sourceProperty) {

    override fun getSourceType() = MediaSource.PEXELS
}