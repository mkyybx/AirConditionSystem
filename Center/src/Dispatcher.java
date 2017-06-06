/**
 * Created by Layne on 2017/5/19.
 * This class is designed for scheduling the active slaves and informing them the changes of central system.
 * Whenever a connection is established/dropped, scheduling is invoked.
 * whenever mode/frequency is changed, inform the slaves the changes.
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class Dispatcher implements Runnable{
    private ArrayList<Integer> oldList = null;
    private MainForm mainForm;

    public Dispatcher(MainForm mainForm){
        this.mainForm = mainForm;
    }

    @Override
    public void run() {
        int previousQueueLength = 0;
        while(true){
            if(Thread.currentThread().isInterrupted())
                return;
            Server.produceLock.lock();
            try {
                // wait for timer's or client's signal
                Server.wakeCond.await();
                if (WakeUpTable.isModeChanged()){
                    System.out.println("Mode changed");
                    String Msg = XMLPacker.packModeInfo();
                    for (Integer i: Server.clients.keySet())
                        sendMsg(i, Msg);
                    WakeUpTable.setModeChanged(false);
                }
                if (WakeUpTable.isFrequencyChanged()){
                    System.out.println("Frequency changed");
                    String Msg = XMLPacker.packFreqInfo();
                    for (Integer i: Server.clients.keySet())
                        sendMsg(i, Msg);
                    WakeUpTable.setFrequencyChanged(false);
                }
                if(WakeUpTable.isQueueChanged() || WakeUpTable.isSchedulingTimeout()){
                    System.out.println("Rescheduling");
                    schedule();
                    if(WakeUpTable.isQueueChanged())
                        WakeUpTable.setQueueChanged(false);
                    if (WakeUpTable.isSchedulingTimeout())
                        WakeUpTable.setSchedulingTimeout(false);
                    if (!(previousQueueLength > 0 && Server.queue.size() > 0 || previousQueueLength == Server.queue.size()))
                        mainForm.setState(Config.getServerState());
                }
                if (WakeUpTable.isFareTimeout()){
                    System.out.println("Sending fare");
                    broadcastFare();
                    if(oldList != null){
                        for(Integer i: oldList){        // update duration for selected clients
                            Server.logTable.get(i).netDuration++;
                            Server.logTable.get(i).updateEnergyAndFare();
                        }
                    }
                    WakeUpTable.setFareTimeout(false);
                }
            }
            catch (InterruptedException e){
                System.out.println("Dispatcher is shut down");
            }
            finally {
                Server.produceLock.unlock();
            }
            previousQueueLength = Server.queue.size();
        }
    }

    private void broadcastFare() {
        for (Integer i: Server.clients.keySet()){
            double energy = Server.energyTable.get(i) + Server.logTable.get(i).energy;
            double fare = energy * 5;
            String Msg = XMLPacker.packFareInfo(fare, energy);
            sendMsg(i, Msg);
        }
    }

    private void schedule() {
        if (Server.queue.size() == 0) {                 // No one's waiting
            Config.setServerState(ServerState.Idle);    // server go idle
            return;
        }
        Config.setServerState(ServerState.On);          // otherwise it's on

        ArrayList<Integer> selectedList = new ArrayList<>();
        ArrayList<Integer> fullList = new ArrayList<>(Server.queue);
        if(fullList.size() <= 3)          // Everyone can freely use the air conditioner
            selectedList = fullList;
        else{
            // acquire all client_no and sort them
            int first = 0;
            if(oldList != null && fullList.indexOf(oldList.get(oldList.size()-1)) != -1)
                first = fullList.indexOf(oldList.get(oldList.size()-1));
            for (int i = 0; i < 3; i++)         // todo : '3' need to be replaced with a variable
                selectedList.add(fullList.get((first + i) % fullList.size()));
        }

        // keep an old list and current list
        String Msg = XMLPacker.packQueueInfo(true, 1);

        for (Integer i: selectedList)       // inform slaves to start blowing
            sendMsg(i, Msg);

        if (oldList != null){               // inform them to stop
            Msg = XMLPacker.packQueueInfo(false, 1);

            for (Integer i: oldList){
                if (selectedList.contains(i))
                    continue;
                sendMsg(i, Msg);
            }
        }

        oldList = selectedList;
    }

    private void sendMsg(Integer client_no, String Msg) {
        try{
            if(!RequestHandler.sendMsg(new DataOutputStream(Server.clients.get(client_no).getOutputStream()), Msg))
                Server.clients.remove(client_no);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
