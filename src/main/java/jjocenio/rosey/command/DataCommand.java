package jjocenio.rosey.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.service.RowService;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.util.Map;

@ShellComponent
@ShellCommandGroup("data")
public class DataCommand extends BaseCommand {

    private final RowService service;

    @Autowired
    public DataCommand(RowService service) {
        this.service = service;
    }

    @ShellMethod(key = "data load", value = "loads data into internal database")
    public void load(@ShellOption(value = "--file", help = "the source file to load. It must be in a standard csv format") String file) throws IOException {
        final MutableLong totalRecords = new MutableLong(0l);
        final MutableObject<ProgressBar> pb = new MutableObject<>(null);

        RowService.FileLoadProgressTracker progressTracker = new RowService.FileLoadProgressTracker() {
            @Override
            public void fileLoadingStarted(String file, long recordCount) {
                pb.setValue(new ProgressBar("Loading", recordCount));
            }

            @Override
            public void fileLoadingUpdate(Row row, long recordNumber) {
                pb.getValue().step();
            }

            @Override
            public void fileLoadingFinished(long recordTotal) {
                totalRecords.setValue(recordTotal);
            }
        };

        try {
            service.loadFromFile(file, progressTracker);
        } finally {
            pb.getValue().close();
        }

        newWriterBuilder()
                .append(totalRecords.toString(), AttributedStyle.BOLD)
                .append(" records loaded from ", AttributedStyle.DEFAULT)
                .append(file, AttributedStyle.BOLD)
                .println();
    }

    @ShellMethod(key = "data count", value = "count the number of rows")
    public long count() {
        return service.count();
    }

    @ShellMethod(key = "data count-status", value = "count the number of rows")
    public Map<Row.Status, Long> countStatus() throws JsonProcessingException {
        return service.countGroupByStatus();
    }

    @ShellMethod(key = "data set-status", value = "sets the status for the row")
    public Row setStatus(@ShellOption(value = "--row-id", help = "the id of the row to change") long rowId,
                         @ShellOption(value = "--status", help = "the new status") Row.Status status) {
        service.setStatus(rowId, status);
        return service.findById(rowId).get();
    }

    @ShellMethod(key = "data delete", value = "deletes the row")
    public Row delete(@ShellOption(value = "--row-id", help = "the id of the row to delete") long rowId) {
        Row saved = service.findById(rowId).get();
        service.deleteById(rowId);
        return saved;
    }

    @ShellMethod(key = "data get", value = "gets the row")
    public Row get(@ShellOption(value = "--row-id", help = "the id of the row to retrieve") long rowId) {
        return service.findById(rowId).get();
    }

    @ShellMethod(key = "data clear", value = "removes all data")
    public void clear() throws IOException {
        newWriterBuilder()
                .append("This command will delete all data loaded. Are you sure? [y/n] ", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                .print();

        char response = readCharacter();
        if (response == 'y' || response == 'Y') {
            service.deleteAll();
            newWriterBuilder()
                    .append("All data has been deleted!", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                    .println();
        }
    }

    @ShellMethod(key = "data reset", value = "reset processing rows to pending")
    public void reset() {
        service.updateProcessingToPending();
    }
}
