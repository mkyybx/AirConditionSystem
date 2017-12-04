package AC;

import Info.ModeInfo;
import Info.SensorTempInfo;
import XML.XMLizableHandler;
import XML.XMLizableParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Ice on 5/18/2017.
 */
public class TEST extends HttpServlet{
    public static void main(String[] args) {
        if ((Boolean)null) {
            System.out.println("111");
        }
        System.out.println("222");


/*
        XMLizableParser parser = XMLizableParser.getInstance();
        parser.setModeInfoHandler(new XMLizableHandler<ModeInfo>() {
            @Override
            public void onParseComplete(ModeInfo payload, int num, boolean isWebSide) {
                System.out.println(payload.isHeating());
            }
        });
        parser.setSensorTempInfoHandler(new XMLizableHandler<SensorTempInfo>() {
            @Override
            public void onParseComplete(SensorTempInfo payload, int num, boolean isWebSide) {
                System.out.println(payload.getSensorTemp());
            }
        });
        try {
            //Scanner input = new Scanner(System.in);
            //System.out.print(input.nextLine());
            while (true) {

                parser.parse(new FileInputStream(new File("D:\\workspace\\AirConditioner\\test.xml")),1,true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
