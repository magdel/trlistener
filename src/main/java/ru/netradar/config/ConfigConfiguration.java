package ru.netradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netradar.server.JVMRuntime;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
public class ConfigConfiguration {

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public JVMRuntime jvmRuntime() {
        return new JVMRuntime();
    }

   }
