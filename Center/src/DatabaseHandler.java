/**
 * Created by Layne on 2017/5/20.
 */
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.*;

public class DatabaseHandler {
    protected Connection conn;
    protected PreparedStatement stmt;

    protected DatabaseHandler(String name){
        conn = null;
        name = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\" + name;
        // System.out.println(name);
        try{
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(name);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    protected void close(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected synchronized ArrayList<Object> selectColumnDistinct(String tableName, String columnName, HashMap<String, Object> conditionSet){
        ArrayList<Object> ret = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName;
            if (conditionSet.size() > 0)
                sql += " WHERE " + prepStr(conditionSet.keySet(), "and");

            stmt = conn.prepareStatement(sql);
            int i = 1;
            for(Object object: conditionSet.values()){
                mapValue(object, i++);
            }
            ResultSet res = stmt.executeQuery();
            while(res.next())
                ret.add(res.getObject(columnName));

            res.close();
            stmt.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return ret;
    }

    protected synchronized void update(String tableName, HashMap<String, Object> conditionSet, HashMap<String, Object> valueSet){
        assert valueSet.size() > 0 : "Condition set should not be empty.";
        try {
            String sql = "UPDATE " + tableName + " SET " + prepStr(valueSet.keySet(), ",");
            if(conditionSet.size() > 0)
                sql += " WHERE " + prepStr(conditionSet.keySet(), "and");
            stmt = conn.prepareStatement(sql);

            int i = 1;
            for(Object object: valueSet.values()) {
                mapValue(object, i++);
            }
            for(Object object: conditionSet.values()) {
                mapValue(object, i++);
            }

            stmt.executeUpdate();
            stmt.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    protected synchronized void delete(String tableName, HashMap<String, Object> conditionSet){
        try {
            String sql = "DELETE FROM " + tableName;
            if(conditionSet.size() > 0)
                sql += " WHERE " + prepStr(conditionSet.keySet(), "and");
            stmt = conn.prepareStatement(sql);

            int i = 1;
            for(Object object: conditionSet.values()) {
                mapValue(object, i);
                i++;
            }

            stmt.executeUpdate();
            stmt.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    protected synchronized void mapValue(Object object, int i) throws SQLException{
        if(object instanceof Integer)
            stmt.setInt(i, (int) object);
        else if(object instanceof String)
            stmt.setString(i, (String) object);
        else if(object instanceof Double)
            stmt.setDouble(i, (Double) object);
        else if(object instanceof Boolean)
            stmt.setBoolean(i, (Boolean) object);
        else
            throw new RuntimeException("No match type for " + object);
    }

    protected synchronized String prepStr(Set<String> set, String separator){
        String ret = "";
        int i = 1;
        for(String key: set){
            ret += key +" = ?";
            if(i++ != set.size())
                ret += " " + separator + " ";
        }
        return ret;
    }

    public synchronized static HashMap<String, Object> map(Object...objects){
        HashMap<String, Object> ret = new HashMap<>();
        for(int i = 0; i < objects.length; i += 2)
            ret.put((String) objects[i], objects[i+1]);
        return ret;
    }
}

class RoomInfo{                                         // records information of each room
    public int Client_No;
    public String Name;
    public String Password;
}

class RoomInfoHandler extends DatabaseHandler{          // specific database handler for handling roomInfo
    public RoomInfoHandler(String name){
        super(name);
    }

    public synchronized void insert(String tableName, ArrayList<RoomInfo> infos) throws SQLException {
        stmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES(?, ?, ?)");
        for(RoomInfo info: infos){
            stmt.setInt(1, info.Client_No);
            stmt.setString(2, info.Name);
            stmt.setString(3,info.Password);
            stmt.addBatch();
        }
        conn.setAutoCommit(false);
        stmt.executeBatch();
        conn.setAutoCommit(true);
        stmt.close();
    }

    public synchronized ArrayList<RoomInfo> select(String tableName, HashMap<String, Object> conditionSet) throws SQLException {
        ArrayList<RoomInfo> ret = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        if (conditionSet.size() > 0)
            sql += " WHERE " + prepStr(conditionSet.keySet(), "and");

        stmt = conn.prepareStatement(sql);
        int i = 1;
        for(Object object: conditionSet.values()){
            mapValue(object, i++);
        }
        ResultSet res = stmt.executeQuery();
        while(res.next()){
            RoomInfo info = new RoomInfo();
            info.Client_No = res.getInt("Client_No");
            info.Name = res.getString("Name");
            info.Password = res.getString("Password");
            ret.add(info);
        }

        res.close();
        stmt.close();
        return ret;
    }
}

class MyDate{
    public int month;
    public int week;
    public int day;

    @Override
    public String toString() {
        return month + "月-" + week + "周-" + day + "日";
    }

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

class LogHandler extends DatabaseHandler{
    public LogHandler(String name){
        super(name);
    }

    public synchronized void insert(String tableName, ArrayList<Log> logs) throws SQLException {
        stmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        for(Log log: logs){
            stmt.setInt(1, log.Client_No);
            stmt.setString(2, log.Name);
            stmt.setInt(3, log.startDate.month);
            stmt.setInt(4, log.startDate.week);
            stmt.setInt(5, log.startDate.day);
            stmt.setInt(6, log.endDate.month);
            stmt.setInt(7, log.endDate.week);
            stmt.setInt(8, log.endDate.day);
            stmt.setInt(9, log.netDuration);
            stmt.setDouble(10, log.energy);
            stmt.setDouble(11, log.fee);
            stmt.setInt(12, log.level);
            stmt.setInt(13, log.startTemp);
            stmt.setInt(14, log.endTemp);
            stmt.setBoolean(15, log.checkOut);
            stmt.addBatch();
        }
        conn.setAutoCommit(false);
        stmt.executeBatch();
        conn.setAutoCommit(true);
        stmt.close();
    }

    public synchronized ArrayList<Log> select(String tableName, HashMap<String, Object> conditionSet) throws SQLException {
        ArrayList<Log> ret = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        if(conditionSet.size() > 0)
            sql += " WHERE " + prepStr(conditionSet.keySet(), "and");
        System.out.println("sql : " + sql);

        stmt = conn.prepareStatement(sql);
        int i = 1;
        for(Object object: conditionSet.values()){
            mapValue(object, i);
        }
        ResultSet res = stmt.executeQuery();
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
        stmt.close();
        return ret;
    }
}
