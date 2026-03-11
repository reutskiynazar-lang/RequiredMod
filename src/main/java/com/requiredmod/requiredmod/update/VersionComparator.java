package com.requiredmod.requiredmod.update;

import java.util.Arrays;

public final class VersionComparator {
    private VersionComparator() {
    }

    public static boolean isNewer(String current, String remote) {
        return compare(current, remote) < 0;
    }

    public static int compare(String left, String right) {
        int[] lv = split(left);
        int[] rv = split(right);
        int max = Math.max(lv.length, rv.length);
        for (int i = 0; i < max; i++) {
            int l = i < lv.length ? lv[i] : 0;
            int r = i < rv.length ? rv[i] : 0;
            if (l != r) {
                return Integer.compare(l, r);
            }
        }
        return 0;
    }

    private static int[] split(String value) {
        return Arrays.stream(value.split("[.-]"))
                .mapToInt(part -> part.chars().allMatch(Character::isDigit) ? Integer.parseInt(part) : 0)
                .toArray();
    }
}
