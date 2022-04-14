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
import org.springframework.shell.table.BeanListTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

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

    @ShellMethod(key = "data reset", value = "resets processing rows to pending")
    public void reset() {
        service.updateProcessingToPending();
    }

    @ShellMethod(key = "data show-failed", value = "presents a list of failed rows if any")
    public void showFailed(@ShellOption(value = "--limit", help = "the limit f rows to show", defaultValue = "50") long limit,
                           @ShellOption(value = "--width", help = "the width of the table in number characters. if <= 0, it defaults to terminal width", defaultValue = "-1") int width) {
        List<Row> rowsFailed = service.findAllByStatus(Row.Status.FAILED).stream().limit(limit).collect(toList());

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("id", "Id");
        headers.put("status", "Status");
        headers.put("lastUpdate", "Last Update");
        headers.put("resultDetail", "Result Detail");

        TableModel tableModel = new BeanListTableModel<>(rowsFailed, headers);

        TableBuilder tableBuilder = new TableBuilder(tableModel);
        tableBuilder.addInnerBorder(BorderStyle.fancy_light);
        tableBuilder.addHeaderBorder(BorderStyle.fancy_double);

        terminal.writer().println(tableBuilder.build().render(width <= 0 ? terminal.getWidth() : width));
        terminal.flush();
    }
}
