/**
 * Created by Layne on 2017/5/20.
 */
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Map;

class DatabaseHandler {
    protected Connection conn;

    protected DatabaseHandler(String name){
        conn = null;
        name = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\" + name;
        System.out.println(name);
        try{
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(name);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        //System.out.println("Success!");
    }

    protected void close(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class RoomInfo{
    public int Client_No;
    public String Name;
    public String Password;
}

class MyDate{
    public int month;
    public int week;
    public int day;

    public MyDate(LocalDateTime now){
        month = now.getMonthValue();
        week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        day = now.getDayOfMonth();
    }

    public MyDate(int month, int week, int day){
        this.month = month;
        this.week = week;
        this.day = day;
    }
}

class Log{                              // struct for store log info
    public int Client_No;
    public String Name;
    public MyDate startDate;
    public MyDate endDate;
    public int netDuration = 0;
    public double energy = 0;
    public double fee = 0;
    public int level = 1;
    public int startTemp = 26;
    public int endTemp = 26;
    public boolean checkOut = false;

    public void updateEnergyAndFare(){
        double rate = 1;
        if (level != 2)
            rate = level == 1 ? 0.8 : 1.3;
        energy = netDuration * rate;
        fee = energy * 5;
    }
}

public class RoomInfoHandler extends DatabaseHandler{
    public RoomInfoHandler(String name){
        super(name);
    }

    public void insert(ArrayList<RoomInfo> infos) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO roomInfo VALUES(?, ?, ?)");
        for(RoomInfo info: infos){
            prep.setInt(1, info.Client_No);
            prep.setString(2, info.Name);
            prep.setString(3,info.Password);
            prep.addBatch();
        }
        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
        prep.close();
    }

    public ArrayList<RoomInfo> select(int client_no) throws SQLException {
        ArrayList<RoomInfo> ret = new ArrayList<>();

        PreparedStatement prep = conn.prepareStatement("SELECT * FROM roomInfo WHERE Client_No = ?");
        prep.setInt(1, client_no);
        ResultSet res = prep.executeQuery();
        while(res.next()){
            RoomInfo info = new RoomInfo();
            info.Client_No = res.getInt("Client_No");
            info.Name = res.getString("Name");
            info.Password = res.getString("Password");
            ret.add(info);
        }

        res.close();
        prep.close();
        return ret;
    }
}


class LogHandler extends DatabaseHandler{
    private PreparedStatement stmt;
    public LogHandler(String name){
        super(name);
    }

    public void insert(ArrayList<Log> logs) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO log VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        for(Log log: logs){
            prep.setInt(1, log.Client_No);
            prep.setString(2, log.Name);
            prep.setInt(3, log.startDate.month);
            prep.setInt(4, log.startDate.week);
            prep.setInt(5, log.startDate.day);
            prep.setInt(6, log.endDate.month);
            prep.setInt(7, log.endDate.week);
            prep.setInt(8, log.endDate.day);
            prep.setInt(9, log.netDuration);
            prep.setDouble(10, log.energy);
            prep.setDouble(11, log.fee);
            prep.setInt(12, log.level);
            prep.setInt(13, log.startTemp);
            prep.setInt(14, log.endTemp);
            prep.setBoolean(15, log.checkOut);
            prep.addBatch();
        }
        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
        prep.close();
    }

    public ArrayList<Log> select(int client_no) throws SQLException {
        ArrayList<Log> ret = new ArrayList<>();

        PreparedStatement prep = conn.prepareStatement("SELECT * FROM log WHERE Client_No = ?");
        prep.setInt(1, client_no);
        ResultSet res = prep.executeQuery();
        while(res.next()){
            Log log = new Log();
            log.Client_No = res.getInt("Client_No");
            log.Name = res.getString("Name");
            log.startDate = new MyDate(res.getInt("startMonth"), res.getInt("startWeek"),
                                        res.getInt("startDay"));
            log.endDate = new MyDate(res.getInt("endMonth"), res.getInt("endWeek"), res.getInt("endDay"));
            log.netDuration = res.getInt("netDuration");
            log.energy = res.getDouble("energy");
            log.fee = res.getDouble("fee");
            log.level = res.getInt("level");
            log.startTemp = res.getInt("startTemp");
            log.endTemp = res.getInt("endTemp");
            log.checkOut = res.getBoolean("checkOut");
            ret.add(log);
        }

        res.close();
        prep.close();
        return ret;
    }

    public void setCheckOut(int client_no) {
        try {
            PreparedStatement prep = conn.prepareStatement("UPDATE log SET checkOut = 1 WHERE Client_No = ?");
            prep.setInt(1, client_no);
            prep.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    // todo
    public void update(String name, Map<String, Object> conditionSet, Map<String, Object> valueSet){
        try {
            String sql = "UPDATE " + name + " SET ";
            for(int i = 0; i < valueSet.size(); i++){
                sql += i == valueSet.size() ? "? = ?" : "? = ?, ";
            }
            sql += " Where ";
            for(int i = 0; i < conditionSet.size(); i++){
                sql += i == conditionSet.size() ? "? = ?" : "? = ?, ";
            }
            stmt = conn.prepareStatement(sql);

            int i = 1;
            for(Map.Entry<String, Object> e: valueSet.entrySet()) {
                mapValue(e, i);
                i += 2;
            }
            for (Map.Entry<String, Object> e: valueSet.entrySet()){
                mapValue(e, i);
                i += 2;
            }

            stmt.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    // todo
    private void mapValue(Map.Entry<String, Object> e, int i) throws SQLException{
        stmt.setString(i, e.getKey());
        if(e.getValue() instanceof Integer)
            stmt.setInt(i+1, (int) e.getValue());
        else if(e.getValue() instanceof String)
            stmt.setString(i+1, (String) e.getValue());
        else if(e.getValue() instanceof Double)
            stmt.setDouble(i+1, (Double) e.getValue());
        else if(e.getValue() instanceof Boolean)
            stmt.setBoolean(i+1, (Boolean) e.getValue());
    }
}
