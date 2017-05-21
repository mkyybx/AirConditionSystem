package AC;

import ClientSide.ClientFactory;
import Info.*;
import WebSide.WebSocketEventHandler;
import WebSide.WebSocketFactory;
import XML.XMLizable;
import XML.XMLizableHandler;
import XML.XMLizableParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Ice on 5/20/2017.
 */
public class Controller implements Runnable{

    private static Hashtable<Integer, Integer> clientRoomMap;
    private static Hashtable<Integer, Integer> webSideRoomMap;
    private static Lock lock;
    private static Hashtable<Integer, Boolean> webSideLoginMap;
    private static boolean isHeatMode;
    private static final int ILLEGAL_OPERATION = -1;
    private static final int UNREGISTERED_CLIENT = -2;

    static {
        clientRoomMap = new Hashtable<Integer, Integer>();
        webSideRoomMap = new Hashtable<Integer, Integer>();
        webSideLoginMap = new Hashtable<Integer, Boolean>();
        lock = new ReentrantLock(true);
        isHeatMode = false;
        new Thread(new Controller()).start();
    }

    private Controller() {

    }

    private static void sendErrorInfo(int webSideNum, int errorNum) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            new LoginACKInfo(errorNum, false).write(bao);
        } catch (IOException | UnimplementedException e) {
            e.printStackTrace();
        }
        WebSocketFactory.send(webSideNum, bao.toByteArray());
    }

    private <K, V> ArrayList<K> inverseFind(Hashtable<K, V> table, V value) {
        ArrayList<K> result = new ArrayList<K>();
        for (Map.Entry<K, V> temp : table.entrySet()) {
            if (temp.getValue() == value)
                result.add(temp.getKey());
        }
        return result;
    }

    @Override
    public void run() {

        //初始化Server端
        ClientFactory.getInstance().setLostConnectionHandler(num -> {
            Integer room;
            if ((room = clientRoomMap.get(num)) != null){
                lock.lock();
                clientRoomMap.remove(num);
                ArrayList<Integer> webSide = inverseFind(webSideRoomMap, room);
                for (Integer i : webSide) {
                    try {
                        WebSocketFactory.close(i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    webSideRoomMap.remove(i);
                    webSideLoginMap.remove(i);
                }
/*
                Iterator<Map.Entry<Integer, Integer>> it = webSideRoomMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Integer> temp = it.next();
                    if (temp.getValue() == room) {
                        try {
                            WebSocketFactory.close(temp.getKey());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        webSideRoomMap.remove(temp.getKey());
                        webSideLoginMap.remove(temp.getKey());
                        //可能有点小问题，remove之后可能别人又add进去了。
                    }
                }*/
                lock.unlock();
            }
        });
        new Thread(ClientFactory.getInstance()).start();

        //初始化WebSocket端
        WebSocketFactory.setEventHandler(new WebSocketEventHandler() {
            @Override
            public void onWebSocketStart(int sessionID) {

            }

            @Override
            public void onWebSocketEnd(int sessionID) {
                webSideRoomMap.remove(sessionID);
                webSideLoginMap.remove(sessionID);
            }
        });

        //添加业务逻辑
        class directForwardInfoHandler implements XMLizableHandler {
            @Override
            public void onParseComplete(XMLizable payload, int num, boolean isWebSide) {
                if (!isWebSide) {
                    lock.lock();
                    ArrayList<Integer> webSide = inverseFind(webSideRoomMap, clientRoomMap.get(num));
                    for (Integer i : webSide) {
                        WebSocketFactory.send(i, payload.getRawString());
                    }
                    lock.unlock();
                }
                else {
                    if (webSideLoginMap.get(num) != null && webSideLoginMap.get(num)) {
                        lock.lock();
                        ArrayList<Integer> clientSide = inverseFind(clientRoomMap, webSideRoomMap.get(num));
                        int i = clientSide.get(0);
                        ClientFactory.getInstance().send(i, payload.getRawString());
                        lock.unlock();
                    }
                    else {
                        sendErrorInfo(num, ILLEGAL_OPERATION);
                    }
                }
            }
        }

        //费用信息
        XMLizableParser.getInstance().setFareInfoHandler(new directForwardInfoHandler());

        //Login ACK
        XMLizableParser.getInstance().setLoginACKInfoHandler((payload, num, isWebSide) -> {
            if (!isWebSide) {
                int i = payload.getUuid();
                int webSide = webSideRoomMap.get(i);
                webSideLoginMap.put(webSide, payload.getSuccessful());
                WebSocketFactory.send(webSide, payload.getRawString());
                if (!payload.getSuccessful()) {
                    webSideLoginMap.remove(webSide);
                    webSideRoomMap.remove(webSide);
                }
            }
        });

        //模式信息
        XMLizableParser.getInstance().setModeInfoHandler((payload, num, isWebSide) -> {
            if (payload.isHeating() != isHeatMode && !isWebSide) {
                isWebSide = payload.isHeating();
                WebSocketFactory.broadcast(payload.getRawString());
            }
        });

        //注册信息
        XMLizableParser.getInstance().setRegisterInfoHandler((payload, num, isWebSide) -> {
            if (!isWebSide)
                clientRoomMap.put(num, payload.getClientNumber());
        });

        //传感器温度信息
        XMLizableParser.getInstance().setSensorTempInfoHandler(new directForwardInfoHandler());

        //目标温度消息
        XMLizableParser.getInstance().setTargetTempInfoHandler(new directForwardInfoHandler());

        //Web端登录信息
        XMLizableParser.getInstance().setWebLoginInfoHandler((payload, num, isWebSide) -> {
            if (isWebSide) {
                if (webSideRoomMap.get(num) == null) {
                    try {
                        int clientID = inverseFind(clientRoomMap, payload.getRoom()).get(0);
                        webSideRoomMap.put(num, payload.getRoom());
                        webSideLoginMap.put(num, false);
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        new ClientLoginInfo(payload, payload.getRoom()).write(bao);
                        ClientFactory.getInstance().send(clientID, bao.toByteArray());
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        sendErrorInfo(num, UNREGISTERED_CLIENT);
                    } catch (IOException | UnimplementedException e) {
                        e.printStackTrace();
                    }
                } else {
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    try {
                        new LoginACKInfo(num, true).write(bao);
                    } catch (IOException | UnimplementedException e) {
                        e.printStackTrace();
                    }
                    WebSocketFactory.send(num, bao.toByteArray());
                }
            }
        });

        //注销信息
        XMLizableParser.getInstance().setLogoutInfoHandler((payload, num, isWebSide) -> {
            if (isWebSide) {
                if (webSideRoomMap.get(num) != null && webSideLoginMap.get(num) != null && webSideLoginMap.get(num)) {
                    try {
                        int room = webSideRoomMap.get(num);
                        int clientID = inverseFind(clientRoomMap, room).get(0);
                        ClientFactory.getInstance().close(clientID);
                        //接下来的步骤留给ClientConnectionLostHandler进行
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        sendErrorInfo(num, UNREGISTERED_CLIENT);
                    }
                }
                else sendErrorInfo(num, ILLEGAL_OPERATION);
            }
        });
    }
}
