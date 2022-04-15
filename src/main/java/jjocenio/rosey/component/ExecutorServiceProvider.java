package jjocenio.rosey.component;

import jjocenio.rosey.ApplicationExitRequestEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExecutorServiceProvider {

    private final int threads;
    private final ShutdownHook shutdownHook;
    private final MutableObject<ExecutorService> executorServiceRef = new MutableObject<>(null);

    public ExecutorServiceProvider(@Value("${batch.threads:10}") int threads) {
        this.threads = threads;
        this.shutdownHook = new ShutdownHook(this.executorServiceRef);
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
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

    @EventListener
    public void handleContextStopped(ApplicationExitRequestEvent event) {
        this.shutdownHook.run();
    }
}

class ShutdownHook extends Thread {
    private final MutableObject<ExecutorService> executorServiceRef;

    ShutdownHook(MutableObject<ExecutorService> executorServiceRef) {
        this.executorServiceRef = executorServiceRef;
    }

    @Override
    public void run() {
        if (executorServiceRef.getValue() != null) {
            executorServiceRef.getValue().shutdownNow();
        }
    }
}
