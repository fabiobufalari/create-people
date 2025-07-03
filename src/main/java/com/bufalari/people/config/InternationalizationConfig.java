package com.bufalari.people.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Internationalization Configuration for Construction Hub Financial System
 * 
 * EN: Configures multi-language support for the application
 * PT: Configura suporte a múltiplos idiomas para a aplicação
 * FR: Configure le support multilingue pour l'application
 * ZH: 为应用程序配置多语言支持
 */
@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    /**
     * Message source configuration for internationalization
     * 
     * EN: Configures the message source to load translation files
     * PT: Configura a fonte de mensagens para carregar arquivos de tradução
     * FR: Configure la source de messages pour charger les fichiers de traduction
     * ZH: 配置消息源以加载翻译文件
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        
        // Set the base name for message files
        messageSource.setBasename("classpath:i18n/messages");
        
        // Set default encoding
        messageSource.setDefaultEncoding("UTF-8");
        
        // Cache duration in seconds (3600 = 1 hour)
        messageSource.setCacheSeconds(3600);
        
        // Fallback to system locale if message not found
        messageSource.setFallbackToSystemLocale(false);
        
        // Use code as default message if message not found
        messageSource.setUseCodeAsDefaultMessage(true);
        
        return messageSource;
    }

    /**
     * Locale resolver configuration
     * 
     * EN: Determines how the locale is resolved for each request
     * PT: Determina como o locale é resolvido para cada requisição
     * FR: Détermine comment les paramètres régionaux sont résolus pour chaque demande
     * ZH: 确定如何为每个请求解析区域设置
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        
        // Set default locale to English (Canadian)
        localeResolver.setDefaultLocale(Locale.forLanguageTag("en-CA"));
        
        return localeResolver;
    }

    /**
     * Locale change interceptor configuration
     * 
     * EN: Allows changing locale via request parameter
     * PT: Permite alterar o locale via parâmetro de requisição
     * FR: Permet de changer les paramètres régionaux via le paramètre de demande
     * ZH: 允许通过请求参数更改区域设置
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        
        // Parameter name to change locale (e.g., ?lang=pt-BR)
        interceptor.setParamName("lang");
        
        return interceptor;
    }

    /**
     * Add interceptors to the registry
     * 
     * EN: Registers the locale change interceptor
     * PT: Registra o interceptor de mudança de locale
     * FR: Enregistre l'intercepteur de changement de paramètres régionaux
     * ZH: 注册区域设置更改拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

