package jjocenio.rosey.command;

import com.beust.jcommander.Parameter;

public class OutputArgs {

    @Parameter(names = "--output-path", description = "the path for the output file. variable substitution is supported")
    private String path;

    @Parameter(names = "--output-override", description = "force the output file to be replaced if exists")
    private boolean override;

    @Parameter(names = "--output-append", description = "force the data to be appended to the file. use this if all output goes to the same file")
    private boolean append;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }
}
