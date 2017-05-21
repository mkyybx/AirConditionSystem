package WebSide;

/**
 * Created by Ice on 5/20/2017.
 */
public interface WebSocketEventHandler {
    void onWebSocketStart(int sessionID);
    void onWebSocketEnd(int sessionID);
}
