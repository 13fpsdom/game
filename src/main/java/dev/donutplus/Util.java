package dev.donutplus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class Util {
    private static final Map<String, Pattern> CACHE = new HashMap<>();

    /** Returns null if the regex is invalid. */
    public static Pattern pattern(String regex) {
        try { return CACHE.computeIfAbsent(regex, Pattern::compile); }
        catch (Exception e) { return null; }
    }

    /** Parses 1500, 1.5K, 2m, 3B, $4,000 ... returns 0 on failure. */
    public static double parse(String s) {
        try {
            s = s.replace(",", "").replace("$", "").trim();
            if (s.isEmpty()) return 0;
            char last = Character.toUpperCase(s.charAt(s.length() - 1));
            double mult = 1;
            if (last == 'K') mult = 1e3; else if (last == 'M') mult = 1e6; else if (last == 'B') mult = 1e9;
            if (mult != 1) s = s.substring(0, s.length() - 1);
            return Double.parseDouble(s) * mult;
        } catch (Exception e) { return 0; }
    }

    public static String fmt(double v) {
        double a = Math.abs(v);
        if (a >= 1e9) return String.format("$%.2fB", v / 1e9);
        if (a >= 1e6) return String.format("$%.2fM", v / 1e6);
        if (a >= 1e3) return String.format("$%.2fK", v / 1e3);
        return String.format("$%.0f", v);
    }

    public static String clock(long ms) {
        long s = Math.max(0, ms / 1000);
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
