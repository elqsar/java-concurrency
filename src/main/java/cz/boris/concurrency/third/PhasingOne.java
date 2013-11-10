package cz.boris.concurrency.third;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class PhasingOne {

	public static void main(String[] args) {
		Phaser phaser = new Phaser(3);
		FileSearch system = new FileSearch("C:\\Windows", "log", phaser);
		FileSearch apps = new FileSearch("C:\\Program Files", "log", phaser);
		FileSearch documents = new FileSearch("C:\\Documents And Settings",
				"log", phaser);
		Thread systemThread = new Thread(system, "System");
		systemThread.start();
		Thread appsThread = new Thread(apps, "Apps");
		appsThread.start();
		Thread documentsThread = new Thread(documents, "Documents");
		documentsThread.start();
		try {
			systemThread.join();
			appsThread.join();
			documentsThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Terminated: " + phaser.isTerminated());
	}

	static class FileSearch implements Runnable {

		private String initPath;
		private String end;
		private List<String> result;
		private Phaser phaser;

		public FileSearch(String initPath, String end, Phaser phaser) {
			this.initPath = initPath;
			this.end = end;
			this.result = new ArrayList<>();
			this.phaser = phaser;
		}

		private void processDirectory(File file) {
			File[] list = file.listFiles();
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					if (list[i].isDirectory()) {
						processDirectory(list[i]);
					} else {
						processFile(list[i]);
					}
				}
			}
		}

		private void processFile(File file) {
			if (file.getName().endsWith(end)) {
				result.add(file.getAbsolutePath());
			}
		}

		private void filter() {
			List<String> newResults = new ArrayList<>();
			long actualDate = new Date().getTime();
			for (int i = 0; i < result.size(); i++) {
				File file = new File(result.get(i));
				long fileDate = file.lastModified();
				if (actualDate - fileDate < TimeUnit.MICROSECONDS.convert(1,
						TimeUnit.DAYS)) {
					newResults.add(result.get(i));
				}
			}
			result = newResults;
		}

		private boolean checkResult() {
			if (result.isEmpty()) {
				System.out.printf("%s: Phase %d: 0 results.\n", Thread
						.currentThread().getName(), phaser.getPhase());
				System.out.printf("%s: Phase %d: End.\n", Thread
						.currentThread().getName(), phaser.getPhase());
				phaser.arriveAndDeregister();
				return false;
			} else {
				System.out.printf("%s: Phase %d: %d results.\n", Thread
						.currentThread().getName(), phaser.getPhase(), result
						.size());
				phaser.arriveAndAwaitAdvance();
				return true;
			}
		}

		private void showInfo() {
			for (int i = 0; i < result.size(); i++) {
				File file = new File(result.get(i));
				System.out.printf("%s: %s\n", Thread.currentThread().getName(),
						file.getAbsolutePath());
			}
			phaser.arriveAndAwaitAdvance();
		}

		@Override
		public void run() {
			phaser.arriveAndAwaitAdvance();
			System.out.printf("%s: Starting.\n", Thread.currentThread()
					.getName());
			File file = new File(initPath);
			if (file.isDirectory()) {
				processDirectory(file);
			}
			if (!checkResult()) {
				return;
			}
			filter();
			if (!checkResult()) {
				return;
			}
			showInfo();
			phaser.arriveAndDeregister();
			System.out.printf("%s: Work completed.\n", Thread.currentThread()
					.getName());
		}

	}

}
