package dev.aparikh.jsonplaceholder.config;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for JTE templating engine.
 */
@Configuration
public class JteConfig {

    /**
     * Creates a JTE template engine bean.
     *
     * @return The configured template engine
     */
    @Bean
    public TemplateEngine templateEngine() {
        // Use ResourceCodeResolver to load templates from the classpath
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("jte");
        
        // Create and return the template engine
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }
}