package ru.tcloud.vmaker.configuration

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfiguration {

    @Bean
    fun okHttpClient(): OkHttpClient {
      return OkHttpClient()
    }
}