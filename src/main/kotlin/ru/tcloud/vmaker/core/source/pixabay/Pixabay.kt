package ru.tcloud.vmaker.core.source.pixabay

import org.springframework.stereotype.Service
import ru.tcloud.vmaker.configuration.property.SourceProperty
import ru.tcloud.vmaker.core.MediaType
import ru.tcloud.vmaker.core.source.AbstractSource
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.model.Audio
import ru.tcloud.vmaker.core.source.pixabay.PixabayAudioParser.*
import ru.tcloud.vmaker.core.source.pixels.api.PixelsApi
import ru.tcloud.vmaker.core.source.repository.AudioRepository
import ru.tcloud.vmaker.core.source.repository.PageCounterRepository
import ru.tcloud.vmaker.core.source.repository.VideoRepository

@Service
class Pixabay(
    videoRepository: VideoRepository,
    pageCounterRepository: PageCounterRepository,
    resource: PixelsApi,
    sourceProperty: SourceProperty,
    private val pixabayAudioParser: PixabayAudioParser,
    private val audioRepository: AudioRepository,
    ) : AbstractSource(videoRepository, pageCounterRepository, resource, sourceProperty) {

    override suspend fun findAudio(path: String): Audio? {
        var audio = audioRepository.getNotUsage(getSourceType()).firstOrNull()
        if(audio == null) {
            var tryCount = 0
            while (tryCount <= sourceProperty.searchTry) {
                audio = pixabayAudioParser.properDuration().map {
                    updateAudioCollections(it)
                }.firstOrNull()
                if(audio != null) {
                    break
                }
                tryCount++
            }
            if(audio == null) {
                return null
            }
        }
        pixabayAudioParser.downLoadVideo(audio, path)
        audioRepository.update(audio)
        return audio
    }

    private suspend fun updateAudioCollections(duration: AudioDuration): Audio? {
        val page = pageCounterRepository.getCounter(MediaType.AUDIO, getSourceType())
        return pixabayAudioParser.findAudio(page,duration)
            .apply { pageCounterRepository.increaseCounter(MediaType.AUDIO, getSourceType()) }
            .apply { audioRepository.add(this) }
            .firstOrNull()
    }

    override fun getSourceType() = MediaSource.PIXABAY
}