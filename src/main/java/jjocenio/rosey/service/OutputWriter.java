package jjocenio.rosey.service;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import jjocenio.rosey.component.TemplateHelper;
import jjocenio.rosey.persistence.Row;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

            if (!append) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    private OutputStream getOutputStream(Row row) throws IOException {
        try {
            String path = templateHelper.merge(pathTemplate, Map.of("row", row));
            OutputStream outputStream = outputMap.get(path);

            if (outputStream == null) {
                File outputFile = new File(path);

                if (!override && outputFile.exists()) {
                    throw new IOException("File exists: " + path);
                }

                outputFile.getParentFile().mkdirs();

                outputStream = new FileOutputStream(path, !override);
                if (append) {
                    outputMap.put(path, outputStream);
                }
            }

            return outputStream;
        } catch (TemplateException e) {
            throw new IOException(e);
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
