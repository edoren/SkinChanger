package me.edoren.skin_changer.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class SharedPool {
    private static final ExecutorService pool = Executors.newWorkStealingPool(ForkJoinPool.getCommonPoolParallelism() + 1);

    public static void execute(Runnable command) {
        pool.execute(command);
    }

    public static ExecutorService get() {
        return pool;
    }

    private SharedPool() {
    }
}
