package backoff;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author Baoyi Chen
 */
public class ConstantBackoff implements Backoff {
    //
    private final int maxTimes;
    private final int interval;
    private final int maxElapsedTime;
    //
    private long st;
    private int times;

    public ConstantBackoff() {
        this(1000, 900000, 1);
    }

    public ConstantBackoff(int interval, int maxElapsedTime, int maxTimes) {
        this.interval = interval;
        this.maxTimes = maxTimes;
        this.maxElapsedTime = maxElapsedTime;
        reset();
    }

    @Override
    public void reset() {
        this.times = 0;
        this.st = System.nanoTime();
    }

    @Override
    public long next() {
        long ed = System.nanoTime();
        if (times >= maxTimes) return -1L;
        if (NANOSECONDS.toMillis(ed - st) > maxElapsedTime) return -1L;
        times++; return interval;
    }
}
