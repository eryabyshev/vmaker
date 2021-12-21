package ru.tcloud.vmaker.core.source.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import ru.tcloud.vmaker.core.source.MediaSource
import java.util.*

@Document
data class Video(
    @Id
    @Field("_id")
    val id: String,
    val src: MediaSource,
    val usageDate: Date,
    val downLoadUrl: String,
    val author: String
)