package cz.boris.concurrency.fourth;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutor {

	public static void main(String[] args) {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		System.out.println("Starting at: " + new Date());
		for (int i = 0; i < 5; i++) {
			Task task = new Task("Task " + i);
			service.schedule(task, i+1, TimeUnit.SECONDS);
		}
		service.shutdown();
		try {
			service.awaitTermination(1, TimeUnit.HOURS);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("End at: " + new Date());
	}

	static class Task implements Callable<String> {

		private String name;

		public Task(String name) {
			this.name = name;
		}

		@Override
		public String call() throws Exception {
			System.out.println("Starting Task at: " + name + " " + (new Date()));
			return "Hello!";
		}

	}

}
