package cz.boris.concurrency.fourth;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class FutureTaskSample {

	public static void main(String[] args) {
		ExecutorService service = Executors.newCachedThreadPool();
		Result[] results = new Result[5];
		for (int i = 0; i < 5; i++) {
			Task task = new Task("Task " + i);
			results[i] = new Result(task);
			service.submit(results[i]);
		}
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < results.length; i++) {
			results[i].cancel(true);
		}
		for (int i = 0; i < results.length; i++) {
			try {
				if(!results[i].isCancelled()) System.out.println("Done " + results[i].get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		service.shutdown();
	}

	static class Task implements Callable<String> {

		private String name;

		public Task(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String call() throws Exception {
			try {
				long duration = (long) (Math.random() * 10);
				System.out.println(name + ":Waiting for " + duration
						+ " for result");
				TimeUnit.SECONDS.sleep(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "Return from " + name;
		}

	}

	static class Result extends FutureTask<String> {

		private String name;

		public Result(Callable<String> callable) {
			super(callable);
			this.name = ((Task) callable).getName();
		}

		@Override
		protected void done() {
			if (isCancelled())
				System.out.println("Task cancelled: " + name);
			else
				System.out.println("Task finished: " + name);
		}

	}

}
