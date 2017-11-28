/*
 * Copyright 2016 leon chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util;

import java.util.Arrays;

/**
 * @author Leon Chen
 */
public class SuffixArray {
    public static void main(String[] args) {
        System.out.println(lcp("banana"));
    }

    private static String lcp(String str) {
        SuffixItem[] sa = sa(str);
        Arrays.sort(sa);
        String c = "";
        for (int i = 1; i < sa.length; i++) {
            String mc = sa[i - 1].common(sa[i]);
            if (mc.length() > c.length()) c = mc;
        }
        return c;
    }

    private static SuffixItem[] sa(String str) {
        SuffixItem[] sa = new SuffixItem[str.length()];
        char[] ary = str.toCharArray();
        for (int i = 0; i < ary.length; i++) {
            sa[i] = new SuffixItem(i, ary);
        }
        return sa;
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
