import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class CarPark {
	public static final int MAXIUM_CAPACITY = 1000;
	public static final int TIME_TO_ENDORSED = 1000;
	public static final int MAX_CAR_SPACE_WAIT = 1;
	public static final int NUM_ENTRANCES = 3;
	public static final int NUM_EXITS = 3;
	public static final int NUM_PERMITS = 100;
	public static final int NUM_COMMUTERS = 200;

	final AtomicInteger runningTotal;
	public final Semaphore enter; //Limits number of cars entering
	public final Semaphore leave; //Limits number of cars leaving
	public final Semaphore driverPermits; //

	//Singleton Pattern
	static CarPark res = new CarPark();
	static CarPark getInstance() {
		return res;
	}

	//Constructor
	private CarPark () {
		runningTotal = new AtomicInteger();
		this.enter = new Semaphore(NUM_ENTRANCES, true);
		this.leave = new Semaphore(NUM_EXITS, true);
		this.driverPermits = new Semaphore(NUM_PERMITS, true);
	}

	/*
		Represents the car queueing for the entrance and either being admitted or turned away.
		Delays if the car's card is rejected.
		Returns false if the car cannot acquire a permit to drive around and look for a space within a certain time.
		Returns true if the car successfully enters.
	 */
	boolean checkAvailable(boolean cardRefused){
		try {
			enter.acquire();
			if(cardRefused) {
				Thread.sleep(TIME_TO_ENDORSED);
			}
			if(!driverPermits.tryAcquire(MAX_CAR_SPACE_WAIT, TimeUnit.SECONDS)) {
				enter.release();
				return false;
			}
			enter.release();
			return true;
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			return false;
		}
		
	}

	/*
		Returns the current number of occupied spaces.
		Using this to make any decision would be a malign race condition.
		Used by the StatusPrinter class.
	 */
	public int getRunningTotal(){
		return runningTotal.get();
	};


	/*
		Represents a car parking in a space.
		Releases the driving around permit and increases the running total.
		Returns false if all spaces are taken.
		Number of spaces taken depends on isDex flag.
	 */
	synchronized boolean park(boolean isDex){
		driverPermits.release();
		int incValue = isDex ? 1 : 2;
		if(runningTotal.get() + incValue <= MAXIUM_CAPACITY) {
			runningTotal.addAndGet(incValue);
			return true;
		}
		return false;
	}

	/*
		Represents a car leaving a space.
		RunningTotal is decremented.
	 */
	synchronized void vacateSpace(boolean isDex) {
		runningTotal.addAndGet(-(isDex ? 1 : 2));
	}

}
