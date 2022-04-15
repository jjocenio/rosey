package jjocenio.rosey.component.result;

import jjocenio.rosey.persistence.Row;
import org.jline.builtins.Nano;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.shell.result.TerminalAwareResultHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RowResultHandler extends TerminalAwareResultHandler<Row> {

    private final Nano.SyntaxHighlighter jsonSyntaxHighlighter;

    public RowResultHandler(@Value("classpath:json.nanorc") Resource jsonNanorc) throws IOException {
        this.jsonSyntaxHighlighter = Nano.SyntaxHighlighter.build(String.valueOf(jsonNanorc.getURL()));
    }

    @Override
    protected void doHandleResult(Row result) {
        jsonSyntaxHighlighter.highlight(String.valueOf(result)).println(terminal);
    }
}
