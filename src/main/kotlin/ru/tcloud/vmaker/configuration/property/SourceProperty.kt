package ru.tcloud.vmaker.configuration.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "source")
@Validated
@ConstructorBinding
data class SourceProperty(
    val pixels: Property,
    val pixabay: Property,
    val searchTry: Int
) {
    data class Property(
        val authorizationToken: String,
        val videoResource: String,
        val audioResource: String,
    )
}