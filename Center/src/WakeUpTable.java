/**
 * Created by Layne on 2017/5/20.
 */

public class WakeUpTable {                                  // used to inform dispatcher to handle specific issues
    private static boolean queueChanged = false;
    private static boolean modeChanged = false;
    private static boolean schedulingTimeout = false;
    private static boolean fareTimeout = false;
    private static boolean frequencyChanged = false;

    public static boolean isQueueChanged() {
        return queueChanged;
    }

    public static void setQueueChanged(boolean queueChanged) {
        WakeUpTable.queueChanged = queueChanged;
    }

    public static boolean isModeChanged() {
        return modeChanged;
    }

    public static void setModeChanged(boolean modeChanged) {
        WakeUpTable.modeChanged = modeChanged;
    }

    public static boolean isSchedulingTimeout() {
        return schedulingTimeout;
    }

    public static void setSchedulingTimeout(boolean schedulingTimeout) {
        WakeUpTable.schedulingTimeout = schedulingTimeout;
    }

    public static boolean isFareTimeout() {
        return fareTimeout;
    }

    public static void setFareTimeout(boolean fareTimeout) {
        WakeUpTable.fareTimeout = fareTimeout;
    }

    public static boolean isFrequencyChanged() {
        return frequencyChanged;
    }

    public static void setFrequencyChanged(boolean frequencyChanged) {
        WakeUpTable.frequencyChanged = frequencyChanged;
    }
}
