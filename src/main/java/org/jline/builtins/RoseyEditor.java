package org.jline.builtins;

import org.jline.terminal.Terminal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class RoseyEditor extends Nano {

    public RoseyEditor(Terminal terminal, File workingDirectory) {
        super(terminal, workingDirectory.toPath());
        this.title = "Rosey Editor";
        this.printLineNumbers = true;
    }

    public void addBuffer(String name, byte[] data, SyntaxHighlighter syntaxHighlighter) {
        Buffer buffer = new ViewerBuffer(name, data, syntaxHighlighter);
        buffers.add(buffer);
    }

    class ViewerBuffer extends Nano.Buffer {
        byte[] data;

        ViewerBuffer(String name, byte[] data, SyntaxHighlighter syntaxHighlighter) {
            super(name);
            this.data = data;
            this.syntaxHighlighter = syntaxHighlighter;
        }

        @Override
        void open() throws IOException {
            open(new ByteArrayInputStream(data));
        }
    }
}