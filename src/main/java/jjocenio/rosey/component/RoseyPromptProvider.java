package jjocenio.rosey.component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.service.DataListener;
import jjocenio.rosey.service.RowService;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class RoseyPromptProvider implements PromptProvider, DataListener {

    private final ExecutorServiceProvider executorServiceProvider;

    private final LoadingCache<Integer, Map<Row.Status, Long>> countCache;


    @Autowired
    public RoseyPromptProvider(RowService rowService, ExecutorServiceProvider executorServiceProvider) {
        this.executorServiceProvider = executorServiceProvider;

        countCache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {

                    @Override
                    public Map<Row.Status, Long> load(Integer key) {
                        return rowService.countGroupByStatus();
                    }
                });
    }

    @Override
    @SuppressWarnings("java:S106")
    public AttributedString getPrompt() {
        AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
        attributedStringBuilder.append(createSimpleString("rosey "));
        Map<Row.Status, Long> count = null;

        try {
            count = countCache.get(0);
        } catch (ExecutionException e) {
            System.err.println("Error retrieving data for status");
        }

        if (count != null || !count.isEmpty()) {
            count.entrySet().forEach(rc -> {
                try {
                    StatusColors statusColor = StatusColors.valueOf(rc.getKey().name());
                    attributedStringBuilder.append(createStringWithColor(String.format(" %d ", rc.getValue()), statusColor.foreground, statusColor.background));
                } catch (IllegalArgumentException ignored) {
                    // Ignored status
                }
            });
        }

        attributedStringBuilder.append(createSimpleString(" :> "));
        return attributedStringBuilder.toAttributedString();
    }

    @Override
    public void dataStatusChanged(Map<Row.Status, Long> count) {
        if (count != null) {
            countCache.put(0, count);
        }
    }

    private AttributedString createSimpleString(String sentence) {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorServiceProvider.getExecutorService();
        int color = threadPoolExecutor.getActiveCount() > 0 ? AttributedStyle.YELLOW : AttributedStyle.GREEN;
        return new AttributedString(sentence, AttributedStyle.DEFAULT.foreground(color));
    }

    private AttributedString createStringWithColor(String sentence, int foreground, int background) {
        return new AttributedString(sentence, AttributedStyle.DEFAULT.foreground(foreground).background(background));
    }

    private enum StatusColors {
        PENDING(AttributedStyle.BLACK, AttributedStyle.WHITE),
        PROCESSED(AttributedStyle.WHITE, AttributedStyle.GREEN),
        FAILED(AttributedStyle.WHITE, AttributedStyle.RED);

        private final int foreground;
        private final int background;

        StatusColors(int foreground, int background) {
            this.foreground = foreground;
            this.background = background;
        }
    }
}
