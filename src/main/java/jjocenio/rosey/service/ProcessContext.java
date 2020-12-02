package jjocenio.rosey.service;

public class ProcessContext {

    private final ProcessConfig config;
    private final OutputWriter outputWriter;

    public ProcessContext(ProcessConfig config, OutputWriter outputWriter) {
        this.config = config;
        this.outputWriter = outputWriter;
    }

    public ProcessConfig getConfig() {
        return config;
    }

    public OutputWriter getOutputWriter() {
        return outputWriter;
    }
}
