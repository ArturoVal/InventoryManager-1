package team.moxie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {
  private static OrderDbDriver orderDriver;
  private static invDbDriver productDriver;
  //private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
  public static void main(String[] args)
    throws InterruptedException, SQLException {
    Properties props = getProperties("target\\classes\\config.properties");

    assert props != null;
    String host = props.getProperty("emailUrl");
    String mailStoreType = props.getProperty("serverType");
    String username = props.getProperty("gmailName");
    String password = props.getProperty("gmailPass");

    ReceiveMail receiver;
    ArrayList<OrderDbEntry> messages = null;

    orderDriver =
      new OrderDbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass")
      );
    productDriver = new invDbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass"));

    // This take a while, if it takes too long just comment it temporarily

    companyInfo info = new companyInfo();
    info.dailyReport(orderDriver,productDriver);

    while (true) {
      try {
        receiver = new ReceiveMail(host, mailStoreType, username, password);
        messages = receiver.getMessages();
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      assert messages != null;
      for (OrderDbEntry message : messages) {
        System.out.println(message);
        System.out.println(message.getDate());

        System.out.println("Placing the order...");
        orderDriver.createEntry(
          message.getEmail(),
          message.getDate(),
          message.getProductID(),
          message.getQuantity(),
          "processing"
        );
        System.out.println("Order placed.");
        //send email confirmation
        EmailSend.sendConfirmation(message.getEmail(), message.getQuantity(), message.getProductID(), message.getLocation(),props);
      }
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
