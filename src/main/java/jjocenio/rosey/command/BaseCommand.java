package jjocenio.rosey.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.io.Reader;

public abstract class BaseCommand {

    protected Terminal terminal;

    @Autowired
    @Lazy
    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    protected String convertToJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    protected TerminalWriterBuilder newWriterBuilder() {
        return new TerminalWriterBuilder(terminal);
    }

    protected char readCharacter() throws IOException {
        try (Terminal tmpTerminal = TerminalBuilder.builder()
                .jna(true)
                .system(true)
                .build()) {

            tmpTerminal.enterRawMode();
            try (Reader tmpReader = tmpTerminal.reader()) {
                char response = (char) tmpReader.read();
                tmpTerminal.writer().println();
                tmpTerminal.flush();

                return response;
            }
        }
    }

    @SuppressWarnings("java:S106")
    protected void println(String message) {
        System.out.println(message);
    }
}
