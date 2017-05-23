/**
 * Created by Layne on 2017/5/18.
 * RequestHandler handle the requests sent from slaves.
 * When state of slaves changed, dump the changes to database.
 * When slaves request for fee, load the records from database, compute the fee and return it to slaves.
 * When the connection is dropped, RequestHandler inform Dispatcher for rescheduling.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
                    req =  buf.readLine();
                }
                catch (IOException e){              // client may drop connection
                    break;
                }
                if(req == null || "".equals(req)){  // client may drop connection
                    break;
                }
                else{
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
                            RoomInfoHandler handler = new RoomInfoHandler(Interactor.roomInfoDir);
                            RoomInfo info = handler.select(client_no).get(0);
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
                                int defaultTemp = Config.getMode() == 0 ? 22 : 26;
                                Server.tempTable.put(client_no, defaultTemp);
                            }
                            break;
                    }
                }
            }
            // remove from queue
            if(Server.queue.contains(client_no)){
                int ind = Server.queue.indexOf(client_no);
                Server.queue.remove(ind);
                WakeUpTable.setQueueChanged(true);

            }
            if(succeed){
                Server.clients.remove(client_no);
                Server.logTable.remove(client_no);
                Server.tempTable.remove(client_no);
                Server.energyTable.remove(client_no);
            }
            // close connections
            if(!client.isClosed())
                client.close();

            // dump the log to database
            if(Config.getServerState() == ServerState.Off && !client.isClosed()){
                newLog(Server.logTable.get(client_no), 0);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleAC(Element element) throws Exception {
        boolean positive = Integer.parseInt(element.elementText("Positive")) == 1;
        int level = Integer.parseInt(element.elementText("Wind_Level"));

        Log log = Server.logTable.get(client_no);
        if(positive && !this.positive){         // request for start
            Server.produceLock.lock();                 // wait for dispatcher
            try {
                //Server.produceCond.await();
                if (level != log.level)             // if level changed, compute fee and create a new log
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
        double newFare = Server.energyTable.get(log.Client_No) + log.fee;

        // dump the log to database
        LogHandler handler = new LogHandler(Interactor.logDir);
        ArrayList<Log> logs = new ArrayList<>();
        logs.add(log);
        handler.insert(logs);
        handler.close();

        // start logging a new log
        log.startDate = new MyDate(LocalDateTime.now());
        log.startTemp = Server.tempTable.get(log.Client_No);
        log.level = level;
        log.netDuration = 0;
        log.updateEnergyAndFare();
        log.checkOut = false;
        Server.energyTable.replace(log.Client_No, newFare);
        Server.logTable.replace(log.Client_No, log);
    }

    public static boolean sendMsg(Socket sock, String Msg){
        try {
            PrintStream out = new PrintStream(sock.getOutputStream());
            out.println(Msg);
            out.close();
        }
        catch (IOException e){
            return false;
        }
        return true;
    }
}