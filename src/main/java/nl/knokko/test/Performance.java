package nl.knokko.test;

import java.util.LinkedList;
import java.util.List;

public class Performance {

    private static String current;

    private static long startTime;

    private static List<Entry> entries = new LinkedList<>();

    private static boolean disabled;

    public static void disable() {
        disabled = true;
    }

    public static void next(String description) {
        if (disabled) return;
        long time = System.nanoTime();
        if (current != null)
            entries.add(new Entry(current, time - startTime));
        current = description;
        startTime = time;
    }

    public static void print(EntryFilter filter) {
        if (disabled) {
            System.out.println("Performance has been disabled");
            return;
        }
        end();
        entries.sort(null);
        System.out.println();
        System.out.println("Performance results:");
        System.out.println();
        for (Entry entry : entries)
            if (filter.accept(entry.description, entry.duration))
                entry.print();
        System.out.println();
    }

    public static interface EntryFilter {

        boolean accept(String description, long duration);
    }

    public static void end() {
        if (!disabled && current != null) {
            entries.add(new Entry(current, System.nanoTime() - startTime));
            current = null;
        }
    }

    private static class Entry implements Comparable<Entry> {

        String description;
        long duration;

        Entry(String description, long duration){
            this.description = description;
            this.duration = duration;
        }

        void print() {
            System.out.println(description + " took " + (duration / 1000000.0) + " ms");
        }

        @Override
        public int compareTo(Entry other) {
            if (duration > other.duration)
                return 1;
            if (duration < other.duration)
                return -1;
            return 0;
        }
    }
}