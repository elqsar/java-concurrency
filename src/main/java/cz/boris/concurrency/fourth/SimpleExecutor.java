package cz.boris.concurrency.fourth;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleExecutor {

	enum Mode {
		FIXED(5), CACHED(0);
		private int number;

		private Mode(int number) {
			this.number = number;
		}

		public int getNumber() {
			return number;
		}
	}

	public static void main(String[] args) {
		Server server = new Server(Mode.FIXED);
		for (int i = 0; i < 100; i++) {
			Task task = new Task("Task " + i);
			server.execute(task);
		}
		server.shutDown();
	}

	static class Server {

		private ThreadPoolExecutor executor;

		/**
		 * Cached Thread Pool is most suitable for reasonable number of short
		 * living threads.
		 */
		public Server(Mode mode) {
			executor = (ThreadPoolExecutor) (Mode.CACHED.equals(mode) ? (ThreadPoolExecutor) Executors
					.newCachedThreadPool() : Executors.newFixedThreadPool(mode
					.getNumber()));
		}

		public void execute(Runnable task) {
			System.out.println("*** New task arrived ***");
			executor.execute(task);
			System.out
					.printf("Server: Pool Size: %d\n", executor.getPoolSize());
			System.out.printf("Server: Task Count: %d\n",
					executor.getTaskCount());
			System.out.printf("Server: Active Count: %d\n",
					executor.getActiveCount());
			System.out.printf("Server: Completed Tasks: %d\n",
					executor.getCompletedTaskCount());
		}

		/**
		 * You should remember to finish executor explicitly. Otherwise it will
		 * be waiting forever and because active non-daemon Thread your program
		 * never end.
		 */
		public void shutDown() {
			executor.shutdown();
		}
	}

	static class Task implements Runnable {

		private Date initDate;
		private String name;

		public Task(String name) {
			this.initDate = new Date();
			this.name = name;
		}

		@Override
		public void run() {
			System.out.println(new StringBuilder()
					.append(Thread.currentThread().getName()).append(" Task: ")
					.append(name).append(" Created on: ").append(initDate)
					.toString());
			System.out.println(new StringBuilder()
					.append(Thread.currentThread().getName()).append(" Task: ")
					.append(name).append(" Created on: ").append(new Date())
					.toString());
			try {
				long duration = (long) (Math.random() * 10);
				System.out.println(new StringBuilder()
						.append(Thread.currentThread().getName())
						.append(" Task: ").append(name)
						.append(" Doing a task during: ").append(duration)
						.toString());
				TimeUnit.SECONDS.sleep(duration);
				System.out
						.println(new StringBuilder()
								.append(Thread.currentThread().getName())
								.append(" Task: ").append(name)
								.append(" Finished on: ").append(new Date())
								.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
