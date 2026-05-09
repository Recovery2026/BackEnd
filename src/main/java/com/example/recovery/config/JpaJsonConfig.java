package com.example.recovery.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JpaJsonConfig {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer(JsonMapper jsonMapper) {
        return hibernateProperties ->
                hibernateProperties.put(
                        AvailableSettings.JSON_FORMAT_MAPPER,
                        new Jackson3FormatMapper(jsonMapper)
                );
    }
}
