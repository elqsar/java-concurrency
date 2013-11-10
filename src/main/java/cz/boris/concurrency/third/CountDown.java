package cz.boris.concurrency.third;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDown {

	public static void main(String[] args) {
		Course course = new Course(10);
		Thread thread = new Thread(course);
		thread.start();
		for (int i = 0; i < 10; i++) {
			Student student = new Student(course, "Student " + i);
			Thread t = new Thread(student);
			t.start();
		}
	}

	static class Student implements Runnable {

		private String name;
		private Course course;

		public Student(Course course, String name) {
			this.course = course;
			this.name = name;
		}

		@Override
		public void run() {
			try {
				long duration = (long) (Math.random() * 10);
				TimeUnit.SECONDS.sleep(duration);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			course.arrive(name);
		}

	}

	static class Course implements Runnable {

		private final CountDownLatch latch;

		public Course(int number) {
			latch = new CountDownLatch(number);
		}

		/**
		 * countDown() method is thread safe but 
		 * getCount() is valid just till it returned
		 * @param student name
		 */
		public void arrive(String name) {
			System.out.println("Student arrive " + name);
			latch.countDown();
			//System.out.println("Number arrived: " + latch.getCount());
		}

		@Override
		public void run() {
			System.out.println("Course about to begin. Waiting for "
					+ latch.getCount() + " participants");
			try {
				latch.await();
				System.out.println("All are here.Lets begin!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
