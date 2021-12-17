package ru.tcloud.vmaker.core.source.repository

import ru.tcloud.vmaker.core.source.MediaSource

interface MediaRepository {

    suspend fun getAlreadyUseId(ids: List<String>, src: MediaSource): Set<String>
}