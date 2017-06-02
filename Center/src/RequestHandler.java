/**
 * Created by Layne on 2017/5/18.
 * RequestHandler handle the requests sent from slaves.
 * When state of slaves changed, dump the changes to database.
 * When slaves request for fee, load the records from database, compute the fee and return it to slaves.
 * When the connection is dropped, RequestHandler inform Dispatcher for rescheduling.
 */

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


public class RequestHandler implements Runnable {

    public static final String AC_REQ = "AC_req";
    public static final String TEMP_SUBMIT = "Temp_submit";
    public static final String LOGIN = "Login";
    public static final String ACK = "Login_ACK";
    public static final String MODE = "Mode";
    public static final String WIND = "Wind";
    public static final String FARE =  "Fare_info";
    public static final String FREQ = "Temp_Submit_Freq";

    private Socket client = null;
    private boolean positive;
    private int client_no;

    public RequestHandler(Socket client){
        this.client = client;
    }

    @Override
    public void run() {
        try{
            // input stream from client
            BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // first message should be login message
            boolean isFirst = true;
            boolean succeed = false;
            String req;

            while(Config.getServerState() != ServerState.Off && !client.isClosed()){  // until server is shut down or connection is closed by server
                try {                               // receive data from client
                    req =  readMsg(client);
                }
                catch (IOException e){              // client may drop connection
                    break;
                }
                if(req == null || "".equals(req)){  // client may drop connection
                    break;
                }

                Document document = DocumentHelper.parseText(req);
                Element rootElement = document.getRootElement();
                String rootName = rootElement.getName();

                // first message have to be LOGIN package, non-first package should not be LOGIN package
                if ((isFirst && !rootName.equals(LOGIN)) || (!isFirst && rootName.equals(LOGIN)))
                    continue;

                switch (rootName) {
                    case AC_REQ:
                        handleAC(rootElement);
                        break;

                    case TEMP_SUBMIT:
                        handleSubmit(rootElement);
                        break;

                    case LOGIN:
                        String name = rootElement.elementText("Name");
                        String password = rootElement.elementText("Password");
                        int client_no = Integer.parseInt(rootElement.elementText("Client_No"));

                        // verify the name and password
                        RoomInfoHandler handler = new RoomInfoHandler(Config.roomInfoDir);
                        HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", client_no);
                        RoomInfo info = handler.select(Config.roomTable, conditionSet).get(0);
                        handler.close();
                        succeed = (name.equals(info.Name) && password.equals(info.Password));

                        // check if the login ip is the same
                        InetAddress ip = client.getInetAddress();
                        if (succeed && Server.IPTable.containsKey(client_no)) {
                            if (!Server.IPTable.get(client_no).equals(ip))
                                succeed = false;
                        }
                        else if (succeed)
                            Server.IPTable.put(client_no, ip);

                        // pack return message
                        Document doc = DocumentHelper.createDocument();
                        Element root = doc.addElement(ACK);
                        root.addElement("Name").setText(name);
                        root.addElement("Password").setText(password);
                        root.addElement(MODE).setText(Integer.toString(Config.getMode()));
                        root.addElement("Succeed").setText(succeed ? "1" : "0");
                        // return message to client without header
                        if(!sendMsg(client, document.asXML().split("\n")[1])){  // if sending fails, client is disconnected
                            if(succeed)
                                Server.IPTable.remove(client_no);
                            return;             // no need to clean up, since login is not completed
                        }

                        isFirst = !succeed;     // if not succeed, ignore messages except LOGIN
                        if (succeed){
                            this.client_no = client_no;
                            Log log = new Log();
                            log.Client_No = client_no;
                            log.Name = name;
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

            // clean up:
            // remove client from queue
            Server.removeClient(client_no);
            // dump the logs to database
            if(Server.logTable.containsKey(client_no) && (Config.getServerState() == ServerState.Off || !client.isClosed())){
                newLog(Server.logTable.get(client_no), 0);
                Server.logTable.remove(client_no);
            }
            Server.tempTable.remove(client_no);
            // don't remove from energy and IP table, in case of bad network connection
            // close connections
            if(!client.isClosed())
                client.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleAC(Element element) throws Exception {
        boolean positive = Integer.parseInt(element.elementText("Positive")) == 1;
        int level = Integer.parseInt(element.elementText("Wind_Level"));

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

    private void handleSubmit(Element element) {
        Element childElement = element.element("Time");
        int min = Integer.parseInt(childElement.elementText("Min"));
        int sec = Integer.parseInt(childElement.elementText("Sec"));
        int client_no = Integer.parseInt(element.elementText("Client_No"));
        int temp = Integer.parseInt(element.elementText("Temp"));

        // update the temperature
        Server.tempTable.replace(client_no, temp);

    }

    public static void newLog(Log log, int level) throws SQLException{        // store old log into database and start a new log
        log.updateEnergyAndFare();
        log.endDate = new MyDate(LocalDateTime.now());
        log.endTemp = Server.tempTable.get(log.Client_No);
        double newEnergy = Server.energyTable.get(log.Client_No) + log.energy;

        // dump the log to database
        ArrayList<Log> logs = new ArrayList<>(Arrays.asList(log));
        Server.logHandler.insert(Config.logTable, logs);

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

    public static boolean sendMsg(Socket sock, String Msg){
        try {
            DataOutputStream output = new DataOutputStream(sock.getOutputStream());
            output.writeInt(Msg.length());
            output.writeBytes(Msg);
            output.close();
        }
        catch (IOException e){
            return false;
        }
        return true;
    }

    public static String readMsg(Socket sock) throws IOException{
        DataInputStream input = new DataInputStream(sock.getInputStream());
        int length = input.readInt();
        byte[] temp = new byte[length];
        input.readFully(temp);
        return temp.toString();
    }
}