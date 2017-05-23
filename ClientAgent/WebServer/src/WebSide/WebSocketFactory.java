package WebSide;
/**
 * Created by Ice on 5/17/2017.
 */
import XML.XMLizableParser;
import org.dom4j.DocumentException;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Hashtable;
import java.util.Map;

@ServerEndpoint(value = "/ws")
public class WebSocketFactory {

    private static WebSocketEventHandler eventHandler = null;//用之前记得判空
    private static Hashtable<Integer, Session> sessionMapping;
    private static Hashtable<String, Long> tokenTimesMapping;
    private int sessionID;
    private String token;
    private static final int TIMES = 5;
    private static final int RESET_TIME = 60000;

    static {
        sessionMapping = new Hashtable<Integer, Session>();
        tokenTimesMapping = new Hashtable<String, Long>();
    }


    public static void setEventHandler(WebSocketEventHandler eventHandler) {
        WebSocketFactory.eventHandler = eventHandler;
    }


    public static void send(int num, byte[] message) {
        Session session = sessionMapping.get(num);
        try {
            session.getBasicRemote().sendText(new String(message));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error");
        }
    }

    public static void broadcast(byte[] message) {
        for (Map.Entry<Integer, Session> entry : sessionMapping.entrySet()) {
            try {
                entry.getValue().getBasicRemote().sendBinary(MappedByteBuffer.wrap(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(int num) throws IOException {
        sessionMapping.get(num).close();
        sessionMapping.remove(num);
    }

    @OnOpen
    public void onOpen(Session session) {
        while (eventHandler == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        do {
            sessionID = (int) (Math.random() * 65536);
        } while (sessionMapping.put(sessionID, session) != null);
        this.token = token;
        eventHandler.onWebSocketStart(sessionID);
    }

    @OnClose
    public void onClose() {
        sessionMapping.remove(sessionID);
        eventHandler.onWebSocketEnd(sessionID);
    }

    @OnMessage
    public void onMessage(String message) throws DocumentException {
        XMLizableParser.getInstance().parse(message.getBytes(), sessionID, true);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {

    }
}



//
//import java.io.IOException;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import javax.websocket.OnClose;
//import javax.websocket.OnError;
//import javax.websocket.OnMessage;
//import javax.websocket.OnOpen;
//import javax.websocket.Session;
//import javax.websocket.server.ServerEndpoint;
//
///**
// * Created by Ice on 5/17/2017.
// */
//@ServerEndpoint(value = "/ws")
//public class Login {
//
//    private static final String GUEST_PREFIX = "Guest";
//    private static final AtomicInteger connectionIds = new AtomicInteger(0);
//    private static final Set<Login> connections =
//            new CopyOnWriteArraySet<>();
//
//    private final String nickname;
//    private Session session;
//
//    public Login() {
//        nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
//    }
//
//
//    @OnOpen
//    public void start(Session session) {
//        this.session = session;
//        connections.add(this);
//        String message = String.format("* %s %s", nickname, "has joined.");
//        broadcast(message);
//    }
//
//
//    @OnClose
//    public void end() {
//        connections.remove(this);
//        String message = String.format("* %s %s",
//                nickname, "has disconnected.");
//        broadcast(message);
//    }
//
//
//    @OnMessage
//    public void incoming(String message) {
//        // Never trust the client
//        String filteredMessage = message.toString();
//        broadcast(filteredMessage);
//    }
//
//
//
//
//    @OnError
//    public void onError(Throwable t) throws Throwable {
//        System.out.println("Chat Error: " + t.toString());
//        t.printStackTrace();
//    }
//
//
//    private static void broadcast(String msg) {
//        for (Login client : connections) {
//            try {
//                synchronized (client) {
//                    client.session.getBasicRemote().sendText(msg);
//                }
//            } catch (IOException e) {
//                System.out.println("Chat Error: Failed to send message to client");
//                e.printStackTrace();
//                connections.remove(client);
//                try {
//                    client.session.close();
//                } catch (IOException e1) {
//                    // Ignore
//                }
//                String message = String.format("* %s %s",
//                        client.nickname, "has been disconnected.");
//                broadcast(message);
//            }
//        }
//    }
////    @Override
////    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
////        File f = new File("D:\\workspace\\AirConditioner\\web\\chat.xhtml");
//////        String[] temp = f.list();
//////        for (String s : temp) {
//////            resp.getWriter().println(s);
//////        }
////        Scanner input = new Scanner(f);
////        while (input.hasNext())
////            resp.getWriter().println(input.nextLine());
////    }
//}
