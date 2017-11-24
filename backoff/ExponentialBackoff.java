package backoff;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author Baoyi Chen
 * @see <a href="https://en.wikipedia.org/wiki/Exponential_backoff">exponential backoff</a>
 * @see <a href="https://github.com/google/google-http-java-client/blob/master/google-http-client/src/main/java/com/google/api/client/util/ExponentialBackOff.java">ExponentialBackOff</a>
 */
public class ExponentialBackoff implements Backoff {
    //
    private final int maxTimes;
    private final double factor;
    private final int initialize;
    private final int maxInterval;
    private final double multiplier;
    private final int maxElapsedTime;
    //
    private long st;
    private int times;
    private int interval;

    public ExponentialBackoff() {
        this(1000, 60000, 900000, 1);
    }

    public ExponentialBackoff(int initialize, int maxInterval, int maxElapsedTime, int maxTimes) {
        this(initialize, maxInterval, maxElapsedTime, maxTimes, 0.2, 1.5);
    }

    public ExponentialBackoff(int initialize, int maxInterval, int maxElapsedTime, int maxTimes, double factor, double multiplier) {
        this.factor = factor;
        this.maxTimes = maxTimes;
        this.multiplier = multiplier;
        this.initialize = initialize;
        this.maxInterval = maxInterval;
        this.maxElapsedTime = maxElapsedTime;
        reset();
    }

    @Override
    public void reset() {
        times = 0;
        interval = initialize;
        st = System.nanoTime();
    }

    /**
     * request#     retry_interval     randomized_interval
     * 1             0.5                [0.25,   0.75]
     * 2             0.75               [0.375,  1.125]
     * 3             1.125              [0.562,  1.687]
     * 4             1.687              [0.8435, 2.53]
     * 5             2.53               [1.265,  3.795]
     * 6             3.795              [1.897,  5.692]
     * 7             5.692              [2.846,  8.538]
     * 8             8.538              [4.269, 12.807]
     * 9            12.807              [6.403, 19.210]
     * 10           19.210              [19.210, -1]
     *
     * @return next trigger millis.
     */
    @Override
    public long next() {
        long ed = System.nanoTime();
        if (times >= maxTimes) return -1L;
        if (NANOSECONDS.toMillis(ed - st) > maxElapsedTime) return -1L;
        final double delta = factor * interval, min = interval - delta;
        final int random = (int) (min + (Math.random() * (2 * delta + 1)));
        if (interval > maxInterval / multiplier) interval = maxInterval; else interval *= multiplier;
        times++; return random;
    }

}
