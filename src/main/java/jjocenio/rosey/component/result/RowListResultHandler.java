package jjocenio.rosey.component.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jline.builtins.RoseyEditor;
import jjocenio.rosey.persistence.Row;
import org.jline.builtins.Nano;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.shell.result.TerminalAwareResultHandler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RowListResultHandler extends TerminalAwareResultHandler<List<Row>> {

    private final Nano.SyntaxHighlighter jsonSyntaxHighlighter;
    private final File workingDirectory;

    @Autowired
    public RowListResultHandler(@Value("classpath:json.nanorc") Resource jsonNanorc, File workingDirectory) throws IOException {
        this.jsonSyntaxHighlighter = Nano.SyntaxHighlighter.build(String.valueOf(jsonNanorc.getURL()));
        this.workingDirectory = workingDirectory;
    }

    @Override
    protected void doHandleResult(List<Row> result) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            byte[] data = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(result);

            RoseyEditor viewer = new RoseyEditor(terminal, workingDirectory);
            viewer.addBuffer("rows", data, jsonSyntaxHighlighter);
            viewer.run();
        } catch (Exception e) {
            jsonSyntaxHighlighter.highlight(String.valueOf(result)).println(terminal);
        }
    }
}
