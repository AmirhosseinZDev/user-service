package com.ftgo.user.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tosan.validation.Validation;
import com.tosan.validation.core.ValidatorBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * @author AmirHossein ZamanZade
 * @since 4/18/25
 */
@Configuration
public class UserServiceConfiguration {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

    @Bean
    @Primary
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
                .json()
                .build();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN));
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Bean
    public Validation validation(ValidatorBuilder validatorBuilder) {
        return new Validation(validatorBuilder, new HashMap<>());
    }

    @Bean
    public ValidatorBuilder validatorBuilder() {
        return new ValidatorBuilder(new HashMap<>());
    }


    @Bean("devPropertySourcesPlaceholderConfigurer")
    @Profile("dev")
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return generatePlaceHolderConfigurer("/config/application-dev.properties",
                "config/application-dev.properties");
    }

    @Bean("testPropertySourcesPlaceholderConfigurer")
    @Profile("test")
    public PropertySourcesPlaceholderConfigurer testPropertySourcesPlaceholderConfigurer() {
        return generatePlaceHolderConfigurer("/config/application-test.properties",
                "config/application-test.properties");
    }

    @Bean("propertySourcesPlaceholderConfigurer")
    @ConditionalOnMissingBean(PropertySourcesPlaceholderConfigurer.class)
    public PropertySourcesPlaceholderConfigurer productionPropertySourcesPlaceholderConfigurer() {
        return generatePlaceHolderConfigurer(null, null);
    }

    private PropertySourcesPlaceholderConfigurer generatePlaceHolderConfigurer(String classpath,
            String fileSystemPath) {
        PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        placeholderConfigurer.setFileEncoding("UTF-8");
        placeholderConfigurer.setLocations(getResources(classpath, fileSystemPath));
        placeholderConfigurer.setIgnoreResourceNotFound(true);
        placeholderConfigurer.setLocalOverride(true);
        return placeholderConfigurer;
    }

    private Resource[] getResources(String classpath, String fileSystemPath) {
        if (classpath != null && fileSystemPath != null) {
            return new Resource[]{
                    new ClassPathResource("/config/application.properties"),
                    new ClassPathResource(classpath),
                    new FileSystemResource(fileSystemPath),
                    new ClassPathResource("/config/application.yml")
            };
        } else {
            return new Resource[]{
                    new ClassPathResource("/config/application.properties"),
                    new FileSystemResource("config/application.properties"),
                    new ClassPathResource("/config/application.yml")
            };
        }
    }
}
