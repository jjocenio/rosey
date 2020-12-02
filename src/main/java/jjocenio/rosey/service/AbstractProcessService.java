package jjocenio.rosey.service;

import jjocenio.rosey.persistence.Row;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractProcessService implements ProcessService {

    private final RowService rowService;
    private final ExecutorService executorService;

    public AbstractProcessService(RowService rowService, ExecutorService executorService) {

        this.rowService = rowService;
        this.executorService = executorService;
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

        Row row = rowService.findById(rowId).get();

        if (row.getStatus() == Row.Status.PROCESSED) {
            throw new IllegalStateException("Row is already processed");
        }

        row = process(row, context);
        writeOutput(row, context);
        return row;
    }

    @Override
    public boolean isRunning() {
        return ((ThreadPoolExecutor) executorService).getActiveCount() > 0;
    }

    protected void checkRunning() {
        if (isRunning()) {
            throw new IllegalStateException("Process is already running!");
        }
    }

    protected long processAll(Iterable<Row> rows, ProcessContext context) {
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

        return count;
    }

    protected void updateRowStatus(Row row, Row.Status status, String detail) {
        updateRowStatus(row, status, detail, null);
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

    protected abstract Row process(Row row, ProcessContext context);
}
