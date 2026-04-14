package com.capstone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// [삭제] @Value, RedisStandaloneConfiguration, LettuceConnectionFactory import 제거
// → host, port, password 모두 application.yml에서 Spring Boot가 자동으로 읽음

@Configuration
public class RedisConfig {

    // [삭제] @Value("${spring.data.redis.host}") private String host;
    // [삭제] @Value("${spring.data.redis.port}") private int port;
    // → application.yml의 spring.data.redis.host / port / password를
    //   Spring Boot auto-configuration이 직접 처리하므로 불필요

    // [삭제] redisConnectionFactory() 빈 전체 제거
    // → 수동으로 LettuceConnectionFactory를 만들면
    //   application.yml의 timeout / pool / password 설정이 무시됨
    //   Spring Boot가 자동 생성한 RedisConnectionFactory를 그대로 주입받는 방식이 올바름

    @Bean
    // [삭제] @Primary 제거 → RedisTemplate<String, Object>가 하나만 등록되므로 불필요
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory
            // [유지] Spring Boot auto-configuration이 생성한 Factory 주입
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer();

        // [유지] Key / HashKey → 문자열, Value / HashValue → JSON 직렬화
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }
}