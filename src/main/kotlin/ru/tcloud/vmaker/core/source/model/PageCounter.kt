package ru.tcloud.vmaker.core.source.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.tcloud.vmaker.core.MediaType
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.model.PageCounter.Companion.COLLECTION_NAME

@Document(COLLECTION_NAME)
class PageCounter(
   @Id
   val id: ObjectId = ObjectId.get(),
   val source: MediaSource,
   val mediaType: MediaType,
   var counter: Int,
) {
    companion object {
        const val COLLECTION_NAME = "media.page.lock"
    }
}