package jjocenio.rosey.configuration;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import jjocenio.rosey.component.DBServer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@Configuration
public class ApplicationContext {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public freemarker.template.Configuration templateConfiguration() throws IOException {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_29);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);

        cfg.setTemplateLoader(new FileTemplateLoader(new File("."), true));

        return cfg;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource(DBServer dbServer) {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DBServer dbServer() throws PersistenceException {
        DBServer dbServer = new DBServer();
        dbServer.start();
        return dbServer;
    }
}