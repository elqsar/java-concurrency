package cz.boris.concurrency.second;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class ConsumerProducer {
	
	public static void main(String[] args) {
		DataStorage storage = new DataStorage();
		Producer producer = new Producer(storage);
		Thread prod = new Thread(producer);
		Consumer consumer = new Consumer(storage);
		Thread cons = new Thread(consumer);
		prod.start();
		cons.start();
	}
	
	static class DataStorage {
		private static final int MAX_SIZE = 10;
		private Queue<Date> storage;
		
		public DataStorage() {
			storage = new LinkedList<>();
		}
		
		public synchronized void put() {
			while(storage.size() == MAX_SIZE) {
				try {
					wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			storage.offer(new Date());
			System.out.printf("Put new data: size is %d\n", storage.size());
			notifyAll();
		}
		
		public synchronized void get() {
			while(storage.size() == 0) {
				try {
					wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.printf("Get: %d : %s\n", storage.size(), storage.poll());
			// Caution! Its not guarantee Thread wake-up
			notifyAll();
		}
	}
	
	static class Producer implements Runnable {
		private DataStorage storage;
		public Producer(DataStorage storage) {
			this.storage = storage;
		}
		@Override
		public void run() {
			for (int i = 0; i < 100; i++) {
				storage.put();
			}
		}
	}
	
	static class Consumer implements Runnable {
		private DataStorage storage;
		public Consumer(DataStorage storage) {
			this.storage = storage;
		}
		@Override
		public void run() {
			for (int i = 0; i < 100; i++) {
				storage.get();
			}
		}
	}

}
