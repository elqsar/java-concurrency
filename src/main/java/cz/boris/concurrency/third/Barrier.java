package cz.boris.concurrency.third;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Barrier {

	public static void main(String[] args) {
		final int rows = 10000;
		final int numbers = 1000;
		final int search = 5;
		final int participants = 5;
		final int linesParticipant = 2000;

		MatrixMock mock = new MatrixMock(rows, numbers, search);
		Result result = new Result(rows);
		Grouper grouper = new Grouper(result);
		CyclicBarrier barrier = new CyclicBarrier(participants, grouper);
		Searcher[] searchers = new Searcher[participants];
		for (int i = 0; i < participants; i++) {
			searchers[i] = new Searcher(i * linesParticipant,
					(i * linesParticipant) + linesParticipant, mock, result, 5,
					barrier);
			Thread task = new Thread(searchers[i]);
			task.start();
		}
		System.out.println("Main() finished");

	}

	static class MatrixMock {
		private int[][] data;

		public MatrixMock(int size, int length, int number) {
			int counter = 0;
			data = new int[size][length];
			Random random = new Random();
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < length; j++) {
					data[i][j] = random.nextInt(10);
					if (data[i][j] == number) {
						counter++;
					}
				}
			}
			System.out
					.printf("Mock: There are %d ocurrences of number in generated data.\n",
							counter, number);
		}

		public int[] getRow(int row) {
			if (row >= 0 && row < data.length) {
				return data[row];
			}
			return null;
		}

	}

	static class Result {

		private int[] data;

		public Result(int size) {
			data = new int[size];
		}

		public void setData(int position, int value) {
			data[position] = value;
		}

		public int[] getData() {
			return data;
		}

	}

	static class Searcher implements Runnable {

		private int firstRow;
		private int lastRow;
		private MatrixMock mock;
		private Result result;
		private int number;
		private final CyclicBarrier barrier;

		public Searcher(int firstRow, int lastRow, MatrixMock mock,
				Result result, int number, CyclicBarrier barrier) {
			this.firstRow = firstRow;
			this.lastRow = lastRow;
			this.mock = mock;
			this.result = result;
			this.number = number;
			this.barrier = barrier;
		}

		@Override
		public void run() {
			int counter;
			System.out.printf("%s: Processing lines from %d to %d.\n", Thread
					.currentThread().getName(), firstRow, lastRow);
			for (int i = firstRow; i < lastRow; i++) {
				int row[] = mock.getRow(i);
				counter = 0;
				for (int j = 0; j < row.length; j++) {
					if (row[j] == number) {
						counter++;
					}
				}
				result.setData(i, counter);
			}
			System.out.printf("%s: Lines processed.\n", Thread.currentThread()
					.getName());
			try {
				barrier.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}

	}

	static class Grouper implements Runnable {

		private Result result;

		public Grouper(Result result) {
			this.result = result;
		}

		@Override
		public void run() {
			int finalResult = 0;
			System.out.println("Grouper: processing...");
			int[] data = result.getData();
			for (int i : data) {
				finalResult += i;
			}
			System.out.println("Grouper: Total result " + finalResult);
		}

	}

}
