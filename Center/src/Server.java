/**
 * Created by Layne on 2017/5/18.
 * Server receive and accept connection requests, delegate the connections to RequestHandler to handle.
 * When a connection is accepted, server add the client socket to queue and notify Dispatcher for rescheduling.
 */

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
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
    private ArrayList<Thread> childThreads;

    public Server(int port, MainForm mainForm){
        this.port = port;
        this.mainForm = mainForm;
        childThreads = new ArrayList<>();
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
    }


    @Override
    public void run() {

        try {
            // start dispatcher thread for scheduling
            Thread thDispatcher = new Thread(new Dispatcher(mainForm));
            Thread thTimer = new Thread(new MyTimer());
            childThreads.add(thDispatcher);
            childThreads.add(thTimer);
            thDispatcher.start();
            thTimer.start();
            ServerSocket server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(port));
            server.setSoTimeout(1000);
            Socket client;
            while(true){
                try {
                    client = server.accept();
                }
                catch (SocketTimeoutException e){
                    if (Thread.currentThread().isInterrupted()){
                        for (Thread th : childThreads)
                            th.interrupt();
                        server.close();
                        System.out.println("Server closed.");
                        break;
                    }
                    else
                        continue;
                }
                // for each connection, create a thread to handle request
                Thread th = new Thread(new RequestHandler(client));
                childThreads.add(th);
                th.start();
                System.out.println("A connection is established.");
            }
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }
        finally {
            roomInfoHandler.close();
            logHandler.close();
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
