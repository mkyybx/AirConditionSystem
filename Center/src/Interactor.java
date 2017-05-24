/**
 * Created by Layne on 2017/5/19.
 * This class interact with hotel stuffs. Hotel stuffs can set mode of air conditioner,
 * the frequency slaves report their states and request for reports with this class.
 */

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class Interactor{
    public static String roomInfoDir = "roomInfo.db";
    public static String roomTable = "roomInfo";
    public static String logDir = "log.db";
    public static String logTable = "log";
    private static final int MODE = 0;
    private static final int FREQ = 1;
    private static final int REPORT = 2;
    private static final int CHECKOUT = 3;
    private static final int SHUTDOWN = 4;
    private static final int ASSIGN = 5;

    public static void main(String[] args) {
        int port = 9999;
        new Thread(new Server(port)).start();

        System.out.println("Wake up table isQueueChanged = " + WakeUpTable.isQueueChanged());
        WakeUpTable.setQueueChanged(true);
        System.out.println("Wake up table isQueueChanged = " + WakeUpTable.isQueueChanged());

        printHelp();

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            int choice = scanner.nextInt();
            switch (choice){
                case MODE:
                    System.out.println("Input the mode (0: Cold, 1: Warm) you want to specify:");
                    int mode = scanner.nextInt();
                    if(mode == Config.getMode())
                        System.out.println("The mode is " + mode +" already. No need for change.");
                    else{           // set new mode
                        try {
                            Server.produceLock.lock();
                            System.out.println("Here");
                            Config.setMode(mode);
                        }
                        finally {
                            Server.wakeCond.signal();
                            Server.produceLock.unlock();
                        }
                        System.out.println("The mode is successfully set to " + mode);
                    }
                    break;

                case FREQ:
                    System.out.println("Input the freq (in seconds) you want to specify:");
                    int freq = scanner.nextInt();
                    if(freq == Config.getFrequency())
                        System.out.println("The freq is " + freq +" already. No need for change.");
                    else{                                   // set new frequency
                        try {
                            // System.out.println("Before Interactor locked.");
                            Server.produceLock.lock();
                            // System.out.println("Interactor locked.");
                            Config.setFrequency(freq);
                        }
                        finally {
                            //Server.produceLock.lock();
                            Server.wakeCond.signal();
                            Server.produceLock.unlock();
                        }
                        System.out.println("The freq is successfully set to " + freq);
                    }
                    break;

                case REPORT:
                    try {
                        getReport();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;

                case CHECKOUT:
                    try {
                        clientCheckout();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;

                case SHUTDOWN:
                    try {
                        serverShutdown();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    System.out.println("System is shut down...");
                    System.exit(0);
                    break;

                case ASSIGN:
                    try {
                        assignClient();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            }
            printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("0 to set mode;");
        System.out.println("1 to set frequency;");
        System.out.println("2 to get reports;");
        System.out.println("3 to checkout;");
        System.out.println("4 to shutdown system;");
        System.out.println("5 to assign clients to specific room.");
        System.out.println("Please input your choice:");
    }

    private static void assignClient() throws SQLException {
        RoomInfo info = new RoomInfo();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input the room you want to assign: ");
        info.Client_No = scanner.nextInt();
        System.out.println("Input the name of client: ");
        info.Name = scanner.nextLine();
        System.out.println("Input the password of client: ");
        info.Password = scanner.nextLine();


        HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", info.Client_No);
        ArrayList<RoomInfo> res = Server.roomInfoHandler.select(roomTable, conditionSet);
        if(res.size() == 0){                    // the room is available
            ArrayList<RoomInfo> tempList = new ArrayList<>(Arrays.asList(info));
            Server.roomInfoHandler.insert(tempList);
        }
        else{                                   // the room is occupied
            System.out.println("Assignment failed, the room is occupied. Please check out before check in.");
        }
    }

    private static void serverShutdown() throws SQLException {
        Config.setServerState(ServerState.Off);
        for (Integer i : Server.clients.keySet()){
            roomCheckOut(i);
        }
    }

    private static void clientCheckout() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input the Room No to be checked out (-1 to quit):");
        int client_no = -1;
        while (scanner.hasNext()){
            client_no = scanner.nextInt();
            if(Server.clients.containsKey(client_no))
                break;
            if(client_no == -1)
                return;
            System.out.println("Error. No such room.");
        }
        double energy = roomCheckOut(client_no);
        double fee = energy * 5;
        System.out.println("The energy you consumed is " + energy + " units, total fee is " + fee);
        System.out.println("You have checked out.");
    }

    private static double roomCheckOut(int client_no) throws SQLException{
        // checkout
        try {
            Server.clients.get(client_no).close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if(Server.queue.contains(client_no)){
            Server.produceLock.lock();
            Server.queue.remove(client_no);
            Server.clients.remove(client_no);
            WakeUpTable.setQueueChanged(true);
            Server.wakeCond.signal();
            Server.produceLock.unlock();
        }
        if(Server.logTable.containsKey(client_no)){
            RequestHandler.newLog(Server.logTable.get(client_no), 0);
            Server.logTable.remove(client_no);
        }
        double energy = Server.energyTable.get(client_no);
        Server.energyTable.remove(client_no);
        Server.tempTable.remove(client_no);
        Server.IPTable.remove(client_no);

        // set checkOut to true
        HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", client_no);
        HashMap<String, Object> valueSet = DatabaseHandler.map("checkOut", true);
        Server.roomInfoHandler.update(roomTable, conditionSet, valueSet);

        return energy;
    }

    private static void getReport() throws SQLException {
        System.out.println("What type of report you want to acquire?");
        System.out.println("0 for monthly report, 1 for weekly report, 2 for daily report.");
        Scanner scanner = new Scanner(System.in);

        // acquire the list of clients that have logs in database
        ArrayList<Object> roomList = Server.logHandler.selectColumnDistinct(logTable, "Client_No", new HashMap<String, Object>());

        MyDate now = new MyDate(LocalDateTime.now());
        HashMap<Integer, ArrayList<Log> > reports = new HashMap<>();
        HashMap<Integer, Double> fees = new HashMap<>();
        int reportType = scanner.nextInt();
        for (Object object: roomList){
            int client_no = (int) object;
            HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", client_no, "startMonth", now.month);
            if(reportType > 0)
                conditionSet.put("startWeek", now.week);
            if(reportType > 1)
                conditionSet.put("startDay", now.day);
            ArrayList<Log> logs = Server.logHandler.select(logTable, conditionSet);
            double fee = 0;
            for(Log log: logs){
                fee += log.fee;
            }
            reports.put(client_no, logs);
            fees.put(client_no, fee);
        }
        // show report
    }
}
