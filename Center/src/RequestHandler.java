/**
 * Created by Layne on 2017/5/18.
 * RequestHandler handle the requests sent from slaves.
 * When state of slaves changed, dump the changes to database.
 * When slaves request for fee, load the records from database, compute the fee and return it to slaves.
 * When the connection is dropped, RequestHandler inform Dispatcher for rescheduling.
 */

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
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
        // first message should be login message
        boolean isFirst = true, succeed = false;
        String req;

        while(Config.getServerState() != ServerState.Off && !client.isClosed()){  // until server is shut down or connection is closed by server
            try {                               // receive data from client
                req = readMsg(input);
            }
            catch (IOException e){              // client may drop connection
                e.printStackTrace();
                break;
            }
            if(req == null || "".equals(req)){  // client may drop connection
                System.out.println("no request received");
                break;
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

            // first message have to be LOGIN package, non-first package should not be LOGIN package
            if ((isFirst && !parser.getRootName().equals(LOGIN)) || (!isFirst && parser.getRootName().equals(LOGIN)))
                continue;

            switch (parser.getRootName()) {
                case AC_REQ:
                    handleAC(parser.getLevel(), parser.isPositive());
                    break;

                case TEMP_SUBMIT:
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
                        e.printStackTrace();
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

                    isFirst = !succeed;     // if not succeed, ignore messages except LOGIN
                    if (succeed){
                        System.out.println("Login succeed");
                        this.client_no = parser.getClient_no();
                        Log log = new Log();
                        log.Client_No = client_no;
                        log.Name = parser.getName();
                        positive = false;
                        Server.logTable.put(client_no, log);
                        Server.clients.put(client_no, client);
                        if(!Server.energyTable.containsKey(client_no))
                            Server.energyTable.put(client_no, 0.);
                        if(!Server.tempTable.containsKey(client_no))
                            Server.tempTable.put(client_no, Config.getMode() == 0 ? 22 : 26);
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
        if(Server.logTable.containsKey(client_no) && (Config.getServerState() == ServerState.Off || !client.isClosed())){
            newLog(Server.logTable.get(client_no), 0);
            Server.logTable.remove(client_no);
        }
        Server.tempTable.remove(client_no);
        // don't remove from energy and IP table, in case of bad network connection; close connections
    }

    private void handleAC(int level, boolean positive) {
        Log log = Server.logTable.get(client_no);
        if(positive && !this.positive){                 // request for start
            Server.produceLock.lock();                  // wait for dispatcher
            try {
                //Server.produceCond.await();
                if (level != log.level)                 // if level changed, compute fee and create a new log
                    newLog(log, level);
                if(Config.getServerState() == ServerState.Idle){        // turn server on
                    Config.setServerState(ServerState.On);
                }
                Server.queue.add(client_no);
                WakeUpTable.setQueueChanged(true);
                Server.wakeCond.signal();
            }
            finally {
                Server.produceLock.unlock();                  // inform dispatcher
            }
        }
        else if(!positive && this.positive){    // request for stop
            Server.produceLock.lock();                 // wait for dispatcher
            try {
                //Server.produceCond.await();
                int ind = Server.queue.indexOf(client_no);
                Server.queue.remove(ind);
                WakeUpTable.setQueueChanged(true);
                if(Server.queue.size() == 0 && Config.getServerState() == ServerState.On){
                    Config.setServerState(ServerState.Idle);
                }
                Server.wakeCond.signal();
            }
            finally {
                Server.produceLock.unlock();                  // inform dispatcher
            }
        }
        else if(positive && log.level != level){     // client's on and level has changed
            newLog(log, level);                      // compute fee and create a new log
        }
    }

    private void handleSubmit(int client_no, int temp) {
        // update the temperature
        Server.tempTable.replace(client_no, temp);
    }

    public static void newLog(Log log, int level) {        // store old log into database and start a new log
        log.updateEnergyAndFare();
        log.endDate = new MyDate(LocalDateTime.now());
        log.endTemp = Server.tempTable.get(log.Client_No);
        double newEnergy = Server.energyTable.get(log.Client_No) + log.energy;

        // dump the log to database
        ArrayList<Log> logs = new ArrayList<>(Arrays.asList(log));
        try {
            Server.logHandler.insert(Config.logTable, logs);
        }
        catch (SQLException e){
            System.out.println("Error when insert into logTable.");
        }

        // start logging a new log
        log.startDate = new MyDate(LocalDateTime.now());
        log.startTemp = Server.tempTable.get(log.Client_No);
        log.level = level;
        log.netDuration = 0;
        log.updateEnergyAndFare();
        log.checkOut = false;
        Server.energyTable.replace(log.Client_No, newEnergy);
        Server.logTable.replace(log.Client_No, log);
    }

    public static boolean sendMsg(DataOutputStream output, String Msg){
        System.out.println(Msg);
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
        System.out.println(Msg);
        return Msg;
    }
}