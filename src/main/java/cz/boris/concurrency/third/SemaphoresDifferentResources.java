package cz.boris.concurrency.third;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoresDifferentResources {

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
			System.out.println("*** Print job about to begin "
					+ Thread.currentThread().getName() + " ***");
			queue.printJob(new Object());
			System.out.println("*** Document printed by thread: "
					+ Thread.currentThread().getName() + " ***");
		}

	}

	static class PrintQueue {
		private final Semaphore semaphore;
		private boolean[] freePrinters;
		private Lock lockPrinter;

		/**
		 * Create 3 printers here and set up number of semaphores to 3.
		 */
		public PrintQueue() {
			semaphore = new Semaphore(3);
			freePrinters = new boolean[3];
			for (int i = 0; i < 3; i++) {
				freePrinters[i] = true;
			}
			lockPrinter = new ReentrantLock();
		}

		public void printJob(Object document) {
			try {
				semaphore.acquire();
				int assignedPrinter = getPrinter();
				long duration = 3000;
				System.out.println("Printing job during " + duration
						+ " Thread: " + Thread.currentThread().getName() + " Printer no. " + assignedPrinter);
				TimeUnit.SECONDS.sleep(duration / 1000);
				freePrinters[assignedPrinter] = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				semaphore.release();
			}
		}

		private int getPrinter() {
			int i = -1;
			try {
				lockPrinter.lock();
				for (int j = 0; j < freePrinters.length; j++) {
					if(freePrinters[j]) {
						i = j;
						freePrinters[j] = false;
						break;
					}
				}
			} finally {
				lockPrinter.unlock();
			}
			return i;
		}

	}

}
