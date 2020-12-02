package jjocenio.rosey.service.http;

import freemarker.template.Template;
import jjocenio.rosey.service.OutputWriter;
import jjocenio.rosey.service.ProcessConfig;
import jjocenio.rosey.service.ProcessContext;

public class HttpProcessContext extends ProcessContext {

    private Template compiledUrlTemplate;
    private Template compiledBodyTemplate;
    private Template CompiledHeaderTemplate;

    public HttpProcessContext(ProcessConfig config, OutputWriter outputWriter) {
        super(config, outputWriter);
    }

    public Template getCompiledUrlTemplate() {
        return compiledUrlTemplate;
    }

    public void setCompiledUrlTemplate(Template urlCompiledTemplate) {
        this.compiledUrlTemplate = urlCompiledTemplate;
    }

    public Template getCompiledBodyTemplate() {
        return compiledBodyTemplate;
    }

    public void setCompiledBodyTemplate(Template bodyCompiledTemplate) {
        this.compiledBodyTemplate = bodyCompiledTemplate;
    }

    public Template getCompiledHeaderTemplate() {
        return CompiledHeaderTemplate;
    }

    public void setCompiledHeaderTemplate(Template headersCompiledTemplate) {
        this.CompiledHeaderTemplate = headersCompiledTemplate;
    }

    public HttpProcessConfig getHttpConfig() {
        return (HttpProcessConfig) super.getConfig();
    }
}
