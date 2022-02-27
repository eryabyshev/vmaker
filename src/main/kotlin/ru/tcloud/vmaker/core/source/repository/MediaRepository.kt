package ru.tcloud.vmaker.core.source.repository

import ru.tcloud.vmaker.core.source.MediaSource

interface MediaRepository<T> {

    suspend fun getNotUsage(src: MediaSource): List<T>

    suspend fun update(entity: T): T

    suspend fun add(entities: Collection<T>): List<T>
}