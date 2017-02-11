import java.util.Arrays;

/**
 * Created by Baoyi Chen on 2017/2/10.
 */
public class CountingBitSort {
    private int[] count;
    private final long[] ary;
    private static final int BITS = 6;
    private static final int MASK = 1 << 6;

    public CountingBitSort(int maxNum) {
        int x = maxNum >> BITS;
        int y = maxNum & (MASK - 1);
        if (y > 0) x = x + 1;
        this.ary = new long[x];
    }

    /**
     * @param num 0->Integer.MAX_VALUE
     */
    public void sort(int num) {
        int x = num >> BITS;
        int y = num & (MASK - 1);
        ary[x] |= (1L << y);
    }

    /**
     * @param num 0->Integer.MAX_VALUE
     */
    public boolean contains(int num) {
        int x = num >> BITS;
        int y = num & (MASK - 1);
        return ((ary[x] >>> y) & 1) != 0;
    }

    /**
     * @param k
     * @return
     */
    public int[] top(int k) {
        int[] r = new int[k];
        int ki = 0;
        for (int i = count.length - 1; i >= 1; i--) {
            if (count[i] - count[i - 1] > 0) {
                int base = (i - 1) << BITS;
                for (int j = MASK - 1; j >= 0; j--) {
                    if (((ary[i - 1] >>> j) & 1) != 0 && ki < k) r[ki++] = base + j;
                }
            }
        }
        if (ki != k) return Arrays.copyOf(r, ki);
        return r;
    }

    /**
     * getIndex和top k之前的预处理
     */
    public void preProcessing() {
        this.count = new int[ary.length + 1];
        int total = 0;
        for (int idx = 1; idx < count.length; idx++) {
            total += Long.bitCount(ary[idx - 1]);
            count[idx] = total;
        }
    }

    /**
     * @param num 0->Integer.MAX_VALUE
     */
    public int getIndex(int num) {
        int x = num >> BITS;
        int y = num & (MASK - 1);
        if (((ary[x] >>> y) & 1) == 0) return -1;
        if (y == 0) return count[x];
        return count[x] + Long.bitCount(ary[x] << (MASK - y) >>> (MASK - y));
    }

    public String toString() {
        return Arrays.toString(ary);
    }

    public static void main(String[] args) {
        long et, st = System.currentTimeMillis();
        CountingBitSort cb = new CountingBitSort(Integer.MAX_VALUE);
        for (int i = Integer.MAX_VALUE; i >= 0; i--) cb.sort(i);
        et = System.currentTimeMillis();
        System.out.println("sort:" + (et - st) / 1000d);

        cb.preProcessing();

        st = System.currentTimeMillis();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (i != cb.getIndex(i)) System.out.println("error" + i);
        }
        et = System.currentTimeMillis();
        System.out.println("sort checking:" + (et - st) / 1000d);

        st = System.currentTimeMillis();
        int[] r = cb.top(65);
        for (int i : r) System.out.println(i);
        et = System.currentTimeMillis();
        System.out.println("top k:" + (et - st) / 1000d);
    }
}
