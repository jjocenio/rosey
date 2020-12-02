package jjocenio.rosey.service;

import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.persistence.repository.RowRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class RowService extends DataChanger {

    private final RowRepository repository;

    @Autowired
    public RowService(RowRepository repository, ObjectProvider<DataListener> dataListenerObjectProvider) {
        super(dataListenerObjectProvider);
        this.repository = repository;
    }

    public <S extends Row> S save(S entity) {
        return repository.save(entity);
    }

    public <S extends Row> Iterable<S> saveAll(Iterable<S> entities) {
        return repository.saveAll(entities);
    }

    public Optional<Row> findById(Long aLong) {
        return repository.findById(aLong);
    }

    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    public Iterable<Row> findAll() {
        return repository.findAll();
    }

    public Iterable<Row> findAllById(Iterable<Long> longs) {
        return repository.findAllById(longs);
    }

    public long count() {
        return repository.count();
    }

    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }

    public void delete(Row entity) {
        repository.delete(entity);
    }

    public void deleteAll(Iterable<? extends Row> entities) {
        repository.deleteAll(entities);
    }

    public void deleteAll() {
        repository.deleteAll();
        callDataListeners();
    }

    public void setStatus(long orderId, Row.Status status) {
        Row orderProcess = repository.findById(orderId).get();
        orderProcess.setStatus(status);
        orderProcess.setLastUpdate(new Date());
        orderProcess.setResultDetail("Updated manually");
        repository.save(orderProcess);
        callDataListeners();
    }

    public Map<Row.Status, Long> countGroupByStatus() {
        return repository.countGroupByStatus()
                .stream()
                .collect(Collectors.toUnmodifiableMap(k -> (Row.Status) k[0], v -> (Long) v[1]));
    }

    public List<Row> findAllByStatus(Row.Status status) {
        return repository.findAllByStatus(status);
    }

    public void updateProcessingToPending() {
        repository.updateProcessingToPending();
        callDataListeners();
    }

    public void loadFromFile(String file, FileLoadProgressTracker tracker) throws IOException {
        FileReader reader = new FileReader(file);
        CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

        List<CSVRecord> records = parser.getRecords();
        callProgressTracker(tracker, t -> t.fileLoadingStarted(file, records.size()));

        final AtomicLong totalCount = new AtomicLong(0l);
        final AtomicLong idTracker = new AtomicLong(0l);
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        final Optional<Map.Entry<String, Integer>> idEntry = headerMap
                .entrySet()
                .stream()
                .filter(e -> e.getKey().toLowerCase().matches("(row)?\\-?id"))
                .findFirst();

        if (idEntry.isEmpty()) {
            idTracker.set(Optional.ofNullable(repository.getMaxRowId()).orElse(0l));
        }

        records.parallelStream().forEach(r -> {
            Row row = new Row();
            row.setId(getRowId(idEntry.orElse(null), idTracker, r));
            row.setData(convertData(headerMap, r));
            save(row);

            totalCount.incrementAndGet();
            callProgressTracker(tracker, t -> t.fileLoadingUpdate(row, r.getRecordNumber()));
        });

        callProgressTracker(tracker, t -> t.fileLoadingFinished(totalCount.get()));
        callDataListeners();
    }

    private Map<String, Object> convertData(Map<String, Integer> headers, CSVRecord record) {
        Map<String, Object> data = new HashMap<>();

        headers.keySet().forEach(k -> {
            data.put(k, record.get(k));
        });

        return data;
    }

    private Long getRowId(Map.Entry<String, Integer> idEntry, AtomicLong idTracker, CSVRecord record) {
        if (idEntry == null) {
            return idTracker.incrementAndGet();
        }

        return Long.valueOf(record.get(idEntry.getKey()));
    }

    private void callProgressTracker(FileLoadProgressTracker tracker, Consumer<FileLoadProgressTracker> call) {
        if (tracker != null) {
            call.accept(tracker);
        }
    }

    public interface FileLoadProgressTracker {

        void fileLoadingStarted(String file, long recordCount);

        void fileLoadingUpdate(Row row, long recordNumber);

        void fileLoadingFinished(long recordTotal);
    }
}
