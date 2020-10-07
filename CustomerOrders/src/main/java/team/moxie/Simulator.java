package team.moxie;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import static team.moxie.Main.getProperties;

public class Simulator {
    private static orderDbDriver orderDriver;
    public static void main(String[] args) throws SQLException {
        Properties props = getProperties("target\\classes\\config.properties");
        orderDriver =
                new orderDbDriver(
                        props.getProperty("ip"),
                        props.getProperty("port"),
                        props.getProperty("dbname"),
                        props.getProperty("username"),
                        props.getProperty("pass")
                );
        try {
            //Set up scanner and hardcode csv file with orders
            Scanner in = new Scanner(new FileInputStream("customer_orders_A_team3.csv"));
            String line;
            in.nextLine();
            int orderNumber = 0;
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
                Date date1 = new Date(year, month, day);
                String email = data[1];
                String address = data[2];
                String id = data[3];
                String amount = data[4];
                int quantity = Integer.parseInt(amount);
                //call orderDbEntry to create a new entry
                //orderDbEntry entry = new orderDbEntry(date1, email, address, id, quantity);
                String proc = "Processing";
                orderDbEntry temporary = new orderDbEntry(date1, email, address, id, quantity);
                System.out.println(temporary);
                int alpha = orderDriver.createEntry(email, date1, id, quantity, proc);
                System.out.println(alpha);
                //System.out.println(entry);
                orderNumber ++;
                System.out.println("Order Number: " + orderNumber + "\n");
            }
            in.close();
        }
        catch (IOException ex) {
            System.out.println("Error: " + ex);
        }
    }
}