import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Commuter implements Runnable {
    final boolean IS_DEX; //Flag for driver that delays entry
    final boolean CARD_REFUSED; //Flag for driver walking to office
    final int PARK_TIME;
    final int WAIT_TIME;
    final int LEAVE_TIME;
    final CarPark carPark;

    //Static variable to hold number of completed commuters.
    static final AtomicInteger complete = new AtomicInteger();

    Commuter(CarPark carPark) {
        //Randomly initialise delays and flags.
        IS_DEX = Math.random() > 0.3;
        CARD_REFUSED = Math.random() < 0.3;
        WAIT_TIME = (int)(Math.random()*100);
        LEAVE_TIME = (int)(Math.random()*100000);
        PARK_TIME = 1000;
        this.carPark = carPark;
    }

    /*
        Represents a driver entering the carpark.
        PARK_TIME is the time taken to park.
        Returns true is parking is successful.
     */
    private boolean enter() throws InterruptedException {
        if(!carPark.checkAvailable(CARD_REFUSED)){
            return false;
        }
        Thread.sleep(PARK_TIME); 
        
        for(int i = 0; i < 10; i++) {
            if(!carPark.park(IS_DEX)) {
                Thread.sleep(1000);
            } else {
                return true;
            }
        }
        return false;
    }

    /*
        Represents a driver leaving the carpark.
        Includes a delay iof 1/4 the park time.
     */
    public boolean leave() throws InterruptedException {
        carPark.vacateSpace(IS_DEX);
        carPark.leave.acquire();
        Thread.sleep(PARK_TIME/4);
        carPark.leave.release();
        return true;
    }

    /*
        Runs the enter and leave with a delay in between.
        Increments completed counter.
     */
    public void run() {
        try {
            if(enter()) {
                Thread.sleep(LEAVE_TIME);
                leave();
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        complete.getAndIncrement();
    }
}
