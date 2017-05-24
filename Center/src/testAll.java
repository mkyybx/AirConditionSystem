import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Layne on 2017/5/20.
 */
public class testAll {
    /*
    public static void main(String[] args) throws Exception{
        String name = "name name name";
        String password = "pass pass pass";
        boolean succeed = true;
        int mode = 2;

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("ACK");
        root.addElement("Name").setText(name);
        root.addElement("Password").setText(password);
        root.addElement("Mode").setText(Integer.toString(mode));
        root.addElement("Succeed").setText(succeed ? "1" : "0");
        String str = document.asXML();
        String[] a = str.split("\n");
        for (String i: a){
            System.out.println("New line: " + i);
        }

        Document d = DocumentHelper.parseText("<ACK><Name>name name"
                +" name</Name><Password>pass pass pass</Password><Mode>2</Mode><Succeed>1</Succeed></ACK>");
        Element rootElement = document.getRootElement();
        for (Iterator<Element> i = rootElement.elementIterator(); i.hasNext(); ){
            Element e = i.next();
            System.out.println(e.getText());
        }

        //System.out.println(str);

        try {
            send(document.asXML());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) throws Exception{

        //RoomInfoHandler handler = new RoomInfoHandler("roomInfo.db");
        ArrayList<RoomInfo> roomInfos = new ArrayList<>();
        RoomInfo info = new RoomInfo();
        info.Client_No = 444444;
        info.Name = "Layne";
        info.Password = "654321";
        roomInfos.add(info);
        info = new RoomInfo();
        info.Client_No = 555555;
        info.Name = "Layne";
        info.Password = "123456";
        roomInfos.add(info);
        //handler.insert(roomInfos);


        ArrayList<Log> logs = new ArrayList<>();
        LogHandler handler = new LogHandler("log.db");
        Log log = new Log();
        log.Client_No = 111111;
        log.Name = "Layne";
        log.startDate = new MyDate(LocalDateTime.now());
        log.endDate = new MyDate(LocalDateTime.now());
        log.netDuration = 0;
        log.energy = 100;
        log.fee = 500;
        log.level = 2;
        log.startTemp = 20;
        log.endTemp = 18;
        log.checkOut = false;
        logs.add(log);

        //handler.insert(logs);

        ArrayList<Log> ret = Server.logHandler.select("log", DatabaseHandler.map("Client_No", log.Client_No));
        for(Log i: ret)
            System.out.println("Result: Client_No " + i.Client_No + ", Name " + i.Name + ", Start " +
                    i.startDate.month + " " + i.startDate.week + " " + i.startDate.day + ", checkOut " + i.checkOut);
        HashMap<String, Object> valueSet = DatabaseHandler.map("checkOut", true);
        HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", log.Client_No);
        handler.update("log", conditionSet, valueSet);
        ret = Server.logHandler.select("log", DatabaseHandler.map("Client_No", log.Client_No));
        for(Log i: ret)
            System.out.println("Result: Client_No " + i.Client_No + ", Name " + i.Name + ", Start " +
                    i.startDate.month + " " + i.startDate.week + " " + i.startDate.day + ", checkOut " + i.checkOut);

        if(valueSet.remove("xxx") == null)
            System.out.println("shit");

        //if(valueSet.remove("checkOut") == null)
        //    System.out.println("shits");
        System.out.println(valueSet.put("checkOut", false));
        for(Map.Entry<String, Object> e: valueSet.entrySet()){
            System.out.println(e.getKey() + e.getValue());
        }
        valueSet.replace("checkOut", true);
        for(Map.Entry<String, Object> e: valueSet.entrySet()){
            System.out.println(e.getKey() + e.getValue());
        }

        Server.roomInfoHandler.close();
        Server.logHandler.close();
    }

    private static void send(String str) throws IOException{
        // connect to server on port 9999
        Socket client = new Socket("127.0.0.1", 9999);
        client.setSoTimeout(10000);
        // get input
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        // output stream to server
        PrintStream out = new PrintStream(client.getOutputStream());
        // input stream from server
        BufferedReader buf =  new BufferedReader(new InputStreamReader(client.getInputStream()));

        System.out.println("Sent string: " + str);
        out.println(str);

        input.close();
    }
}

/*test database
*
public static void main(String[] args) throws Exception{

    RoomInfoHandler handler = new RoomInfoHandler("roomInfo.db");
    ArrayList<RoomInfo> roomInfos = new ArrayList<>();
    RoomInfo info = new RoomInfo();
    info.Client_No = 444444;
    info.Name = "Layne";
    info.Password = "654321";
    roomInfos.add(info);
    info = new RoomInfo();
    info.Client_No = 555555;
    info.Name = "Layne";
    info.Password = "123456";
    roomInfos.add(info);
    handler.insert(roomInfos);


    ArrayList<Log> logs = new ArrayList<>();
    LogHandler handler = new LogHandler("log.db");
    Log log = new Log();
    log.Client_No = 111111;
    log.Name = "Layne";
    log.startDate = new MyDate(LocalDateTime.now());
    log.endDate = new MyDate(LocalDateTime.now());
    log.netDuration = 0;
    log.energy = 100;
    log.fee = 500;
    log.level = 2;
    log.startTemp = 20;
    log.endTemp = 18;
    log.checkOut = false;
    logs.add(log);

    // handler.insert(logs);

    ArrayList<Log> ret = handler.select(log.Client_No);
    for(Log i: ret)
        System.out.println("Result: Client_No " + i.Client_No + ", Name " + i.Name + ", Start " +
                i.startDate.month + " " + i.startDate.week + " " + i.startDate.day + ", checkOut " + i.checkOut);
    handler.setCheckOut(log.Client_No);
    ret = handler.select(log.Client_No);
    for(Log i: ret)
        System.out.println("Result: Client_No " + i.Client_No + ", Name " + i.Name + ", Start " +
                i.startDate.month + " " + i.startDate.week + " " + i.startDate.day + ", checkOut " + i.checkOut);
    handler.close();
}
*/

/* test socket server
public class Server {
    public static void main(String[] args) throws IOException {
        try{
            ServerSocket server = new ServerSocket(9999);
            Socket client = server.accept();
            PrintStream out = new PrintStream(client.getOutputStream());

            BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println("1: " + buf.readLine());
            System.out.println("2: " + buf.readLine());

            out.close();
            client.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
 */

/* test socket client
public static void main(String[] args) throws IOException {
        // connect to server on port 9999
        Socket client = new Socket("127.0.0.1", 9999);
        client.setSoTimeout(10000);
        // get input
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        // output stream to server
        PrintStream out = new PrintStream(client.getOutputStream());
        // input stream from server
        BufferedReader buf =  new BufferedReader(new InputStreamReader(client.getInputStream()));
        boolean flag = true;
        while(flag){
            System.out.print("Enter message:");
            String str = input.readLine();
            // send message to server
            out.println(str);
            if("bye".equals(str)){
                flag = false;
            }else{
                try{
                    // wait for response from server
                    String echo = buf.readLine();
                    System.out.println(echo);
                }catch(SocketTimeoutException e){
                    System.out.println("Time out, No response");
                }
            }
        }
        input.close();
        if(client != null){
            // disconnect if socket connection is established
            client.close();
        }
    }
 */