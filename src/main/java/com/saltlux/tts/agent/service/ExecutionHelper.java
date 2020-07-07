package com.saltlux.tts.agent.service;


import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.util.NamedThreadFactory;
import org.apache.lucene.util.ThreadInterruptedException;

public class ExecutionHelper<T> implements Iterator<T>, Iterable<T> {
	private final CompletionService<T> service;
	private int numTasks;

	public ExecutionHelper(final Executor executor) {
		this.service = new ExecutorCompletionService<T>(executor);
	}

	@Override
	public boolean hasNext() {
		return numTasks > 0;
	}

	public void submit(Callable<T> task) {
		this.service.submit(task);
		++numTasks;
	}

	@Override
	public T next() {
		if (!this.hasNext())
			throw new NoSuchElementException(
					"next() is called but hasNext() returned false");
		try {
			return service.take().get();
		}
		catch (InterruptedException e) {
			throw new ThreadInterruptedException(e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		finally {
			--numTasks;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		// use the shortcut here - this is only used in a private context
		return this;
	}
	
	public static ExecutorService CreateExcutor(int PoolSize, String FactoryName) {
		ExecutorService executor = new ThreadPoolExecutor(PoolSize, PoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory(FactoryName));
		
		return executor;
	}
	
	public static void shutdownExecutorService(ExecutorService ex) {
		if (ex != null) {
			try {
				ex.shutdown();
				ex.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// Just report it on the syserr.
				System.err.println("Could not properly shutdown executor service.");
				e.printStackTrace(System.err);
			}
		}
	}
}