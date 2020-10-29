package team.moxie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;

public class Main {
  private static OrderDbDriver orderDriver;
  private static invDbDriver productDriver;

  public static void main(String[] args)
    throws InterruptedException, SQLException {
    Properties props = getProperties("target\\classes\\config.properties");
    //java.util.Date date = Calendar.getInstance().getTime();

    orderDriver =
      new OrderDbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass")
      );
    productDriver =
      new invDbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass")
      );

    // This take a while, if it takes too long just comment it temporarily
    //companyInfo info = new companyInfo();
    //info.dailyReport(orderDriver, productDriver);

    /**
     * checks email server every 5 seconds
     * handles all email orders and cancellations
    **/
    while (true) {
      MailOrder.checkMail(orderDriver,  props);
      TimeUnit.SECONDS.sleep(5);
    }
  }

  public static Properties getProperties(String fileName) {
    String dir = System.getProperty("user.dir");
    System.out.println("Reading properties from: " + dir + fileName);
    InputStream tmpFile;
    try {
      tmpFile = new FileInputStream(fileName);
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