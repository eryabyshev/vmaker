package ru.tcloud.vmaker.core.source

import ru.tcloud.vmaker.core.source.model.Video


interface Source {

    suspend fun findVideo(path: String, vararg tags: String): Video

    suspend fun findAudio(path: String, vararg tags: String)

    fun getSourceType(): MediaSource
}