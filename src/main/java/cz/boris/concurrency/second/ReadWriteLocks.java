package cz.boris.concurrency.second;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLocks {

	public static void main(String[] args) {
		PricesInfo info = new PricesInfo();
		Reader[] readers = new Reader[5];
		Thread[] tReader = new Thread[5];
		for (int i = 0; i < 5; i++) {
			readers[i] = new Reader(info);
			tReader[i] = new Thread(readers[i]);
		}
		Writer writer = new Writer(info);
		Thread tWriter = new Thread(writer);
		for (int i = 0; i < 5; i++) {
			tReader[i].start();
		}
		tWriter.start();
	}

	static class PricesInfo {
		private BigDecimal priceA;
		private BigDecimal priceB;
		private ReadWriteLock lock;

		public PricesInfo() {
			priceA = new BigDecimal("1.5");
			priceB = new BigDecimal("2.2");
			lock = new ReentrantReadWriteLock();
		}

		public BigDecimal getPriceA() {
			lock.readLock().lock();
			final BigDecimal value = priceA;
			lock.readLock().unlock();
			return value;
		}

		public BigDecimal getPriceB() {
			lock.readLock().lock();
			final BigDecimal value = priceB;
			lock.readLock().unlock();
			return value;
		}

		public void modifyPrices(BigDecimal a, BigDecimal b) {
			lock.writeLock().lock();
			try {
				this.priceA = a;
				this.priceB = b;
			} finally {
				lock.writeLock().unlock();
			}
		}

	}

	static class Reader implements Runnable {

		private PricesInfo info;

		public Reader(PricesInfo info) {
			this.info = info;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				System.out.printf("%s: Price A: %f\n", Thread.currentThread()
						.getName(), info.getPriceA());
				System.out.printf("%s: Price B: %f\n", Thread.currentThread()
						.getName(), info.getPriceB());
			}
		}

	}

	static class Writer implements Runnable {

		private PricesInfo info;

		public Writer(PricesInfo info) {
			this.info = info;
		}

		@Override
		public void run() {
			for (int i = 0; i < 3; i++) {
				System.out.printf("Writer: Attempt to modify the prices.\n");
				info.modifyPrices(new BigDecimal(Math.random() * 10),
						new BigDecimal(Math.random() * 8));
				System.out.printf("Writer: Prices have been modified.\n");
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
