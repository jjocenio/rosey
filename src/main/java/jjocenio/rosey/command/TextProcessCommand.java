package jjocenio.rosey.command;

import com.beust.jcommander.Parameter;
import freemarker.template.Template;
import jjocenio.rosey.component.TemplateHelper;
import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.service.OutputWriter;
import jjocenio.rosey.service.ProcessContext;
import jjocenio.rosey.service.text.TextProcessConfig;
import jjocenio.rosey.service.text.TextProcessContext;
import jjocenio.rosey.service.text.TextProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;

@ShellComponent
@ShellCommandGroup("process")
public class TextProcessCommand extends BaseCommand {

    private final TextProcessService processService;
    private final TemplateHelper templateHelper;

    @Autowired
    public TextProcessCommand(TextProcessService processService, TemplateHelper templateHelper) {
        this.processService = processService;
        this.templateHelper = templateHelper;
    }

    @ShellMethod(key = "process text all", value = "processes all pending rows")
    @ShellMethodAvailability("processCheckAvailability")
    public long runAll(@ShellOption(optOut = true) TextProcessAllArgs args) throws IOException {
        ProcessContext context = createContext(args);
        TextProcessConfig config = (TextProcessConfig) context.getConfig();

        config.setLimit(args.getLimit());
        config.setIncludeFailed(args.isIncludeFailed());

        return processService.processAll(context);
    }

    @ShellMethod(key = "process text", value = "process one single row")
    @ShellMethodAvailability("processCheckAvailability")
    public Row process(@ShellOption(optOut = true) TextProcessRowArgs args) throws IOException {
        ProcessContext context = createContext(args);
        return processService.processRow(args.getRowId(), context);
    }

    public Availability processCheckAvailability() {
        return processService.isRunning() ? Availability.unavailable("Process is already running!") : Availability.available();
    }

    private ProcessContext createContext(@NonNull TextProcessArgs args) throws IOException {
        OutputWriter outputWriter = null;
        if (args.getPath() != null) {
            Template path = templateHelper.getTemplate(args.getPath());
            outputWriter = new OutputWriter(templateHelper, path, args.isOverride(), args.isAppend());
        }

        TextProcessConfig config = new TextProcessConfig();
        config.setOutputTemplate(args.getOutputTemplate());

        return new TextProcessContext(config, outputWriter);
    }
}

class TextProcessArgs extends OutputArgs {

    @Parameter(names = "--output-template", description = "the template to apply to the row. Freemarker template is supported. Use '@' to with a relative path to specify a template file")
    private String outputTemplate;

    public String getOutputTemplate() {
        return outputTemplate;
    }

    public void setOutputTemplate(String outputTemplate) {
        this.outputTemplate = outputTemplate;
    }
}

class TextProcessAllArgs extends TextProcessArgs {

    @Parameter(names = "--include-failed", description = "re-process failed rows")
    private boolean includeFailed = false;

    @Parameter(names = "--limit", description = "limit the number of records to process")
    private long limit = -1;

    public boolean isIncludeFailed() {
        return includeFailed;
    }

    public void setIncludeFailed(boolean includeFailed) {
        this.includeFailed = includeFailed;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}

class TextProcessRowArgs extends TextProcessArgs {

    @Parameter(names = "--row-id", description = "the id of the row to process")
    private long rowId;

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }
}