package Info;

import AC.UnimplementedException;
import XML.XMLizable;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ice on 5/18/2017.
 */
public class FareInfo extends XMLizable {

    /*说不定可以是int*/
    private double fare;
    private double Energy;

    public FareInfo(double fare, double energy) {
        this.fare = fare;
        Energy = energy;
    }

    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        Document document = DocumentHelper.createDocument();
        Element fareInfo = document.addElement("Fare_Info");
        fareInfo.addElement("Fare").setText(String.valueOf(fare));
        fareInfo.addElement("Energy").setText(String.valueOf(Energy));
        OutputFormat format = OutputFormat.createPrettyPrint();
        new XMLWriter(out, format).write(document);
    }

    public double getFare() {
        return fare;
    }

    public double getEnergy() {
        return Energy;
    }
}
