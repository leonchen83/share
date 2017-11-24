package backoff;

/**
 * @author Baoyi Chen
 */
public interface Backoff {

    void reset();

    long next();
}
