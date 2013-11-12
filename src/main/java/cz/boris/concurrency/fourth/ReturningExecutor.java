package cz.boris.concurrency.fourth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReturningExecutor {

	public static void main(String[] args) {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(2);
		List<Future<Integer>> resultList = new ArrayList<>(); // diamonds really nice:)
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			Integer integer = random.nextInt(10);
			FactorialCalculator calculator = new FactorialCalculator(integer);
			Future<Integer> result = executor.submit(calculator);
			resultList.add(result);
		}
		do {
			System.out.printf("Main: Number of Completed Tasks:	%d\n",
					executor.getCompletedTaskCount());
			for (int i = 0; i < resultList.size(); i++) {
				Future<Integer> result = resultList.get(i);
				System.out.printf("Main: Task %d: %s\n", i, result.isDone());
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} while (executor.getCompletedTaskCount() < resultList.size());
		System.out.printf("Main: Results\n");
		for (int i = 0; i < resultList.size(); i++) {
			Future<Integer> result = resultList.get(i);
			Integer number = null;
			try {
				// caution: get blocks until task is finished
				number = result.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.printf("Main: Task %d: %d\n", i, number);
		}
		executor.shutdown();
	}

	static class FactorialCalculator implements Callable<Integer> {

		private Integer number;

		public FactorialCalculator(Integer number) {
			this.number = number;
		}

		@Override
		public Integer call() throws Exception {
			int result = 1;
			if (number == 0 || number == 1)
				result = 1;
			else {
				for (int i = 2; i <= number; i++) {
					result = result * i;
					TimeUnit.MILLISECONDS.sleep(30);
				}
			}
			System.out.printf("%s: %d\n", Thread.currentThread().getName(),
					result);
			return result;
		}

	}

}
