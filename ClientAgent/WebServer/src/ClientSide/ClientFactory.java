package ClientSide;

import XML.XMLizableParser;
import org.dom4j.DocumentException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Created by Ice on 5/18/2017.
 */
public class ClientFactory implements Runnable{

    private static ClientFactory instance = null;
    private ServerSocket socket;
    private Hashtable<Integer, Socket> mapping;//线程安全
    private ClientConnectionLostHandler lostConnectionHandler;

    static {
        instance = new ClientFactory();
    }

    public static ClientFactory getInstance() {
        return instance;
    }

    private ClientFactory() {
        for (int i = 0; i < 1; i++) {
            try {
                //测试用
                socket = new ServerSocket((int)(Math.random() * 65536));
                System.out.println("绑定" + socket.getLocalPort() + "端口成功");
                //socket = new ServerSocket(9999);
            } catch (IOException e) {
                System.out.println("绑定" + socket.getLocalPort() + "端口失败，5秒后重试……");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                i--;
            }
        }
    }

    public void close(int num) {
        try {
            mapping.get(num).close();
            mapping.remove(num);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket thisSocket = socket.accept();
                int num;
                Socket test;
                do {
                    num = (int)(Math.random() * 65535);
                    test = mapping.get(num);
                } while (test != null);
                mapping.put(num,thisSocket);
                new Thread(new ClientReceiver(thisSocket, num)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLostConnectionHandler(ClientConnectionLostHandler lostConnectionHandler) {
        this.lostConnectionHandler = lostConnectionHandler;
    }

    public void send(int clientNum, byte[] xml) {
        try {
            Socket socket = mapping.get(clientNum);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeInt(xml.length);
            output.write(xml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientReceiver implements Runnable {

        Socket socket;
        int num;

        ClientReceiver(Socket socket, int num) {
            this.socket = socket;
            this.num = num;
        }

        //仅在接收时检测是否关闭连接
        @Override
        public void run() {
            try {
                while (true) {
                    DataInputStream stream = new DataInputStream(socket.getInputStream());
                    int length = stream.readInt();
                    byte[] temp = new byte[length];
                    for (int i = 0; i < length; i++)
                        temp[i] = stream.readByte();
                    try {
                        XMLizableParser.getInstance().parse(temp, num, false);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                mapping.remove(num);
                lostConnectionHandler.onClientConnectionLost(num);
            }
        }
    }
}
