import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Leon Chen
 * @since 1.0.0
 */
public class SuffixArray {
    public static void main(String[] args) {
        System.out.println(lcp("banana"));
    }

    private static String lcp(String str) {
        List<SuffixItem> sa = sa(str);
        Collections.sort(sa);
        String c = "";
        for (int i = 1; i < sa.size(); i++) {
            String mc = sa.get(i - 1).common(sa.get(i));
            if (mc.length() > c.length()) c = mc;
        }
        return c;
    }

    private static List<SuffixItem> sa(String str) {
        List<SuffixItem> list = new ArrayList<>(str.length());
        char[] ary = str.toCharArray();
        for (int i = 0; i < ary.length; i++) {
            list.add(new SuffixItem(i, ary));
        }
        return list;
    }

    private static class SuffixItem implements Comparable<SuffixItem> {
        private final int idx;
        private final char[] ary;

        public SuffixItem(final int idx, final char[] ary) {
            this.idx = idx;
            this.ary = ary;
        }

        @Override
        public int compareTo(SuffixItem that) {
            int i = this.idx, j = that.idx;
            int min = Math.min(this.ary.length - i, that.ary.length - j);
            for (int k = 0; k < min; k++, i++, j++) {
                if (this.ary[i] > that.ary[j]) {
                    return 1;
                } else if (this.ary[i] < that.ary[j]) {
                    return -1;
                }
            }
            return (this.ary.length - i) - (that.ary.length - j);
        }

        public String common(SuffixItem that) {
            int i = this.idx, j = that.idx;
            int min = Math.min(this.ary.length - i, that.ary.length - j);
            for (int k = 0; k < min && this.ary[i++] == that.ary[j++]; k++) ;
            return new String(ary, this.idx, i - this.idx);
        }

        @Override
        public String toString() {
            return new String(ary, idx, ary.length - idx);
        }
    }
}
