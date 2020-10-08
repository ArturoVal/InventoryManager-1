package team.moxie;

import static team.moxie.Main.getProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class Simulator {
  private static OrderDbDriver orderDriver;
  private static invDbDriver invDbDriver;

  public static void main(String[] args) throws SQLException {
    Properties props = getProperties("target\\classes\\config.properties");
    orderDriver =
      new OrderDbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass")
      );

    invDbDriver =
      new invDbDriver(
        props.getProperty("ip"),
        props.getProperty("port"),
        props.getProperty("dbname"),
        props.getProperty("username"),
        props.getProperty("pass")
      );

    try {
      //Set up scanner and hardcode csv file with orders
      Scanner in = new Scanner(
        new FileInputStream("customer_orders_A_team3.csv")
      );
      String line;
      in.nextLine();
      int orderNumber = 0;

      // Use the order processor

      OrderProcessor processor = new OrderProcessor(invDbDriver, orderDriver);

      LinkedList<OrderDbEntry> allOrders = new LinkedList<>();

      //go through each line and create the orderDbEntry object
      while (in.hasNextLine()) {
        line = in.nextLine();
        String data[] = line.split(",");
        //split date into 3 strings
        String date[] = data[0].split("-");
        //convert strings into integers
        int year = Integer.parseInt(date[2]);
        int month = Integer.parseInt(date[0]);
        int day = Integer.parseInt(date[1]);

        //create date object using the integers
        // Dustin - Using the Calender object instead as the Date(year,month,day) is deprecated
        Calendar calDate = Calendar.getInstance();
        calDate.set(year, month, day, 0, 0, 0);
        Date date1 = calDate.getTime();

        String email = data[1];
        String address = data[2];
        String id = data[3];
        String amount = data[4];
        int quantity = Integer.parseInt(amount);
        //call orderDbEntry to create a new entry
        OrderDbEntry entry = new OrderDbEntry(
          date1,
          email,
          address,
          id,
          quantity,
          "processing"
        );
        allOrders.add(entry);

        orderNumber++;
      }
      in.close();
      System.out.println("Order Number: " + orderNumber + "\n");
      processor.loadOrders(allOrders);
      processor.processOrders(allOrders);
      System.out.println("Done.");
    } catch (IOException ex) {
      System.out.println("Error: " + ex);
    }
  }
}
