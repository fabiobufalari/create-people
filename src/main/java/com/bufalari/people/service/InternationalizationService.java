package com.bufalari.people.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Internationalization Service for Construction Hub Financial System
 * 
 * EN: Provides methods to retrieve localized messages and manage language preferences
 * PT: Fornece métodos para recuperar mensagens localizadas e gerenciar preferências de idioma
 * FR: Fournit des méthodes pour récupérer les messages localisés et gérer les préférences linguistiques
 * ZH: 提供检索本地化消息和管理语言首选项的方法
 */
@Service
@RequiredArgsConstructor
public class InternationalizationService {

    private final MessageSource messageSource;

    // Supported languages with their display names
    private static final Map<String, String> SUPPORTED_LANGUAGES = new LinkedHashMap<>();
    
    static {
        SUPPORTED_LANGUAGES.put("en-CA", "English (Canada)");
        SUPPORTED_LANGUAGES.put("pt-BR", "Português (Brasil)");
        SUPPORTED_LANGUAGES.put("fr-CA", "Français (Canada)");
        SUPPORTED_LANGUAGES.put("zh-CN", "中文 (简体)");
    }

    /**
     * Get localized message by key
     * 
     * EN: Retrieves a localized message for the current locale
     * PT: Recupera uma mensagem localizada para o locale atual
     * FR: Récupère un message localisé pour les paramètres régionaux actuels
     * ZH: 检索当前区域设置的本地化消息
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message by key with parameters
     * 
     * EN: Retrieves a localized message with parameter substitution
     * PT: Recupera uma mensagem localizada com substituição de parâmetros
     * FR: Récupère un message localisé avec substitution de paramètres
     * ZH: 检索带有参数替换的本地化消息
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message for specific locale
     * 
     * EN: Retrieves a localized message for a specific locale
     * PT: Recupera uma mensagem localizada para um locale específico
     * FR: Récupère un message localisé pour des paramètres régionaux spécifiques
     * ZH: 检索特定区域设置的本地化消息
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * Get localized message for specific locale with parameters
     * 
     * EN: Retrieves a localized message for a specific locale with parameter substitution
     * PT: Recupera uma mensagem localizada para um locale específico com substituição de parâmetros
     * FR: Récupère un message localisé pour des paramètres régionaux spécifiques avec substitution de paramètres
     * ZH: 检索特定区域设置的带有参数替换的本地化消息
     */
    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    /**
     * Get supported languages
     * 
     * EN: Returns a map of supported language codes and their display names
     * PT: Retorna um mapa de códigos de idioma suportados e seus nomes de exibição
     * FR: Renvoie une carte des codes de langue pris en charge et de leurs noms d'affichage
     * ZH: 返回支持的语言代码及其显示名称的映射
     */
    public Map<String, String> getSupportedLanguages() {
        return new HashMap<>(SUPPORTED_LANGUAGES);
    }

    /**
     * Get supported language codes
     * 
     * EN: Returns a list of supported language codes
     * PT: Retorna uma lista de códigos de idioma suportados
     * FR: Renvoie une liste des codes de langue pris en charge
     * ZH: 返回支持的语言代码列表
     */
    public List<String> getSupportedLanguageCodes() {
        return new ArrayList<>(SUPPORTED_LANGUAGES.keySet());
    }

    /**
     * Check if language is supported
     * 
     * EN: Checks if a language code is supported by the system
     * PT: Verifica se um código de idioma é suportado pelo sistema
     * FR: Vérifie si un code de langue est pris en charge par le système
     * ZH: 检查系统是否支持语言代码
     */
    public boolean isLanguageSupported(String languageCode) {
        return SUPPORTED_LANGUAGES.containsKey(languageCode);
    }

    /**
     * Get default language
     * 
     * EN: Returns the default language code
     * PT: Retorna o código do idioma padrão
     * FR: Renvoie le code de langue par défaut
     * ZH: 返回默认语言代码
     */
    public String getDefaultLanguage() {
        return "en-CA";
    }

    /**
     * Get current language
     * 
     * EN: Returns the current locale language code
     * PT: Retorna o código do idioma do locale atual
     * FR: Renvoie le code de langue des paramètres régionaux actuels
     * ZH: 返回当前区域设置语言代码
     */
    public String getCurrentLanguage() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        String languageTag = currentLocale.toLanguageTag();
        
