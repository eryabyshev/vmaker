package ru.tcloud.vmaker.core.source.repository

import kotlinx.coroutines.reactive.awaitFirst
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import ru.tcloud.vmaker.core.MediaType
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.model.PageCounter

@Repository
class PageCounterRepository(private val mongoTemplate: ReactiveMongoTemplate) {

    suspend fun getCounter(mediaType: MediaType, mediaSource: MediaSource): Int {
        val pageCounter =
            mongoTemplate.findOne(
                getQuery(mediaType, mediaSource),
                PageCounter::class.java
            )
                .awaitFirst()
        if (pageCounter != null) {
            return pageCounter.counter
        }
        return mongoTemplate.save(PageCounter(ObjectId.get(), mediaSource, mediaType, 1)).awaitFirst().counter
    }

    suspend fun increaseCounter(mediaType: MediaType, mediaSource: MediaSource) {
        mongoTemplate.updateFirst(
            getQuery(mediaType, mediaSource),
            Update().inc("counter", 1),
            PageCounter::class.java
        ).awaitFirst()
    }

    private fun getQuery(mediaType: MediaType, mediaSource: MediaSource): Query {
        return Query.query(Criteria.where("mediaType").`is`(mediaType).and("source").`is`(mediaSource))
    }
}