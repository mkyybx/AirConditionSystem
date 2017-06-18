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

            try {
                Server.produceLock.lock();
                System.out.println("dispatcher lock...");
                // wait for timer's or client's signal
                Server.wakeCond.await();
                System.out.println("dispatcher wake...");
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
                    System.out.println("Back to run...");
                    if(WakeUpTable.isQueueChanged())
                        WakeUpTable.setQueueChanged(false);
                    if (WakeUpTable.isSchedulingTimeout())
                        WakeUpTable.setSchedulingTimeout(false);
                    if (!(previousQueueLength > 0 && Server.queue.size() > 0 || previousQueueLength == Server.queue.size()))
                        mainForm.setState(Config.getServerState());
                    System.out.println("end of queue changed...");
                }
                if (WakeUpTable.isFareTimeout()){
                    System.out.println("Sending fare");
                    broadcastFare();
                    if(oldList != null){
                        for(Integer i: oldList){        // update duration for selected clients
                            if(!Server.queue.contains(i)){
                                System.out.println("not in the queue");
                                continue;
                            }
                            System.out.println(i + " in the queue");
                            Server.logTable.get(i).netDuration++;
                            Server.logTable.get(i).updateEnergyAndFare();
                        }
                    }
                    WakeUpTable.setFareTimeout(false);
                }
                System.out.println("end of try...");
            }
            catch (InterruptedException e){
                System.out.println("Dispatcher is shut down");
            }
            finally {
                System.out.println("dispatcher unlock...");
                Server.produceLock.unlock();
            }
            previousQueueLength = Server.queue.size();
        }
    }

    private void broadcastFare() {
        double energy;
        for (Integer i: Server.clients.keySet()){
            try {
                energy = Server.energyTable.get(i) + Server.logTable.get(i).energy;
            }
            catch (NullPointerException e){
                e.printStackTrace();
                energy = 0;
            }
            double fare = energy * 5;
            String Msg = XMLPacker.packFareInfo(fare, energy);
            sendMsg(i, Msg);
        }
    }

    private void schedule() {
        if (Server.queue.size() == 0) {                 // No one's waiting
            Config.setServerState(ServerState.Idle);    // server go idle
            System.out.println("queue size is 0");
            for (Integer i: Server.clients.keySet()){
                if (Server.logTable.get(i).level == 0)
                    continue;
                String Msg = XMLPacker.packQueueInfo(false, Server.logTable.get(i).level);
                sendMsg(i, Msg);
            }
            oldList = null;
            return;
        }
        System.out.println("queue size is > 0");
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

        String Msg;
        // keep an old list and current list
        for (Integer i: selectedList) {
            Msg = XMLPacker.packQueueInfo(true, Server.logTable.get(i).level);// inform slaves to start blowing
            sendMsg(i, Msg);
        }

        for (Integer i: Server.clients.keySet()){
            if (selectedList.contains(i) || Server.logTable.get(i).level == 0)
                continue;
            Msg = XMLPacker.packQueueInfo(false, Server.logTable.get(i).level);
            sendMsg(i, Msg);
        }

        oldList = selectedList;
    }

    private void sendMsg(Integer client_no, String Msg) {
        if (!Server.clients.containsKey(client_no))
            return;
        try{
            if(!RequestHandler.sendMsg(new DataOutputStream(Server.clients.get(client_no).getOutputStream()), Msg))
                Server.clients.remove(client_no);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
