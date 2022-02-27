package ru.tcloud.vmaker.core.source.repository

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.model.Audio
import java.time.Duration

@Repository
class AudioRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val retrySpec: RetryBackoffSpec,
) : MediaRepository<Audio> {

    override suspend fun getNotUsage(src: MediaSource): List<Audio> {
        val query = Query.query(
            Criteria.where("src").`is`(src).andOperator(
            Criteria().orOperator(
                Criteria.where("usageDate").exists(false),
                Criteria.where("usageDate").`is`(null),
            ),
        ))
            .allowSecondaryReads()
            .cursorBatchSize(BATCH_SIZE)

        return mongoTemplate.find(query, Audio::class.java)
            .onErrorResume {
                println("Error occurred while trying to fetch from video: $it")
                Mono.empty()
            }
            .retryWhen(
                Retry.backoff(MONGO_QUERY_RETRY_COUNT, MONGO_BACKOFF_DURATION)
            )
            .collectList().awaitFirst()
    }

    override suspend fun update(entity: Audio): Audio {
        return mongoTemplate.save(entity)
            .retryWhen(retrySpec)
            .awaitFirst()
    }

    override suspend fun add(entities: Collection<Audio>): List<Audio> {
        return mongoTemplate.insertAll(entities).collectList().awaitFirst()
    }

    companion object {
        private val MONGO_BACKOFF_DURATION = Duration.ofMillis(100)
        private const val MONGO_QUERY_RETRY_COUNT = 2L
        const val BATCH_SIZE = 100
    }
}