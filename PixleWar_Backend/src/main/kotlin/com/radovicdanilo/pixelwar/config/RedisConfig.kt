package com.radovicdanilo.pixelwar.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, ByteArray> {
        val template = RedisTemplate<String, ByteArray>()
        template.connectionFactory = factory

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = RedisSerializer.byteArray()

        template.setEnableTransactionSupport(false)

        template.afterPropertiesSet()
        return template
    }
}
