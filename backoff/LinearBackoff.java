package backoff;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author Baoyi Chen
 */
public class LinearBackoff implements Backoff {
    //
    private final int maxTimes;
    private final int interval;
    private final int maxInterval;
    private final int maxElapsedTime;
    //
    private long st;
    private int times;
    private int current;

    public LinearBackoff() {
        this(1000, 60000, 900000, 1);
    }

    public LinearBackoff(int interval, int maxInterval, int maxElapsedTime, int maxTimes) {
        this.maxTimes = maxTimes;
        this.interval = interval;
        this.maxInterval = maxInterval;
        this.maxElapsedTime = maxElapsedTime;
        reset();
    }

    @Override
    public void reset() {
        times = 0;
        current = 0;
        st = System.nanoTime();
    }

    @Override
    public long next() {
        long ed = System.nanoTime();
        if (times >= maxTimes) return -1L;
        if (NANOSECONDS.toMillis(ed - st) > maxElapsedTime) return -1L;
        current += interval; if (current > maxInterval) current = maxInterval;
        times++; return current;
    }

}
