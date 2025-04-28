package com.bufalari.people.config;


import com.bufalari.people.auditing.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProviderPeople") // Ref único
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProviderPeople() { // Nome do bean corresponde
        return new AuditorAwareImpl();
    }
}