/**
 * Created by Layne on 2017/5/21.
 */
public class MyTimer implements Runnable{

    @Override
    public void run() {
        int count = 0;
        boolean flag = true;
        while (flag){
            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                System.out.println("Timer is shut down.");
                return;
            }

            // System.out.println("Timer expired...");
            Server.produceLock.lock();             // wait for dispatcher to finish current job
            try {                           // ticking every one minute
                if (Config.getServerState() != ServerState.Off){
                    count++;
                    WakeUpTable.setFareTimeout(true);
                    if(count >= Config.getFrequency()){
                        WakeUpTable.setSchedulingTimeout(true);
                        count = 0;
                    }
                }
                else{
                    flag = false;
                }
            }
            finally{
                Server.wakeCond.signal();
                Server.produceLock.unlock();
            }
        }
    }
}
