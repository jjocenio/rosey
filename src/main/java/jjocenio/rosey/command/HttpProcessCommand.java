package jjocenio.rosey.command;

import com.beust.jcommander.Parameter;
import freemarker.template.Template;
import jjocenio.rosey.component.TemplateHelper;
import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.service.OutputWriter;
import jjocenio.rosey.service.ProcessContext;
import jjocenio.rosey.service.http.HttpProcessConfig;
import jjocenio.rosey.service.http.HttpProcessContext;
import jjocenio.rosey.service.http.HttpProcessService;
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
public class HttpProcessCommand extends BaseCommand {

    private final HttpProcessService processService;
    private final TemplateHelper templateHelper;

    @Autowired
    public HttpProcessCommand(HttpProcessService processService, TemplateHelper templateHelper) {
        this.processService = processService;
        this.templateHelper = templateHelper;
    }

    @ShellMethod(key = "process http all", value = "processes all pending rows")
    @ShellMethodAvailability("processCheckAvailability")
    public long runAll(@ShellOption(optOut = true) HttpProcessAllArgs args) throws IOException {
        ProcessContext context = createContext(args);
        HttpProcessConfig config = (HttpProcessConfig) context.getConfig();

        config.setLimit(args.getLimit());
        config.setIncludeFailed(args.isIncludeFailed());
        config.setWait(args.getWait());

        return processService.processAll(context);
    }

    @ShellMethod(key = "process http", value = "process one single row")
    @ShellMethodAvailability("processCheckAvailability")
    public Row process(@ShellOption(optOut = true) HttpProcessRowArgs args) throws IOException {
        ProcessContext context = createContext(args);
        return processService.processRow(args.getRowId(), context);
    }

    public Availability processCheckAvailability() {
        return processService.isRunning() ? Availability.unavailable("Process is already running!") : Availability.available();
    }

    private ProcessContext createContext(@NonNull HttpProcessArgs args) throws IOException {
        OutputWriter outputWriter = null;
        if (args.getPath() != null) {
            Template path = templateHelper.getTemplate(args.getPath());
            outputWriter = new OutputWriter(templateHelper, path, args.isOverride(), args.isAppend());
        }

        HttpProcessConfig config = new HttpProcessConfig();

        config.setUrl(args.getUrl());
        config.setHttpMethod(args.getHttpMethod());
        config.setBody(args.getBody());
        config.setHeaders(args.getHeaders());

        return new HttpProcessContext(config, outputWriter);
    }
}

class HttpProcessArgs extends OutputArgs {

    @Parameter(names = "--url", description = "the target endpoint to send the request. Freemarker template is supported", required = true)
    private String url;

    @Parameter(names = "--http-method", description = "the http method to use", required = true)
    private HttpProcessConfig.HttpMethod httpMethod;

    @Parameter(names = "--body", description = "the request body. Freemarker template is supported. Use '@' to use a file instead of inline template")
    private String body;

    @Parameter(names = "--headers", description = "the request headers. Freemarker template is supported. Use '@' to use a file instead of inline template")
    private String headers;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpProcessConfig.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpProcessConfig.HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }
}

class HttpProcessAllArgs extends HttpProcessArgs {

    @Parameter(names = "--include-failed", description = "re-process failed rows")
    private boolean includeFailed = false;

    @Parameter(names = "--limit", description = "limit the number of records to process")
    private long limit = -1;

    @Parameter(names = "--wait", description = "Time in seconds to wait between rows")
    private long wait = -1;

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

    public long getWait() {
        return wait;
    }

    public void setWait(long wait) {
        this.wait = wait;
    }
}

class HttpProcessRowArgs extends HttpProcessArgs {

    @Parameter(names = "--row-id", description = "the id of the row to process")
    private long rowId;

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }
}