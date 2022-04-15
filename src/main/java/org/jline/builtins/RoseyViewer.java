package org.jline.builtins;

import org.jline.terminal.Terminal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RoseyViewer extends Less {

    public RoseyViewer(Terminal terminal) {
        super(terminal, Paths.get(System.getProperty("user.home") + File.separator + ".rosey"));
    }

    public void view(String name, byte[] data) throws IOException, InterruptedException {
        Source.InputStreamSource source = new Source.InputStreamSource(new ByteArrayInputStream(data), true, name);
        List<Source> sources = new ArrayList<>();
        sources.add(source);

        run(sources);
    }
}
