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
  private static DbDriver productDriver;
  private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
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
    productDriver = new DbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass"));
    

    while (true) {
      try {
        receiver = new ReceiveMail(host, mailStoreType, username, password);
        messages = receiver.getMessages();
      } catch (Exception e) {
        e.printStackTrace();
      }
      // This take a while, if it takes too long just comment it temporarily
      dailyReport();
  
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
        EmailSend.SMTP_setup();
        String cx = "Hi, "+message.getEmail()+"! Thank you for your order of "+message.getQuantity()+" of "+
                message.getProductID() +"\nYour order will be shipped to: "+ message.getLocation() ;
        try {
        EmailSend.createEmail(message.getEmail(),"Order confirmation", cx);
          EmailSend.sendEmail(props); // pass in the props object so it has access to username and password
        } catch (MessagingException e) {
          e.printStackTrace();
        }

        //updateDB(messages);
      }
      TimeUnit.SECONDS.sleep(5);
    }
  }
  
  public static void dailyReport () {
    System.out.println("----- Daily report -----");
    double currentTotal = sumAssets();
    System.out.println(" Current total assets: " + CURRENCY.format( currentTotal));
    int num = numOrders();
    System.out.println("There are currently " + num + " orders");
    double orderTotal = totalOrderCost();
    System.out.println("The orders total " + CURRENCY.format(orderTotal));
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

  public static void updateDB(ArrayList<OrderDbEntry> entries)
    throws SQLException {

    System.out.println("Submitting the order.");
    for (OrderDbEntry entry : entries) {
      int result = orderDriver.createEntry(
        entry.getEmail(),
        entry.getDate(),
        entry.getProductID(),
        entry.getQuantity(),
        "processing"
      );

      System.out.println("Submit result: " + result);
    }
  }
  public static double sumAssets() {
    LinkedList <dbEntry> products = productDriver.returnAllEntries();
    double value = 0;
    //System.out.println(products.size());
    for (dbEntry product: products){
      double cost = product.getQuantity() * product.getWholesalePrice();
      value += cost;
      
    }
    return value;
  }
  public static int numOrders(){
    LinkedList<OrderDbEntry> orders = orderDriver.returnAllEntries();
    return orders.size();
  }
  public static double totalOrderCost(){
    LinkedList <OrderDbEntry> orders = orderDriver.returnAllEntries();
    double value = 0;
    int cnt = 0;
    for (OrderDbEntry order : orders){
      int quantity = order.getQuantity();
      String productid = order.getProductID();
      dbEntry product = productDriver.searchById(productid);
      
      if(product != null){
        double cost = quantity *product.getSalePrice();
        value += cost;
      }
      if((++cnt) % 100 == 0){
        System.out.println("Summing cost of order " + cnt + "/" + orders.size());
      }
    }
    return value;
  }
  
}
