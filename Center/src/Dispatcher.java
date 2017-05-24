/**
 * Created by Layne on 2017/5/19.
 * This class is designed for scheduling the active slaves and informing them the changes of central system.
 * Whenever a connection is established/dropped, scheduling is invoked.
 * whenever mode/frequency is changed, inform the slaves the changes.
 */
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.*;

public class Dispatcher implements Runnable{
    private ArrayList<Integer> oldList = null;

    @Override
    public void run() {
        while(true){
            Server.produceLock.lock();
            try {
                try {
                    // wait for timer's or client's signal
                    Server.wakeCond.await();
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (WakeUpTable.isModeChanged()){
                    Document document = DocumentHelper.createDocument();
                    Element root = document.addElement(RequestHandler.MODE);
                    root.addElement("Heater").setText(Integer.toString(Config.getMode()));
                    String Msg = document.asXML().split("\n")[1];
                    for (Integer i: Server.clients.keySet())
                        sendMsg(i, Msg);
                    WakeUpTable.setModeChanged(false);
                }
                if (WakeUpTable.isFrequencyChanged()){
                    Document document = DocumentHelper.createDocument();
                    Element root = document.addElement(RequestHandler.FREQ);
                    root.addElement("Temp_Submit_Freq").setText(Integer.toString(Config.getFrequency()));
                    String Msg = document.asXML().split("\n")[1];
                    for (Integer i: Server.clients.keySet())
                        sendMsg(i, Msg);
                    WakeUpTable.setFrequencyChanged(false);
                }
                if(WakeUpTable.isQueueChanged() || WakeUpTable.isSchedulingTimeout()){
                    schedule();
                    if(WakeUpTable.isQueueChanged())
                        WakeUpTable.setQueueChanged(false);
                    if (WakeUpTable.isSchedulingTimeout())
                        WakeUpTable.setSchedulingTimeout(false);
                }
                if (WakeUpTable.isFareTimeout()){
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

            finally {
                Server.produceLock.unlock();
            }
        }
    }

    private void broadcastFare() {
        for (Integer i: Server.clients.keySet()){
            double energy = Server.energyTable.get(i) + Server.logTable.get(i).energy;
            double fare = energy * 5;
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement(RequestHandler.FARE);
            root.addElement("Fare").setText(Double.toString(fare));
            root.addElement("Energy").setText(Double.toString(energy));
            sendMsg(i, document.asXML().split("\n")[1]);
        }
    }

    private void schedule() {
        // todo: ServerState should not be changed by dispatcher
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
        Document doc1 = DocumentHelper.createDocument();
        Element root1 = doc1.addElement(RequestHandler.WIND);
        root1.addElement("Start_Blowing").setText(Integer.toString(1));
        // Level should be ignored in this case
        root1.addElement("Level").setText(Integer.toString(1));
        String Msg = doc1.asXML().split("\n")[1];

        for (Integer i: selectedList)       // inform slaves to start blowing
            sendMsg(i, Msg);

        if (oldList != null){               // inform them to stop
            Document doc2 = DocumentHelper.createDocument();
            Element root2 = doc1.addElement(RequestHandler.WIND);
            root2.addElement("Start_Blowing").setText(Integer.toString(0));
            root2.addElement("Level").setText(Integer.toString(1));
            Msg = doc2.asXML().split("\n")[1];

            for (Integer i: oldList){
                if (selectedList.contains(i))
                    continue;
                sendMsg(i, Msg);
            }
        }

        oldList = selectedList;
    }

    private void sendMsg(Integer client_no, String Msg) {
        if(!RequestHandler.sendMsg(Server.clients.get(client_no), Msg))
            Server.clients.remove(client_no);
    }
}
