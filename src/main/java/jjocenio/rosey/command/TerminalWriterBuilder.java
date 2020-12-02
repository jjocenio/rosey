package jjocenio.rosey.command;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class TerminalWriterBuilder {

    private AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
    private final Terminal terminal;

    public TerminalWriterBuilder(Terminal terminal) {
        this.terminal = terminal;
    }

    public TerminalWriterBuilder append(String text) {
        attributedStringBuilder.append(text);
        return this;
    }

    public TerminalWriterBuilder append(String text, AttributedStyle style) {
        attributedStringBuilder.append(text, style);
        return this;
    }

    public void print() {
        print(false);
    }

    public void println() {
        print(true);
    }

    private void print(boolean newLine) {
        String formattedText = attributedStringBuilder.toAnsi();
        if (newLine) {
            terminal.writer().println(formattedText);
        } else {
            terminal.writer().print(formattedText);
        }

        terminal.writer().flush();
    }
}