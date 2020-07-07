package com.saltlux.tts;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/*
 * ConcurrentQueue의 capacity 값에 따른 Throughput에 대한 실험
 */
public class QueueTest {
    static ExecutorService e = Executors.newFixedThreadPool(2);
    static int N = 1000000;

    public static void main(String[] args) throws Exception {    
        for (int i = 7; i < 21; i++) {
            int length = (i == 0) ? 1 : i * 5;
            System.out.print(length + "\t");
            System.out.print(doTest(new LinkedBlockingQueue<Integer>(length), N, "linked " + length) + "\t");
            System.out.print(doTest(new ArrayBlockingQueue<Integer>(length), N, "array " + length) + "\t");
            System.out.print(doTest(new SynchronousQueue<Integer>(), N, "synchro " + length));
            System.out.println();
        }

        e.shutdown();
    }

    private static long doTest(final BlockingQueue<Integer> q, final int n, String name) throws Exception {
        long t = System.nanoTime();
        System.out.print(name + "\t");
        e.submit(new Runnable() {
            public void run() {
                for (int i = 0; i < n; i++)
                    try { q.put(i); } catch (InterruptedException ex) {}
            }
        });    

        Long r = e.submit(new Callable<Long>() {
            public Long call() {
                long sum = 0;
                for (int i = 0; i < n; i++)
                    try { sum += q.take(); } catch (InterruptedException ex) {}
                return sum;
            }
        }).get();
        t = System.nanoTime() - t;

        return (long)(1000000000.0 * N / t); // Throughput, items/sec
    }
}    