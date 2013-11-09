package cz.boris.concurrency.first;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Daemon thread.
 * 
 */
public class Daemon {

	public static Object lock = new Object();

	/**
	 * Use Thread-safe collection or synchronize with lock.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Deque<Event> deque = new ArrayDeque<>();
		WriterTask task = new WriterTask(deque);
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(task);
			thread.start();
		}
		CleanerTask cleanerTask = new CleanerTask(deque);
		cleanerTask.start();
	}

	static class WriterTask implements Runnable {

		private Deque<Event> deque;

		public WriterTask(Deque<Event> deque) {
			this.deque = deque;
		}

		@Override
		public void run() {
			for (int i = 0; i < 100; i++) {
				Event event = new Event();
				event.setId(i);
				event.setDate(new Date());
				event.setEvent(String.format(
						"The thread %s has generated an event", Thread
								.currentThread().getId()));
				synchronized (lock) {
					deque.addFirst(event);
				}
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static class CleanerTask extends Thread {
		private Deque<Event> deque;

		public CleanerTask(Deque<Event> deque) {
			this.deque = deque;
			setDaemon(true); // set it as a daemon thread 
		}

		@Override
		public void run() {
			while (true) {
				Date date = new Date();
				clean(date);
			}
		}

		private void clean(Date date) {
			long difference;
			boolean delete;
			if (deque.isEmpty()) {
				return;
			}
			delete = false;
			do {
				Event event = deque.getLast();
				difference = date.getTime() - event.getDate().getTime();
				if (difference > 10000) {
					System.out.println("Clean event: " + event.getId());
					deque.removeLast();
					delete = true;
				}
			} while (difference > 10000);
			if (delete) {
				System.out.println("Cleaner: size of queue is " + deque.size());
			}
		}

	}

	static class Event {
		private int id;
		private Date date;
		private String event;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getEvent() {
			return event;
		}

		public void setEvent(String event) {
			this.event = event;
		}

	}

}
