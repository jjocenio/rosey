package jjocenio.rosey.component;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class TemplateHelper {

    private final Configuration templateConfiguration;

    @Autowired
    public TemplateHelper(Configuration templateConfiguration) {
        this.templateConfiguration = templateConfiguration;
    }

    public Template getTemplate(String templateString) throws IOException {
        if (templateString.startsWith("@")) {
            return templateConfiguration.getTemplate(templateString.substring(1));
        }

        return new Template(UUID.randomUUID().toString(), new StringReader(templateString), templateConfiguration);
    }

    public String merge(Template template, Map<String, Object> params) throws IOException, TemplateException {
        Writer stringWriter = new StringWriter();
        merge(template, params, stringWriter);

        stringWriter.flush();
        return stringWriter.toString();
    }

    private void merge(Template template, Map<String, Object> params, Writer output) throws IOException, TemplateException {
        template.process(params, output);
    }

    public Template getCompiledTemplate(Supplier<Template> cacheGetter, Supplier<String> configGetter, Consumer<Template> templateSetter) throws IOException {
        Template template = cacheGetter.get();
        if (template == null) {
            template = getTemplate(configGetter.get());
            templateSetter.accept(template);
        }

        return template;
    }
}
