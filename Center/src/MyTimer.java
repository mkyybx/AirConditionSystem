/**
 * Created by Layne on 2017/5/21.
 */
public class MyTimer implements Runnable{

    @Override
    public void run() {
        int count = 0, MAX_COUNT = 10;
        boolean flag = true;
        while (flag){
            try {
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                System.out.println("Timer is shut down.");
                return;
            }

            System.out.println("Timer expired...");
            try {                           // ticking every one minute
                Server.produceLock.lock();             // wait for dispatcher to finish current job
                System.out.println("timer lock");
                if (Config.getServerState() != ServerState.Off){
                    count++;
                    WakeUpTable.setFareTimeout(true);
                    if(count >= MAX_COUNT){
                        WakeUpTable.setSchedulingTimeout(true);
                        count = 0;
                    }
                }
                else{
                    flag = false;
                }
                Server.wakeCond.signal();
            }
            finally{
                System.out.println("timer unlock");
                Server.produceLock.unlock();
            }
        }
    }
}
