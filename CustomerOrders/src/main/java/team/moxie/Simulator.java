package team.moxie;

import static team.moxie.Main.getProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
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
      //Ask for bulk order csv file name
      System.out.println("Enter name of file for bulk order: ");
      Scanner in = new Scanner(System.in);
      String csvFile = in.nextLine();

      in = new Scanner(
        new FileInputStream("customer_orders_A_team3.csv")
      );
      String line;
      in.nextLine();
      int orderNumber = 0;

      // Use the order processor

      OrderProcessor processor = new OrderProcessor(invDbDriver, orderDriver);

      //go through each line and create the orderDbEntry object
      while (in.hasNextLine()) {
        line = in.nextLine();
        String data[] = line.split(",");
        //split date into 3 strings
        String date[] = data[0].split("-");
        //convert strings into integers

        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        //arturo - in this class Zero is january so have to decrease month by one
        //otherwise 2020-01-01 is read as February 1, 2020
        month--;
        int day = Integer.parseInt(date[2]);


        //create date object using the integers
        // Dustin - Using the Calender object instead as the Date(year,month,day) is deprecated
        Calendar calDate = Calendar.getInstance();

        //arturo - removed the hour of day, minute, and second since we don't use them.
        calDate.set(year, month, day);

        Date date1 = calDate.getTime();

        String email = data[1];
        String address = data[2];
        String id = data[3];
        String amount = data[4];
        int quantity = Integer.parseInt(amount);
        java.sql.Date sqlDate = new java.sql.Date(date1.getTime());
        

        String sql = "INSERT INTO Sim_Orders (date, cust_email, cust_location, product_id, product_quantity, status) VALUES (?)";
        PreparedStatement statement = orderDriver.getDbConn().prepareStatement(sql);
        statement.setDate(1, sqlDate);
        statement.setString(2, email);
        statement.setString(3, address);
        statement.setString(4, id);
        statement.setInt(5, quantity);
        statement.setString(6, "Processing");




        orderNumber++;
      }
      in.close();
      System.out.println("Order Number: " + orderNumber + "\n");
      long begin = System.nanoTime();
      long complete = System.nanoTime();

      double elapsed = (double) (complete - begin) / 1000000000;

      BigDecimal d = new BigDecimal(elapsed);
      String result = d.toPlainString();

      System.out.println("Done in : " + result + " s");
    } catch (IOException ex) {
      System.out.println("Error: " + ex);
    }
  }
}
