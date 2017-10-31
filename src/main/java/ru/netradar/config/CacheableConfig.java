package ru.netradar.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CacheableConfig {
    public static final String CACHE_NAME_DEVICEUSERS = "device_users";


    @Bean
    public CacheManager webCacheManager() {
        final ConcurrentMapCacheManager concurrentMapCacheManager = new ConcurrentMapCacheManager(CACHE_NAME_DEVICEUSERS);
        concurrentMapCacheManager.setAllowNullValues(false);
        return concurrentMapCacheManager;
    }

}
