/**
 * Created by Layne on 2017/5/18.
 * Server receive and accept connection requests, delegate the connections to RequestHandler to handle.
 * When a connection is accepted, server add the client socket to queue and notify Dispatcher for rescheduling.
 */

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

enum ServerState {On, Off, Idle}

public class Server implements Runnable
{
    public static ConcurrentHashMap<Integer, Socket> clients;
    public static ConcurrentHashMap<Integer, InetAddress> IPTable;
    public static ConcurrentHashMap<Integer, Integer> tempTable;
    public static ConcurrentHashMap<Integer, Log> logTable;
    public static ConcurrentHashMap<Integer, Double> energyTable;
    public static CopyOnWriteArrayList<Integer> queue;
    public static ReentrantLock produceLock;
    public static Condition wakeCond;
    public static RoomInfoHandler roomInfoHandler;
    public static LogHandler logHandler;
    private int port;
    private MainForm mainForm;

    public Server(int port, MainForm mainForm){
        this.port = port;
        this.mainForm = mainForm;
    }


    @Override
    public void run() {
        try{
            ServerSocket server = new ServerSocket(port);
            Socket client;
            clients = new ConcurrentHashMap<>();
            IPTable = new ConcurrentHashMap<>();
            tempTable = new ConcurrentHashMap<>();
            logTable = new ConcurrentHashMap<>();
            queue = new CopyOnWriteArrayList<>();
            energyTable = new ConcurrentHashMap<>();
            produceLock = new ReentrantLock();
            wakeCond = produceLock.newCondition();
            roomInfoHandler = new RoomInfoHandler(Config.roomInfoDir);
            logHandler = new LogHandler(Config.logDir);
            // start dispatcher thread for scheduling
            new Thread(new Dispatcher(mainForm)).start();
            new Thread(new MyTimer()).start();
            while(true){
                // wait for connection
                client = server.accept();
                if(Config.getServerState() == ServerState.Off)
                    break;
                // for each connection, create a thread to handle request
                new Thread(new RequestHandler(client)).start();
            }
            server.close();
            roomInfoHandler.close();
            logHandler.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void removeClient(int client_no) {
        if(queue.contains(client_no)){
            produceLock.lock();
            queue.remove(client_no);
            clients.remove(client_no);
            WakeUpTable.setQueueChanged(true);
            wakeCond.signal();
            produceLock.unlock();
        }
    }
}
