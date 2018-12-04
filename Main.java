import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main {

    /*
        Fetches the carPark singleton.
        Creates and starts the StatusPrinter Thread.
        Creates and starts the commuter threads.
        Interrupts the statusPrinter and prints the time after the commuters complete.
     */
	public static void main(String[] args) throws InterruptedException {
		final CarPark carPark = CarPark.getInstance();
		final long start = System.currentTimeMillis();
		
		Thread statusPrinter = new Thread(new StatusPrinter(carPark));
		statusPrinter.start();

		//Create a thread for each commuter.
        Thread[] commuters = new Thread[carPark.NUM_COMMUTERS];
        for(int i = 0; i< commuters.length; i++) {
            commuters[i] = new Thread(
                (Math.random() > 0.5 
                ? new Lecturer(carPark)
                : new Student(carPark))
            );
            commuters[i].start();
        }

        //Wait for the thread to finish.
        for(Thread com : commuters) {
            com.join();
        }

        //Calculate elapsed time for display.
        long elapsed = System.currentTimeMillis() - start;
        statusPrinter.interrupt(); //Stop the StatusPrinter.
        statusPrinter.join(); //Wait until console cursor is back in correct position.
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
        System.out.printf("[program concluded in %d:%s]\n", minutes, seconds < 10 ? "0"+ seconds: "" + seconds);
    }
}
