package cz.boris.concurrency.first;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cz.boris.concurrency.first.First.PrimeGenerator.FileSearch;
import cz.boris.concurrency.first.First.PrimeGenerator.NetworkResourceLoader;
import cz.boris.concurrency.first.First.PrimeGenerator.ResourceLoader;

/**
 * Simple Threading.
 *
 */
public class First {

	enum Example {
		FIRST("Creating simple Thread"), SECOND("Getting Thread information"), THIRD(
				"Thread interruption"), FOURTH("Controlled Thread interruption"), FIFTH(
				"Thread and join()"), SIXTH("Thread Exception handling"), SEVENTH(
				"Thread interruption");

		private String description;

		private Example(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

	}

	/**
	 * Two ways of creating Thread: 1. Extend @link Thread class 2. Implement @link
	 * Runnable class Only calling the start method create new execution thread.
	 * 
	 * @param args
	 */

	public static void main(String[] args) {
		Example code = Example.SIXTH;
		switch (code) {
		case FIRST:
			System.out.println(code.getDescription());
			for (int i = 0; i <= 10; i++) {
				Calculator calculator = new Calculator(i);
				Thread thread = new Thread(calculator);
				thread.start();
			}
			break;
		case SECOND:
			System.out.println(code.getDescription());
			Thread[] threads = new Thread[10];
			Thread.State[] status = new Thread.State[10];
			for (int i = 0; i < 10; i++) {
				threads[i] = new Thread(new Calculator(i));
				if (i % 2 == 0) {
					threads[i].setPriority(Thread.MAX_PRIORITY);
				} else {
					threads[i].setPriority(Thread.MIN_PRIORITY);
				}
				threads[i].setName("THREAD " + i);
			}
			try (FileWriter fw = new FileWriter(new File("log.txt"));
					PrintWriter pw = new PrintWriter(fw)) {
				for (int i = 0; i < 10; i++) {
					pw.println(new StringBuffer().append("Status of Thread ")
							.append(i).append(" : ")
							.append(threads[i].getState().toString()));
					status[i] = threads[i].getState();
				}
				for (int i = 0; i < 10; i++) {
					threads[i].start();
				}
				boolean finish = false;
				while (!finish) {
					for (int i = 0; i < 10; i++) {
						if (threads[i].getState() != status[i]) {
							writeThreadInfo(pw, threads[i], status[i]);
							status[i] = threads[i].getState();
						}
					}
					finish = true;
					for (int i = 0; i < 10; i++) {
						finish = finish
								&& (threads[i].getState() == State.TERMINATED);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case THIRD:
			System.out.println(code.getDescription());
			Thread task = new PrimeGenerator();
			task.start();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			task.interrupt();
			break;
		case FOURTH:
			System.out.println(code.getDescription());
			FileSearch fileSearch = new FileSearch("C:\\", "angular.js");
			Thread thread = new Thread(fileSearch);
			thread.start();
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			thread.interrupt();
			break;
		case FIFTH:
			System.out.println(code.getDescription());
			ResourceLoader loader = new ResourceLoader();
			Thread resource = new Thread(loader, "Resource Loader");
			NetworkResourceLoader loader2 = new NetworkResourceLoader();
			Thread network = new Thread(loader2, "Network Loader");
			resource.start();
			network.start();
			try {
				resource.join(); // wait resource to finish than continue
				network.join(); // wait network to finish than continue
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Configuration has been loaded: %s\n", new Date());
			break;
		case SIXTH:
			System.out.println(code.getDescription());
			Thread error = new Thread(new ErrorTask());
			/**
			 * The way JVM looking for exception handlers for uncaught exceptions:
			 * 1. UncaughtExceptionHandler of the Thread
			 * 2. UncaughtExceptionHandler of the ThreadGroup
			 * 3. DefaultExceptionHandler - this one is valid for all application
			 */
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					System.out.println("Exception is occured.");
					System.out.printf("Thread %s\n", t.getId());
					System.out.printf("Exception info: %s %s", e.getClass().getName(), e.getMessage());
				}
			});
			error.start();
			break;
		default:
			break;
		}
	}

	private static void writeThreadInfo(PrintWriter pw, Thread thread,
			State state) {
		pw.printf("Main : Id %d - %s\n", thread.getId(), thread.getName());
		pw.printf("Main : Priority: %d\n", thread.getPriority());
		pw.printf("Main : Old State: %s\n", state);
		pw.printf("Main : New State: %s\n", thread.getState());
		pw.printf("Main : ************************************\n");
	}
	
	static class ErrorTask implements Runnable {

		@Override
		public void run() {
			Integer.parseInt("AAA");
		}
		
	}

	static class Calculator implements Runnable {

		private int number;

		public Calculator(int number) {
			this.number = number;
		}

		public void run() {
			for (int i = 0; i <= 10; i++) {
				System.out.printf("%s: %d * %d = %d\n", Thread.currentThread()
						.getName(), number, i, i * number);
			}
		}

	}

	static class PrimeGenerator extends Thread {

		@Override
		public void run() {
			long number = 1L;
			while (true) {
				if (isPrime(number)) {
					System.out.printf("Number %d is Prime.\n", number);
				}
				if (isInterrupted()) {
					System.out.println("Generator was interrupted");
					return;
				}
				number++;
			}
		}

		static class FileSearch implements Runnable {

			private final String rootPath;
			private final String fileName;

			public FileSearch(String rootPath, String fileName) {
				this.rootPath = rootPath;
				this.fileName = fileName;
			}

			@Override
			public void run() {
				File file = new File(rootPath);
				if(file.isDirectory()) {
					try {
						processDirectory(file);
					} catch(InterruptedException e) {
						System.out.printf("%s: The search has been interrupted.", Thread.currentThread().getName());
					}
				}
			}

			private void processDirectory(File file) throws InterruptedException {
				File[] list = file.listFiles();
				if(list != null) {
					for(int i = 0; i < list.length; i++) {
						if(list[i].isDirectory()) {
							processDirectory(list[i]);
						} else {
							processFile(list[i]);
						}
					}
				}
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
			}

			private void processFile(File file) throws InterruptedException {
				if(file.getName().equals(fileName)) {
					System.out.printf("%s : %s\n", Thread.currentThread().getName(), file.getAbsolutePath());
				}
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
			}

		}
		
		static class ResourceLoader implements Runnable {

			@Override
			public void run() {
				System.out.printf("%s ***** Beginning loading: %s\n", Thread.currentThread().getName(), new Date());
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				System.out.printf("%s ***** End loading: %s\n", Thread.currentThread().getName(), new Date());
			}
			
		}
		
		static class NetworkResourceLoader implements Runnable {

			@Override
			public void run() {
				System.out.printf("%s ***** Beginning loading: %s\n",Thread.currentThread().getName(), new Date());
				try {
					TimeUnit.SECONDS.sleep(7);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				System.out.printf("%s ***** End loading: %s\n",Thread.currentThread().getName(), new Date());
			}
			
		}

		private boolean isPrime(long number) {
			if (number <= 2) {
				return true;
			}
			for (long i = 2; i < number; i++) {
				if (number % i == 0) {
					return false;
				}
			}
			return true;
		}

	}

}
