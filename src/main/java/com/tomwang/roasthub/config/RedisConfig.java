package com.tomwang.roasthub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, byte[]> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for the keys
        template.setKeySerializer(new StringRedisSerializer());

        // Custom serializer for byte[]
        RedisSerializer<byte[]> byteSerializer = new RedisSerializer<byte[]>() {
            @Override
            public byte[] serialize(byte[] bytes) {
                return bytes;
            }

            @Override
            public byte[] deserialize(byte[] bytes) {
                return bytes;
            }
        };

        // Use byte array serializer for the values
        template.setValueSerializer(byteSerializer);

        // For Hash operations, if needed
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(byteSerializer);

        template.afterPropertiesSet();
        return template;
    }
}