        // Map common locale variations to our supported languages
        switch (languageTag.toLowerCase()) {
            case "en":
            case "en-us":
            case "en-ca":
                return "en-CA";
            case "pt":
            case "pt-br":
                return "pt-BR";
            case "fr":
            case "fr-ca":
                return "fr-CA";
            case "zh":
            case "zh-cn":
                return "zh-CN";
            default:
                return getDefaultLanguage();
        }
    }

    /**
     * Get localized validation messages
     * 
     * EN: Returns a map of common validation messages for the current locale
     * PT: Retorna um mapa de mensagens de validação comuns para o locale atual
     * FR: Renvoie une carte des messages de validation courants pour les paramètres régionaux actuels
     * ZH: 返回当前区域设置的常见验证消息映射
     */
    public Map<String, String> getValidationMessages() {
        Map<String, String> messages = new HashMap<>();
        
        messages.put("required", getMessage("validation.required"));
        messages.put("email.invalid", getMessage("validation.email.invalid"));
        messages.put("phone.invalid", getMessage("validation.phone.invalid"));
        messages.put("amount.invalid", getMessage("validation.amount.invalid"));
        messages.put("date.invalid", getMessage("validation.date.invalid"));
        
        return messages;
    }

    /**
     * Get localized error messages
     * 
     * EN: Returns a map of common error messages for the current locale
     * PT: Retorna um mapa de mensagens de erro comuns para o locale atual
     * FR: Renvoie une carte des messages d'erreur courants pour les paramètres régionaux actuels
     * ZH: 返回当前区域设置的常见错误消息映射
     */
    public Map<String, String> getErrorMessages() {
        Map<String, String> messages = new HashMap<>();
        
        messages.put("general", getMessage("error.general"));
        messages.put("network", getMessage("error.network"));
        messages.put("server", getMessage("error.server"));
        messages.put("not.found", getMessage("error.not.found"));
        messages.put("unauthorized", getMessage("error.unauthorized"));
        messages.put("forbidden", getMessage("error.forbidden"));
        
        return messages;
    }

    /**
     * Get localized success messages
     * 
     * EN: Returns a map of common success messages for the current locale
     * PT: Retorna um mapa de mensagens de sucesso comuns para o locale atual
     * FR: Renvoie une carte des messages de succès courants pour les paramètres régionaux actuels
     * ZH: 返回当前区域设置的常见成功消息映射
     */
    public Map<String, String> getSuccessMessages() {
        Map<String, String> messages = new HashMap<>();
        
        messages.put("created", getMessage("success.created"));
        messages.put("updated", getMessage("success.updated"));
        messages.put("deleted", getMessage("success.deleted"));
        messages.put("saved", getMessage("success.saved"));
        
        return messages;
    }

    /**
     * Get localized navigation labels
     * 
     * EN: Returns a map of navigation labels for the current locale
     * PT: Retorna um mapa de rótulos de navegação para o locale atual
     * FR: Renvoie une carte des étiquettes de navigation pour les paramètres régionaux actuels
     * ZH: 返回当前区域设置的导航标签映射
     */
    public Map<String, String> getNavigationLabels() {
        Map<String, String> labels = new HashMap<>();
        
        labels.put("dashboard", getMessage("nav.dashboard"));
        labels.put("accounts.payable", getMessage("nav.accounts.payable"));
        labels.put("accounts.receivable", getMessage("nav.accounts.receivable"));
        labels.put("cash.flow", getMessage("nav.cash.flow"));
        labels.put("projects", getMessage("nav.projects"));
        labels.put("people", getMessage("nav.people"));
        labels.put("companies", getMessage("nav.companies"));
        labels.put("suppliers", getMessage("nav.suppliers"));
        labels.put("employees", getMessage("nav.employees"));
        labels.put("materials", getMessage("nav.materials"));
        labels.put("reports", getMessage("nav.reports"));
        labels.put("settings", getMessage("nav.settings"));
        labels.put("admin", getMessage("nav.admin"));
        
        return labels;
    }
}

