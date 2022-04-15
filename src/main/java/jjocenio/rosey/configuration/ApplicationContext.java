package jjocenio.rosey.configuration;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import jjocenio.rosey.component.DBServer;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.impl.history.DefaultHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

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
    public DBServer dbServer(File workingDirectory) throws PersistenceException {
        DBServer dbServer = new DBServer(workingDirectory);
        dbServer.start();
        return dbServer;
    }

    @Bean
    public File workingDirectory(@Value("${workingDir:#{null}}") String workingDir) {
        String workingPath = Optional.ofNullable(workingDir).orElseGet(() -> System.getProperty("user.home") + File.separator + ".rosey");
        File workingDirectory = new File(workingPath);

        System.setProperty("user.dir", workingDirectory.getAbsolutePath());
        return workingDirectory;
    }

    @Bean
    public History history(File workingDirectory) {
        return new DefaultHistory() {
            @Override
            public void attach(LineReader reader) {
                reader.setVariable(LineReader.HISTORY_FILE, new File(workingDirectory, "rosey.log"));
                super.attach(reader);
            }
        };
    }
}