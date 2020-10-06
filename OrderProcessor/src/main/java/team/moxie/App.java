package team.moxie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Order processor, will take all of the orders and attempt to complete them
 *
 */
public class App 
{
    public static void main( String[] args ) throws SQLException {
        Properties props = getProperties("\\target\\classes\\config.properties");
        assert props != null;
        OrderDbDriver orderDB = new OrderDbDriver(
                props.getProperty("ip"),
                props.getProperty("port"),
                props.getProperty("dbname"),
                props.getProperty("username"),
                props.getProperty("pass")
        );

        DbDriver invDB = new DbDriver(
                props.getProperty("ip"),
                props.getProperty("port"),
                props.getProperty("dbname"),
                props.getProperty("username"),
                props.getProperty("pass")
        );

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.set(2020,9,2,0,0,0);
        Date date = calendarDate.getTime();

        OrderDbEntry[] orders = orderDB.getOrdersByStatus("processing");

        OrderProcessor processor = new OrderProcessor(invDB,orderDB);
        processor.processOrders(orders);
    }

    public static Properties getProperties(String fileName) {
        String dir = System.getProperty("user.dir");
        System.out.println("Reading properties from: " + dir + fileName);
        InputStream tmpFile;
        try {
            tmpFile = new FileInputStream(dir+fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Properties prop = new Properties();
        try {
            prop.load(tmpFile);
            return prop;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
