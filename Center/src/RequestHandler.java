/**
 * Created by Layne on 2017/5/18.
 * RequestHandler handle the requests sent from slaves.
 * When state of slaves changed, dump the changes to database.
 * When slaves request for fee, load the records from database, compute the fee and return it to slaves.
 * When the connection is dropped, RequestHandler inform Dispatcher for rescheduling.
 */

import org.dom4j.DocumentException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RequestHandler implements Runnable {

    public static final String AC_REQ = "AC_Req";
    public static final String TEMP_SUBMIT = "Temp_Submit";
    public static final String LOGIN = "Login";
    public static final String ACK = "Login_ACK";
    public static final String MODE = "Mode";
    public static final String WIND = "Wind";
    public static final String FARE =  "Fare_Info";
    public static final String FREQ = "Temp_Submit_Freq";

    private Socket client = null;
    private boolean positive;
    private int client_no;

    public RequestHandler(Socket client){
        this.client = client;
    }

    @Override
    public void run() {
        DataInputStream input;
        DataOutputStream output;
        try {
            input = new DataInputStream(client.getInputStream());
            output = new DataOutputStream(client.getOutputStream());
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        sendMsg(output, XMLPacker.packModeInfo());
        sendMsg(output, XMLPacker.packFreqInfo());
        // first message should be login message
        boolean isFirst = true, succeed = false;
        String req;
        int temp = 26;

        while(Config.getServerState() != ServerState.Off && !client.isClosed()){  // until server is shut down or connection is closed by server
            try {                               // receive data from client
                req = readMsg(input);
            }
            catch (IOException e){              // client may drop connection
                e.printStackTrace();
                return;
            }
            if(req == null || "".equals(req)){  // client may drop connection
                System.out.println("no request received");
                return;
            }

            if(Thread.currentThread().isInterrupted()){
                if (succeed)
                    break;
                else
                    return;
            }
            System.out.println(req);

            XMLParser parser = new XMLParser();
            try {
                if(!parser.parseText(req)){
                    System.out.println("Bad request.");
                    continue;
                }
            }
            catch (DocumentException e){
                e.printStackTrace();
            }

            System.out.println("Before");
            // first message have to be Temp Submit package
            if (isFirst && !parser.getRootName().equals(TEMP_SUBMIT)){
                System.out.println("IfsFirst: " + isFirst + " " +  parser.getRootName());
                continue;
            }
            // if not login
            if(!isFirst && !succeed && (!parser.getRootName().equals(LOGIN) && !parser.getRootName().equals(TEMP_SUBMIT))){
                System.out.println("Continued..." + isFirst + " " + succeed + parser.getRootName());
                continue;
            }
            isFirst = false;
            System.out.println("here");

            switch (parser.getRootName()) {
                case AC_REQ:
                    handleAC(parser.getLevel(), parser.isPositive());
                    break;

                case TEMP_SUBMIT:
                    temp = parser.getTemp();
                    handleSubmit(parser.getClient_no(), parser.getTemp());
                    break;

                case LOGIN:
                    System.out.println("Login");
                    // verify the name and password
                    HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", parser.getClient_no());
                    try {
                        RoomInfo info = Server.roomInfoHandler.select(Config.roomTable, conditionSet).get(0);
                        succeed = (parser.getName().equals(info.Name) && parser.getPassword().equals(info.Password));
                    }
                    catch (Exception e){
                        succeed = false;
                    }

                    // check if the login ip is the same
                    InetAddress ip = client.getInetAddress();
                    if (succeed && Server.IPTable.containsKey(client_no)) {
                        if (!Server.IPTable.get(client_no).equals(ip))
                            succeed = false;
                    }
                    else if (succeed)
                        Server.IPTable.put(client_no, ip);

                    // pack return message
                    String Msg = XMLPacker.packLoginACK(parser.getName(), parser.getPassword(), succeed);
                    // return message to client without header
                    if(!sendMsg(output, Msg)){  // if sending fails, client is disconnected
                        if(succeed)
                            Server.IPTable.remove(client_no);
                        return;             // no need to clean up, since login is not completed
                    }

                    if (succeed){
                        System.out.println("Login succeed");
                        this.client_no = parser.getClient_no();
                        Log log = new Log();
                        log.Client_No = client_no;
                        log.Name = parser.getName();
                        log.startDate = new MyDate(LocalDateTime.now());
                        positive = false;
                        if(!Server.energyTable.containsKey(client_no))
                            Server.energyTable.put(client_no, 0.);
                        if(!Server.tempTable.containsKey(client_no))
                            Server.tempTable.put(client_no, temp);
                        Server.logTable.put(client_no, log);
                        Server.clients.put(client_no, client);
                    }
                    break;
            }
        }

        cleanUp();
        // close stream
        try {
            input.close();
            output.close();
            if(!client.isClosed())
                client.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Client disconnected.");
    }

    private void cleanUp(){
        // clean up: remove client from queue
        Server.removeClient(client_no);
        // dump the logs to database
        commitLog(client_no);
        Server.logTable.remove(client_no);
        //if(Server.logTable.containsKey(client_no) && (Config.getServerState() == ServerState.Off || !client.isClosed())){
        //}
        Server.tempTable.remove(client_no);
        // don't remove from energy and IP table, in case of bad network connection; close connections
    }

    private void handleAC(int level, boolean positive) {
        System.out.println("In AC");
        if(level <= 0 || level > 3)
            return;

        if(positive && !this.positive){                 // request for start
            newLog(client_no, level);
            try {
                Server.produceLock.lock();                  // wait for dispatcher
                System.out.println("client start lock");
                if(Config.getServerState() == ServerState.Idle){        // turn server on
                    Config.setServerState(ServerState.On);
                }
                Server.queue.add(client_no);
                WakeUpTable.setQueueChanged(true);
                Server.wakeCond.signal();
            }
            finally {
                System.out.println("client start unlock");
                Server.produceLock.unlock();                  // inform dispatcher
            }
        }
        else if(!positive && this.positive){    // request for stop
            try {
                Server.produceLock.lock();                 // wait for dispatcher
                System.out.println("client stop lock");
                int ind = Server.queue.indexOf(client_no);
                Server.queue.remove(ind);
                System.out.println("Queue size" + Server.queue.size());
                WakeUpTable.setQueueChanged(true);
                if(Server.queue.size() == 0 && Config.getServerState() == ServerState.On){
                    Config.setServerState(ServerState.Idle);
                }
                Server.wakeCond.signal();
            }
            finally {
                System.out.println("client stop unlock");
                Server.produceLock.unlock();                  // inform dispatcher
            }
            commitLog(client_no);
        }
        else if(positive && Server.logTable.get(client_no).level != level){     // client's on and level has changed
            commitLog(client_no);
            newLog(client_no, level);                      // compute fee and create a new log
        }
        this.positive = positive;
    }

    private void handleSubmit(int client_no, int temp) {
        // update the temperature
        Server.tempTable.replace(client_no, temp);
    }

    public static void commitLog(int client_no){
        Log log = Server.logTable.get(client_no);
        log.updateEnergyAndFare();
        log.endDate = new MyDate(LocalDateTime.now());
        log.endTemp = Server.tempTable.get(client_no);
        double newEnergy = Server.energyTable.get(client_no) + log.energy;
        Server.energyTable.replace(log.Client_No, newEnergy);
        log.energy = 0;

        try {
            Server.logHandler.insert(Config.logTable, new ArrayList<>(Arrays.asList(log)));
        }
        catch (SQLException e){
            e.printStackTrace();
            System.out.println("Error when insert into logTable.");
        }
    }

    public static void newLog(int client_no, int level) {        // store old log into database and start a new log
        Log log = Server.logTable.get(client_no);
        log.startDate = new MyDate(LocalDateTime.now());
        log.startTemp = Server.tempTable.get(client_no);
        log.level = level;
        log.netDuration = 0;
        log.energy = 0;
        //double newEnergy = Server.energyTable.get(client_no) + log.energy;
        //Server.energyTable.replace(log.Client_No, newEnergy);
        log.updateEnergyAndFare();
        log.checkOut = false;
        Server.logTable.replace(log.Client_No, log);
    }

    public static boolean sendMsg(DataOutputStream output, String Msg){
        System.out.println("Len + " + Msg.length() + Msg);
        try {
            output.writeInt(Msg.length());
            output.writeBytes(Msg);
        }
        catch (IOException e){
            return false;
        }
        return true;
    }

    public static String readMsg(DataInputStream input) throws IOException{
        int length = input.readInt();
        byte[] temp = new byte[length];
        input.readFully(temp);
        String Msg = new String(temp);
        System.out.println("Read len + " + length + " " +  Msg);
        return Msg;
    }
}