package jjocenio.rosey.service.text;

import freemarker.template.Template;
import jjocenio.rosey.service.OutputWriter;
import jjocenio.rosey.service.ProcessConfig;
import jjocenio.rosey.service.ProcessContext;

public class TextProcessContext extends ProcessContext {

    private Template compiledOutputPathTemplate;
    private Template compiledOutputTemplate;

    public TextProcessContext(ProcessConfig config, OutputWriter outputWriter) {
        super(config, outputWriter);
    }

    public Template getCompiledOutputPathTemplate() {
        return compiledOutputPathTemplate;
    }

    public void setCompiledOutputPathTemplate(Template compiledOutputPathTemplate) {
        this.compiledOutputPathTemplate = compiledOutputPathTemplate;
    }

    public Template getCompiledOutputTemplate() {
        return compiledOutputTemplate;
    }

    public void setCompiledOutputTemplate(Template compiledOutputTemplate) {
        this.compiledOutputTemplate = compiledOutputTemplate;
    }

    public TextProcessConfig getTextConfig() {
        return (TextProcessConfig) super.getConfig();
    }
}
