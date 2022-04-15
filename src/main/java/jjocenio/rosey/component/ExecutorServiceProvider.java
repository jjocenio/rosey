package jjocenio.rosey.component;

import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExecutorServiceProvider {

    private final int threads;
    private final MutableObject<ExecutorService> executorServiceRef = new MutableObject<>(null);

    public ExecutorServiceProvider(@Value("${batch.threads:10}") int threads) {
        this.threads = threads;
    }

    public ExecutorService getCurrentExecutorService() {
        return executorServiceRef.getValue();
    }

    public ExecutorService getExecutorService() {
        if (executorServiceRef.getValue() == null || executorServiceRef.getValue().isTerminated()) {
            executorServiceRef.setValue(Executors.newFixedThreadPool(threads));
        }

        return executorServiceRef.getValue();
    }
}

class ShutdownHook extends Thread {
    private MutableObject<ExecutorService> executorServiceRef;

    @Override
    public void run() {
        if (executorServiceRef.getValue() != null) {
            executorServiceRef.getValue().shutdownNow();
        }
    }
}
