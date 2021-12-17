package ru.tcloud.vmaker.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import ru.tcloud.vmaker.configuration.property.SourceProperty

@Configuration
@EnableConfigurationProperties(
    SourceProperty::class
)
class PropertyHolder