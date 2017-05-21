package Info;

import AC.UnimplementedException;
import XML.XMLizable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ice on 5/18/2017.
 */
public class SensorTempInfo extends XMLizable {

    private int sensorTemp;

    public SensorTempInfo(int sensorTemp) {
        this.sensorTemp = sensorTemp;
    }

    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        throw new UnimplementedException();
    }

    public int getSensorTemp() {
        return sensorTemp;
    }

}
