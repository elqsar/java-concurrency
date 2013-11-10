package cz.boris.concurrency.second;

/**
 * Manipulate on different attributes from several threads.
 * Synchronization of access to variables.
 *
 */
public class TwoCinemas {

	public static void main(String[] args) {
		Cinema cinema = new Cinema();
		TicketOfficeA a = new TicketOfficeA(cinema);
		TicketOfficeB b = new TicketOfficeB(cinema);
		Thread threadA = new Thread(a);
		Thread threadB = new Thread(b);
		threadA.start();
		threadB.start();
		try {
			threadA.join();
			threadB.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		System.out.printf("Cinema A availability: %d\n", cinema.getCinemaAFree());
		System.out.printf("Cinema B availability: %d\n", cinema.getCinemaBFree());
	}

	static class Cinema {
		private int cinemaAFree;
		private int cinemaBFree;

		private final Object lockForA;
		private final Object lockForB;

		public Cinema() {
			this.lockForA = new Object();
			this.lockForB = new Object();
			cinemaAFree = 20;
			cinemaBFree = 20;
		}

		public boolean sellTicketToA(int number) {
			synchronized (lockForA) {
				if (number < cinemaAFree) {
					cinemaAFree -= number;
					return true;
				} else {
					return false;
				}
			}
		}

		public boolean sellTicketToB(int number) {
			synchronized (lockForB) {
				if (number < cinemaBFree) {
					cinemaBFree -= number;
					return true;
				} else {
					return false;
				}
			}
		}

		public boolean freeTicketsForA(int number) {
			synchronized (lockForA) {
				cinemaAFree += number;
				return true;
			}
		}

		public boolean freeTicketsForB(int number) {
			synchronized (lockForB) {
				cinemaBFree += number;
				return true;
			}
		}

		public int getCinemaAFree() {
			return cinemaAFree;
		}

		public int getCinemaBFree() {
			return cinemaBFree;
		}

	}

	static class TicketOfficeB implements Runnable {

		private Cinema cinema;

		public TicketOfficeB(Cinema cinema) {
			this.cinema = cinema;
		}

		@Override
		public void run() {
			cinema.sellTicketToA(3);
			cinema.sellTicketToB(3);
			cinema.sellTicketToA(3);
			cinema.sellTicketToB(5);
			cinema.freeTicketsForB(3);
			cinema.sellTicketToB(1);
			cinema.sellTicketToA(2);
		}

	}

	static class TicketOfficeA implements Runnable {

		private Cinema cinema;

		public TicketOfficeA(Cinema cinema) {
			this.cinema = cinema;
		}

		@Override
		public void run() {
			cinema.sellTicketToB(2);
			cinema.sellTicketToB(3);
			cinema.sellTicketToA(1);
			cinema.sellTicketToB(5);
			cinema.sellTicketToA(3);
			cinema.freeTicketsForA(5);
			cinema.freeTicketsForB(1);
			cinema.sellTicketToB(3);
		}

	}

}
