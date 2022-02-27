package ru.tcloud.vmaker.configuration

import com.mongodb.MongoClientException
import com.mongodb.MongoServerException
import com.mongodb.MongoSocketException
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.dao.DataAccessException
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.time.Duration

@Configuration
class MongoConfiguration(private val mongoProperties: MongoProperties) {


    @Bean
    fun mappingMongoConverter() = MappingMongoConverter(NoOpDbRefResolver.INSTANCE, MongoMappingContext()).also {
        it.setTypeMapper(DefaultMongoTypeMapper(null))
        it.afterPropertiesSet()
    }

    @Bean
    fun retryBackoffSpec(): RetryBackoffSpec = Retry
        .backoff(MONGO_QUERY_RETRY_COUNT, Duration.ofMillis(MONGO_QUERY_RETRY_BETWEEN_MS))
        .doAfterRetry {
            println("Mongo query retry signal received: ${it.failure().message} (total retries: ${it.totalRetries()})")
        }
        .filter {
            it is MongoServerException
                    || it is MongoClientException
                    || it is DataAccessException
                    || it is MongoSocketException
        }

    @Bean
    fun mongoClient(): MongoClient = MongoClients.create(mongoProperties.uri)

    @Bean
    fun reactiveMongoTemplate(converter: MappingMongoConverter) = ReactiveMongoTemplate(
        SimpleReactiveMongoDatabaseFactory(mongoClient(), mongoProperties.database), converter
    )

    companion object {
        private const val MONGO_QUERY_RETRY_COUNT = 12L
        private const val MONGO_QUERY_RETRY_BETWEEN_MS = 100L
    }

}