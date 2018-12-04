import java.util.concurrent.Semaphore;

public class StatusPrinter implements Runnable {

	CarPark carPark;
	int totalPermits;

	//Number of lines in the message
	static final int LINES = 7;
	
	public StatusPrinter(CarPark carPark){
        this.carPark = carPark;
		this.totalPermits = carPark.NUM_PERMITS;
	}

	/*
	    Runs the statusPrinter every 100ms until interrupted.
	    Then cleans up and terminates.
	 */
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            printStatus();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                break;
            }
        }
        printStatus();
        goDownNLines(LINES+1);
    }

    /*
        Calculates the number of threads at each stage of parking, then displays a ticker.
        Since the calculations are not atomic and other threads continue to work, a benign race condition occurs.
        This may result in the values not agreeing with each other temporarily.
     */
    private void printStatus() {
	    final Semaphore enter = carPark.enter;
	    final Semaphore leave = carPark.leave;
	    final Semaphore permits = carPark.driverPermits;
	    final int enterQueueLength = enter.getQueueLength();
	    final int enterTakenPermits = carPark.NUM_ENTRANCES - enter.availablePermits();
	    final int driverQueueLength = permits.getQueueLength();
	    final int driverTakenPermits =  carPark.NUM_PERMITS - permits.availablePermits();
	    final int spacesTaken = carPark.getRunningTotal();
	    final int leaveQueueLength = leave.getQueueLength();
	    final int leaveTakenPermits = carPark.NUM_EXITS - leave.availablePermits();
	    final int completed = Commuter.complete.get();

        System.out.println("\renterQ: \t"+enterQueueLength+"   \t");
        System.out.println("\rentering: \t"+enterTakenPermits+"   \t");
        System.out.println("\rpermitsQ: \t"+driverQueueLength+"   \t");
        System.out.println("\rpermitsUsed: \t"+driverTakenPermits+"   \t");
        System.out.println("\rspacesUsed: \t"+spacesTaken+"   \t");
        System.out.println("\rleaveQ: \t"+leaveQueueLength+"   \t");
        System.out.println("\rleaving: \t"+leaveTakenPermits+"   \t");
        System.out.print("\rcomplete: \t"+completed+"   \t");
        goUpNLines(LINES);
    }

    /*
        Print ansii escape codes to move the cursor vertically N lines.
     */
    private void goUpNLines(int n) {
		final String os = System.getProperty("os.name");
		if ((!os.startsWith("Windows")) || os.contains("10")) {
			//Linux or win10 we can use ansi escape codes
			while((n--)>0) System.out.print("\033[F");
		} else {
			try {
				//windows < 10 just clear console  (not great)
				Thread.sleep(1000);
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	    Print n newlines to move the cursor down N lines.
	 */
	private void goDownNLines(int n){
		while((n--)>0) System.out.print("\r\n");
	}

}
