package cz.boris.concurrency.third;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Semaphores {

	public static void main(String[] args) {
		PrintQueue queue = new PrintQueue();
		Thread[] t = new Thread[10];
		for (int i = 0; i < 10; i++) {
			t[i] = new Thread(new Job(queue), "Thread " + i);
		}
		for (int i = 0; i < 10; i++) {
			t[i].start();
		}
	}

	static class Job implements Runnable {

		private PrintQueue queue;

		public Job(PrintQueue queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			System.out.println("*** Print job about to begin " + Thread.currentThread().getName() +" ***");
			queue.printJob(new Object());
			System.out.println("*** Document printed by thread: " + Thread.currentThread().getName() + " ***");
		}

	}

	static class PrintQueue {
		private final Semaphore semaphore;

		public PrintQueue() {
			semaphore = new Semaphore(1);
		}

		public void printJob(Object document) {
			try {
				semaphore.acquire();
				long duration = 3000;
				System.out.println("Printing job during " + duration
						+ " Thread: " + Thread.currentThread().getName());
				TimeUnit.SECONDS.sleep(duration/1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				semaphore.release();
			}
		}

	}

}
