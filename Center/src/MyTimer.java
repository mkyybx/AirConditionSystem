import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Layne on 2017/5/21.
 */
public class MyTimer implements Runnable{

    @Override
    public void run() {
        int count = 0;
        while (true){
            try {                           // ticking every one minute
                Server.produceLock.lock();             // wait for dispatcher to finish current job

                if (Config.getServerState() == ServerState.Off)
                    break;
                count++;
                WakeUpTable.setFareTimeout(true);
                if(count == Config.getFrequency()){
                    WakeUpTable.setSchedulingTimeout(true);
                    count = 0;
                }
            }
            finally{
                Server.wakeLock.unlock();
            }
        }
    }
}
