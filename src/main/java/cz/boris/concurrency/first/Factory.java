package cz.boris.concurrency.first;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Factory {

	public static void main(String[] args) {
		TFactory factory = new TFactory("Thread Factory");
		Task task = new Task();
		Thread thread;
		System.out.println("Starting threads");
		for (int i = 0; i < 10; i++) {
			thread = factory.newThread(task);
			thread.start();
		}
		System.out.println("Factory statistics: ");
		System.out.printf("%s\n", factory.getStatistics());
	}

	static class TFactory implements ThreadFactory {

		private int counter;
		private String name;
		private List<String> stats;

		public TFactory(String name) {
			counter = 0;
			this.name = name;
			stats = new ArrayList<>();
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, name + "-Thread-" + counter);
			counter++;
			stats.add(String.format("Created thread %d with name %s on %s\n",
					thread.getId(), thread.getName(), new Date()));

			return thread;
		}
		
		public String getStatistics() {
			StringBuffer sb = new StringBuffer();
			Iterator<String> iter = stats.iterator();
			while(iter.hasNext()) {
				sb.append(iter.next());
				sb.append("\n");
			}
			return sb.toString();
		}
	}
	
	/**
	 * Dummy task.
	 */
	static class Task implements Runnable {

		@Override
		public void run() {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
