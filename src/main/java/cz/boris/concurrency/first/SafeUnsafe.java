package cz.boris.concurrency.first;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import cz.boris.concurrency.first.SafeUnsafe.TaskFactory.Type;

public class SafeUnsafe {

	public static void main(String[] args) {
		/**
		 * Switch between Safe and Unsafe Task.
		 */
		
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(TaskFactory.getTask(Type.SAFE));
			thread.start();
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	static class TaskFactory {
		enum Type { SAFE, UNSAFE }
		private TaskFactory(){}
		public static Runnable getTask(Type type) {
			if(Type.SAFE.equals(type)) {
				return new SafeTask();
			} else if(Type.UNSAFE.equals(type)) {
				return new UnsafeTask();
			}
			return null;
		}
	}

	static class UnsafeTask implements Runnable {

		private Date start;

		@Override
		public void run() {
			start = new Date();
			System.out.printf("Starting Thread: %s : %s\n", Thread
					.currentThread().getId(), start);
			try {
				TimeUnit.SECONDS.sleep((int) Math.rint(Math.random() * 10));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Finished Thread: %s : %s\n", Thread
					.currentThread().getId(), start);
		}
	}

	static class SafeTask implements Runnable {
		
		/**
		 * We should be cautious here, if object in ThreadLocal is
		 * not a part of java standard library as Date for example
		 * we need to remember to clear ThreadLocal to prevent
		 * memory leak.
		 */
		private static ThreadLocal<Date> start = new ThreadLocal<Date>() {

			@Override
			protected Date initialValue() {
				return new Date();
			}
			
		};

		@Override
		public void run() {
			System.out.printf("Starting Thread: %s : %s\n", Thread
					.currentThread().getId(), start.get());
			try {
				TimeUnit.SECONDS.sleep((int) Math.rint(Math.random() * 10));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Finished Thread: %s : %s\n", Thread
					.currentThread().getId(), start.get());
		}
		
	}
}
