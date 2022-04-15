package jjocenio.rosey.service;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import jjocenio.rosey.component.TemplateHelper;
import jjocenio.rosey.persistence.Row;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OutputWriter implements Closeable {

    private final Map<String, OutputStream> outputMap = new ConcurrentHashMap<>();

    private final TemplateHelper templateHelper;
    private final Template pathTemplate;
    private final boolean override;
    private final boolean append;

    public OutputWriter(TemplateHelper templateHelper, Template pathTemplate, boolean override, boolean append) {
        this.templateHelper = templateHelper;
        this.pathTemplate = pathTemplate;
        this.override = override;
        this.append = append;
    }

    public void write(Row row) throws IOException {
        if (row != null && row.getOutput() != null) {
            OutputStream outputStream = getOutputStream(row);
            outputStream.write(row.getOutput().getBytes());
            outputStream.flush();

            if (!append) {
                outputStream.close();
            }
        }
    }

    private OutputStream getOutputStream(Row row) throws IOException {
        try {
            String path = templateHelper.merge(pathTemplate, Map.of("row", row));
            if (append) {
                outputMap.computeIfAbsent(path, this::createOutputStream);
            }

            return append ? outputMap.get(path) : createOutputStream(path);
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }

    private OutputStream createOutputStream(String filePath) {
        File outputFile = new File(filePath);

        if (!override && outputFile.exists()) {
            throw new IllegalStateException("File exists: " + filePath);
        }

        outputFile.getParentFile().mkdirs();

        try {
            return new FileOutputStream(filePath, !override);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid output file.", e);
        }
    }

    @Override
    public void close() throws IOException {
        outputMap.values().forEach(output -> {
            try {
                output.flush();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
