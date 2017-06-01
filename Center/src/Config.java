/**
 * Created by Layne on 2017/5/20.
 */
public class Config {
    private static int mode = 0;
    private static int frequency = 5;
    private static ServerState serverState = ServerState.Off;

    public synchronized static int getMode(){
        return mode;
    }

    public synchronized static void setMode(int mode) {
        Config.mode = mode;
    }

    public synchronized static int getFrequency() {
        return frequency;
    }

    public synchronized static void setFrequency(int frequency) {
        Config.frequency = frequency;
    }

    public synchronized static ServerState getServerState() {
        return serverState;
    }

    public synchronized static void setServerState(ServerState serverState) {
        Config.serverState = serverState;
    }
}
