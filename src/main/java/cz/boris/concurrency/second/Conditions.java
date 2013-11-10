package cz.boris.concurrency.second;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Conditions {

	public static void main(String[] args) {
		FileMock mock = new FileMock(100, 10);
		Buffer buffer = new Buffer(20);
		Producer producer = new Producer(mock, buffer);
		Thread prodThread = new Thread(producer, "*** Producer Thread ***");
		Consumer[] consumers = new Consumer[3];
		Thread[] consThreads = new Thread[3];
		for (int i = 0; i < 3; i++) {
			consumers[i] = new Consumer(buffer);
			consThreads[i] = new Thread(consumers[i], "*** Consumer Thread ***");
		}
		prodThread.start();
		for (int i = 0; i < 3; i++) {
			consThreads[i].start();
		}
	}

	static class Buffer {

		private LinkedList<String> buffer;
		private int maxSize;
		private ReentrantLock lock;
		private Condition lines;
		private Condition spaces;
		private boolean pendingLines;

		public Buffer(int maxSize) {
			this.maxSize = maxSize;
			buffer = new LinkedList<>();
			lock = new ReentrantLock();
			lines = lock.newCondition();
			spaces = lock.newCondition();
			pendingLines = true;
		}

		/**
		 * Recieve String and try to store it in the buffer
		 * 
		 * @param line
		 *            to store
		 */
		public void insert(String line) {
			lock.lock();
			try {
				while (buffer.size() == maxSize) {
					spaces.await();
				}
				buffer.offer(line);
				System.out.printf("%s: Inserted Line: %d\n", Thread
						.currentThread().getName(), buffer.size());
				lines.signalAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}

		/**
		 * @return first string stored in the buffer
		 */
		public String get() {
			String line = null;
			lock.lock();
			try {
				while (buffer.size() == 0 && hasPendingLines()) {
					lines.await();
				}
				if (hasPendingLines()) {
					line = buffer.poll();
					System.out.printf("%s: Line Readed: %d\n", Thread
							.currentThread().getName(), buffer.size());
					lines.signalAll();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
			return line;
		}

		public void setPendingLines(boolean pendingLines) {
			this.pendingLines = pendingLines;
		}

		public boolean hasPendingLines() {
			return pendingLines || buffer.size() > 0;
		}

	}

	static class Producer implements Runnable {

		private FileMock mock;
		private Buffer buffer;

		public Producer(FileMock mock, Buffer buffer) {
			this.mock = mock;
			this.buffer = buffer;
		}

		@Override
		public void run() {
			buffer.setPendingLines(true);
			while (mock.hasMoreLines()) {
				final String line = mock.getLine();
				buffer.insert(line);
			}
			buffer.setPendingLines(false);
		}

	}

	static class Consumer implements Runnable {

		private Buffer buffer;

		public Consumer(Buffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public void run() {
			while(buffer.hasPendingLines()) {
				final String line = buffer.get();
				processLine(line);
			}
		}

		private void processLine(String line) {
			try {
				Random random = new Random();
				Thread.sleep(random.nextInt(100));
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static class FileMock {
		private String[] content;
		private int index;

		public FileMock(int size, int length) {
			content = new String[size];
			for (int i = 0; i < size; i++) {
				StringBuilder sb = new StringBuilder(length);
				for (int j = 0; j < length; j++) {
					int indice = (int) (Math.random() * 255);
					sb.append((char) indice);
				}
				content[i] = sb.toString();
			}
			index = 0;
		}

		public boolean hasMoreLines() {
			return index < content.length;
		}

		public String getLine() {
			if (hasMoreLines()) {
				System.out.println("Mock " + (content.length - index));
				return content[index++];
			}
			return null;
		}

	}

}
