package jjocenio.rosey.command;

import jjocenio.rosey.component.ExecutorServiceProvider;
import me.tongfei.progressbar.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ShellComponent
@ShellCommandGroup("process")
public class ProcessCommand extends BaseCommand {

    private static final String STATUS_RUNNING_PATTERN =
            "Running threads: %d\n" +
            "Total Tasks: %d\n" +
            "   - Completed: %d\n" +
            "   - Waiting: %d";

    private static final String STATUS_NOT_RUNNING = "There's no process running now";

    private final ExecutorServiceProvider executorServiceProvider;

    @Autowired
    public ProcessCommand(ExecutorServiceProvider executorServiceProvider) {
        this.executorServiceProvider = executorServiceProvider;
    }

    @ShellMethod(key = "process status", value = "shows the status of the current process")
    public String status() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorServiceProvider.getExecutorService();
        int threads = executor.getActiveCount();

        if (threads == 0) {
            return STATUS_NOT_RUNNING;
        }

        long total = executor.getTaskCount();
        long completed = executor.getCompletedTaskCount();
        long queued = executor.getQueue().size();

        return String.format(STATUS_RUNNING_PATTERN, threads, total, completed, queued);
    }

    @SuppressWarnings("java:S2142")
    @ShellMethod(key = "process show-progress", value = "shows a progress bar")
    public void showProgress(@ShellOption(value = "--delay", defaultValue = "5000", help = "the update interval") long delay) {
        final AtomicBoolean running = new AtomicBoolean(true);
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) executorServiceProvider.getExecutorService();

        if (executor.getActiveCount() == 0) {
            println(STATUS_NOT_RUNNING);
            return;
        }

        println("Press Ctrl + C to stop\n");

        long total = executor.getTaskCount();

        try (ProgressBar pb = new ProgressBar("Processing", total)) {
            Thread progressThread = new Thread(() -> {
                while (running.get() && executor.getActiveCount() > 0) {
                    pb.stepTo(executor.getCompletedTaskCount());
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ignored) {
                    }
                }

                pb.stepTo(executor.getCompletedTaskCount());
            });
            progressThread.start();

            while (progressThread.isAlive()) {
                TimeUnit.MILLISECONDS.sleep(500);
            }
        } catch (InterruptedException ignored) {
        } finally {
            running.set(false);
        }
    }

    @ShellMethod(key = "process stop", value = "stops the process")
    public void stopProcess() throws InterruptedException {
        ExecutorService executorService = executorServiceProvider.getExecutorService();
        executorService.shutdownNow();
        while (!executorService.isShutdown()) {
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }
}
