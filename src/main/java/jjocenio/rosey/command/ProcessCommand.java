package jjocenio.rosey.command;

import me.tongfei.progressbar.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
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

    private final ExecutorService executorService;

    @Autowired
    public ProcessCommand(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @ShellMethod(key = "process status", value = "shows the status of the current process")
    public String status() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
        int threads = executor.getActiveCount();

        if (threads == 0) {
            return STATUS_NOT_RUNNING;
        }

        long total = executor.getTaskCount();
        long completed = executor.getCompletedTaskCount();
        long queued = executor.getQueue().size();

        return String.format(STATUS_RUNNING_PATTERN, threads, total, completed, queued);
    }

    @ShellMethod(key = "process show-progress", value = "shows a progress bar")
    public void showProgress(@ShellOption(value = "--delay", defaultValue = "5000", help = "the update interval") long delay) {
        final AtomicBoolean running = new AtomicBoolean(true);
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;

        if (executor.getActiveCount() == 0) {
            System.out.println(STATUS_NOT_RUNNING);
        }

        System.out.println("Press Ctrl + C to stop\n");

        long total = executor.getTaskCount();
        final ProgressBar pb = new ProgressBar("Processing", total);

        try {
            Thread progressThread = new Thread(() -> {
                while (running.get() && executor.getActiveCount() > 0) {
                    pb.stepTo(executor.getCompletedTaskCount());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                    }
                }
            });
            progressThread.start();

            while (progressThread.isAlive()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
        } finally {
            pb.close();
            running.set(false);
        }
    }

    @ShellMethod(key = "process stop", value = "stops the process")
    public void stopProcess() throws InterruptedException {
        executorService.shutdownNow();
        while (!executorService.isShutdown()) {
            Thread.sleep(1000);
        }
    }
}
