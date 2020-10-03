package team.moxie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;

public class Main {
  private static orderDbDriver orderDriver;

  public static void main(String[] args)
    throws InterruptedException, SQLException {
    Properties props = getProperties("target\\classes\\config.properties");

    assert props != null;
    String host = props.getProperty("emailUrl");
    String mailStoreType = props.getProperty("serverType");
    String username = props.getProperty("gmailName");
    String password = props.getProperty("gmailPass");

    ReceiveMail receiver;
    ArrayList<orderDbEntry> messages = null;

    orderDriver =
      new orderDbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass")
      );

    orderDriver.createEntry(
      "testemail@gmail.com",
      new Date(),
      "fakeproduct",
      12,
      "processing"
    );

    while (true) {
      try {
        receiver = new ReceiveMail(host, mailStoreType, username, password);
        messages = receiver.getMessages();
      } catch (Exception e) {
        e.printStackTrace();
      }

      assert messages != null;
      for (orderDbEntry message : messages) {
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
        EmailSend.SMTP_setup();
        String cx =
          "Hi, " +
          message.getEmail() +
          "! Thank you for your order of " +
          message.getQuantity() +
          " of " +
          message.getProductID() +
          "\nYour order will be shipped to: " +
          message.getShippingAddress();
        try {
          EmailSend.createEmail(message.getEmail(), "Order confirmation", cx);
          EmailSend.sendEmail(props); // pass in the props object so it has access to username and password
        } catch (MessagingException e) {
          e.printStackTrace();
        }
        //updateDB(messages);
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

  public static void updateDB(ArrayList<orderDbEntry> entries)
    throws SQLException {
    /*invDbDriver invDriver = new invDbDriver(
      "50.116.26.153",
      "3306",
      "inv",
      "team",
      "GJ&8YahAh%kS#i"
    );
     */

    System.out.println("Submitting the order.");
    for (orderDbEntry entry : entries) {
      int result = orderDriver.createEntry(
        entry.getEmail(),
        entry.getDate(),
        entry.getProductID(),
        entry.getQuantity(),
        "processing"
      );

      System.out.println("Submit result: " + result);
      //String currEntryID = entry.getProductID();
      //int currEntryQuantity = entry.getQuantity();
      /* if (invDriver.searchById(currEntryID) == null) {
        System.out.println("No match for Inventory ID " + currEntryID + ".");
      } else {
        dbEntry currentDBentry = invDriver.searchById(currEntryID);
        String id = currentDBentry.getId();
        int quantity = currentDBentry.getQuantity();
        double wholesalePrice = currentDBentry.getWholesalePrice();
        double salePrice = currentDBentry.getSalePrice();
        String supplierID = currentDBentry.getSupplierId();
        invDriver.updateEntry(
          id,
          (quantity - currEntryQuantity),
          wholesalePrice,
          salePrice,
          supplierID
        );
        System.out.println(invDriver.searchById(currEntryID) + "\n\n");
      }*/
    }
  }
}
