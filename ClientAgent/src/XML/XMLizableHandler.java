package XML;

/**
 * Created by Ice on 5/18/2017.
 */
public interface XMLizableHandler<T extends XMLizable> {
    void onParseComplete(T payload, int num, boolean isWebSide);
}
