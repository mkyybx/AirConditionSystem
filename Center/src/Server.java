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
import java.util.concurrent.locks.ReentrantLock;

enum ServerState {On, Off, Idle}

public class Server implements Runnable
{
    public static ConcurrentHashMap<Integer, Socket> clients = null;
    public static ConcurrentHashMap<Integer, InetAddress> IPTable = null;
    public static ConcurrentHashMap<Integer, Integer> tempTable = null;
    public static ConcurrentHashMap<Integer, Log> logTable = null;
    public static ConcurrentHashMap<Integer, Double> energyTable = null;
    public static CopyOnWriteArrayList<Integer> queue = null;
    public static ReentrantLock produceLock = new ReentrantLock();
    public static ReentrantLock wakeLock = new ReentrantLock();
    private int port;

    public Server(int port){
        this.port = port;
    }


    @Override
    public void run() {
        try{
            ServerSocket server = new ServerSocket(this.port);
            Socket client;
            clients = new ConcurrentHashMap<>();
            IPTable = new ConcurrentHashMap<>();
            tempTable = new ConcurrentHashMap<>();
            logTable = new ConcurrentHashMap<>();
            queue = new CopyOnWriteArrayList<>();
            energyTable = new ConcurrentHashMap<>();
            // start dispatcher thread for scheduling
            new Thread(new Dispatcher()).start();
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
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
