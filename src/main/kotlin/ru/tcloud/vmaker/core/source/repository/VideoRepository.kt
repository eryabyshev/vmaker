package ru.tcloud.vmaker.core.source.repository

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.model.Video
import java.time.Duration

@Repository
class VideoRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val retrySpec: RetryBackoffSpec
) : MediaRepository {

    override suspend fun getAlreadyUseId(ids: List<String>, src: MediaSource): Set<String> {
        val query = Query.query(Criteria.where("_id").inValues(ids).and("src").`is`(src))
            .allowSecondaryReads()
            .cursorBatchSize(BATCH_SIZE)

        return mongoTemplate.find(query, Video::class.java)
            .onErrorResume {
                println("Error occurred while trying to fetch from video: $it")
                Mono.empty()
            }
            .retryWhen(
                Retry.backoff(
                    MONGO_QUERY_RETRY_COUNT,
                    MONGO_BACKOFF_DURATION
                )
            )
            .collectMap { it.id }
            .awaitFirstOrDefault(emptyMap()).keys
    }

    suspend fun save(video: Video) {
        mongoTemplate.save(video)
            .retryWhen(retrySpec)
            .awaitFirst()
    }

    companion object {
        private val MONGO_BACKOFF_DURATION = Duration.ofMillis(100)
        private const val MONGO_QUERY_RETRY_COUNT = 2L
        const val BATCH_SIZE = 100
    }

}