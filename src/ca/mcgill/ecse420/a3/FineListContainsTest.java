package ca.mcgill.ecse420.a3;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class FineListContainsTest {

    public static void main(String[] args) throws Exception {
        FineList<Integer> list = new FineList<>();
        final int N = 500;
        AtomicBoolean failed = new AtomicBoolean(false);

        // Keys with multiples of 3 are added and never removed
        for (int i = 1; i <= N; i++) {
            list.add(i * 3);
        }

        ExecutorService pool = Executors.newFixedThreadPool(8); // testing with 8 threads, 4 writers and 4 readers
        for (int w = 0; w < 4; w++) {
            pool.submit(() -> {
                for (int r = 0; r < 200; r++)
                    for (int i = 0; i < N; i++) {
                        // keys with indices 3i + 1  are constantly added and removed by the writer threads
                        list.add(i * 3 + 1);
                        list.remove(i * 3 + 1);
                    }
            });
        }
        for (int rd = 0; rd < 4; rd++) {         // 4 readers
            pool.submit(() -> {
                for (int r = 0; r < 200; r++)
                    for (int i = 1; i <= N; i++) {
                        if (!list.contains(i * 3)){
                            failed.set(true); // must be found
                        }
                        if (list.contains(i * 3 + 2)){ // keys with indices 3i + 2 were never written so they must not be there
                            failed.set(true); // must be absent
                        }
                    }
            });
        }
        pool.shutdown();
        boolean finished = pool.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println(finished ? "No deadlock" : "DEADLOCK/hang");
        System.out.println(failed.get() ? "Test failed..." : "Test passed!");
    }
}
