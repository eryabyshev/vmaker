package ru.tcloud.vmaker.core.source

import ru.tcloud.vmaker.core.source.model.Video

interface Resource {

    suspend fun searchVideo(page: Int = 1, vararg tags: String): List<Video>

    suspend fun downLoadVideo(video: Video, path: String): String
}