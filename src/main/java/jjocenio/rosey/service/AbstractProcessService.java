package jjocenio.rosey.service;

import jjocenio.rosey.component.ExecutorServiceProvider;
import jjocenio.rosey.persistence.Row;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.joining;

public abstract class AbstractProcessService implements ProcessService {

    private final RowService rowService;
    private final ExecutorServiceProvider executorServiceProvider;

    protected AbstractProcessService(RowService rowService, ExecutorServiceProvider executorServiceProvider) {
        this.rowService = rowService;
        this.executorServiceProvider = executorServiceProvider;
    }

    public long processAll(ProcessContext context) {
        checkRunning();

        boolean includeFailed = context.getConfig().isIncludeFailed();
        long limit = context.getConfig().getLimit();
        long count = processAll(rowService.findAllByStatus(Row.Status.PENDING), context);
        if (includeFailed && (limit <= 0 || count < limit)) {
            count += processAll(rowService.findAllByStatus(Row.Status.FAILED), context);
        }

        return count;
    }

    public Row processRow(long rowId, ProcessContext context) throws IOException {
        checkRunning();

        Row row = rowService.findById(rowId).orElseThrow(() -> new IllegalArgumentException("Invalid row id"));

        if (row.getStatus() == Row.Status.PROCESSED) {
            throw new IllegalStateException("Row is already processed");
        }

        row = process(row, context);
        writeOutput(row, context);
        return row;
    }

    @Override
    public boolean isRunning() {
        return ((ThreadPoolExecutor) executorServiceProvider.getExecutorService()).getActiveCount() > 0;
    }

    protected void checkRunning() {
        if (isRunning()) {
            throw new IllegalStateException("Process is already running!");
        }
    }

    @SuppressWarnings("java:S112")
    protected long processAll(Iterable<Row> rows, ProcessContext context) {
        ExecutorService executorService = executorServiceProvider.getExecutorService();

        long limit = context.getConfig().getLimit();
        long count = 0;
        for (Row row: rows) {
            executorService.submit(() -> {
                this.process(row, context);

                try {
                    writeOutput(row, context);
                } catch (Exception e) {
                    throw new RuntimeException("Error writing output. ", e);
                }
            });
            count++;

            if (limit > 0 && count == limit) {
                break;
            }
        }

        executorService.shutdown();
        monitorFinishToCloseOutput(context, executorService);

        return count;
    }

    protected String getErrorDetail(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        if (throwable.getMessage() != null) {
            return throwable.getMessage();
        }

        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().lines().limit(5).collect(joining());
    }

    protected void updateRowStatus(Row row, Row.Status status, Throwable detail) {
        updateRowStatus(row, status, getErrorDetail(detail), null);
    }

    protected void updateRowStatus(Row row, Row.Status status, String detail, String output) {
        row.setStatus(status);
        row.setLastUpdate(new Date());
        row.setResultDetail(detail);
        row.setOutput(output);
        rowService.save(row);
    }

    protected Map<String, Object> getParams(ProcessContext processContext, Row row) {
        return Map.of("context", processContext, "row", row);
    }

    protected void writeOutput(Row row, ProcessContext context) throws IOException {
        OutputWriter writer = context.getOutputWriter();
        if (writer != null) {
            writer.write(row);
        }
    }

    protected void monitorFinishToCloseOutput(final ProcessContext processContext, final ExecutorService executorService) {
        Runnable monitorRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                            break;
                        }
                    } catch (InterruptedException ignore) {
                    }
                }

                if (processContext.getOutputWriter() != null) {
                    try {
                        processContext.getOutputWriter().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread monitor = new Thread(monitorRunnable);
        monitor.start();
    }

    protected abstract Row process(Row row, ProcessContext context);
}
