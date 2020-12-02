package jjocenio.rosey.service;

public abstract class ProcessConfig {

    private boolean includeFailed = false;
    private long limit = 0;

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
