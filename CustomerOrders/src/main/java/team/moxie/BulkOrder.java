package team.moxie;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class BulkOrder {
    private static OrderDbDriver orderDriver;
    private static invDbDriver invDbDriver;

    public BulkOrder(OrderDbDriver orderDri, invDbDriver invDri) {
        this.orderDriver = orderDri;
        this.invDbDriver = invDri;
        System.out.println("Bulk order initilized");
    }

    public static void run() throws SQLException {
        try {
            //Ask for bulk order csv file name
            System.out.println("Enter name of file for bulk order: ");
            Scanner in = new Scanner(System.in);
            String csvFile = in.nextLine();

            in = new Scanner(
                    new FileInputStream(csvFile)
            );
            String line;
            in.nextLine();
            int orderNumber = 0;

            // Use the order processor
            System.out.println("setting up order processor");
            OrderProcessor processor = new OrderProcessor(invDbDriver, orderDriver);

            LinkedList<OrderDbEntry> allOrders = new LinkedList<>();

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
            long begin = System.nanoTime();
            processor.processOrders(processor.loadOrders(allOrders));
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
