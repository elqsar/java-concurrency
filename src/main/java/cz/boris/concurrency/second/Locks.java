package cz.boris.concurrency.second;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Locks {

	public static void main(String[] args) {
		PrintQueue queue = new PrintQueue();
		Thread[] threads = new Thread[10];
		for (int i = 0; i < 10; i++) {
			threads[i] = new Thread(new Job(queue), "Thread no. " + i);
		}
		for (int i = 0; i < 10; i++) {
			threads[i].start();
		}
	}

	static class PrintQueue {

		private static final int DEFAULT_INTERVAL = 10000;
		/**
		 * Ensure that only one thread at the time execute a job.
		 * 1. Thread A get the lock and execute the job.
		 * 2. Thread B try get the lock, but A has it, and B puts to sleep.
		 * 3. unlock() notify others so they can continue.
		 */
		private final Lock queueLock = new ReentrantLock();

		public void printJob(Object document) {
			queueLock.lock();
			try {
				Long duration = (long) (Math.random() * DEFAULT_INTERVAL);
				System.out.println(Thread.currentThread().getName()
						+ ": PrintQueue: Printing job during " + duration
						/ 1000 + " seconds");
				Thread.sleep(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				queueLock.unlock();
			}
		}

	}

	static class Job implements Runnable {

		private static final String PRINTED = "%s: The document has been printed.\n";
		private static final String TO_PRINT = "%s: Going to print a document\n";
		
		private PrintQueue queue;

		public Job(PrintQueue queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			System.out.printf(TO_PRINT, Thread
					.currentThread().getName());
			queue.printJob(new Object());
			System.out.printf(PRINTED, Thread
					.currentThread().getName());
		}
	}
}
