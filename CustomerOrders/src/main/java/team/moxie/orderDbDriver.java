package team.moxie;

import java.sql.*;
import java.util.Date;
import java.util.LinkedList;

/**
 * A driver for handling all the SCUD operations for the inventory database
 *
 * @author Dustin
 */
public class orderDbDriver {
  /**
   * The connection to the database
   * @see DriverManager
   */
  private final Connection dbConn;

  /**
   * Constructor for DbDriver
   *
   * @param hostIP        IP of the database host
   * @param hostPort      Port of the database
   * @param databaseName  Name of the database
   * @param dbUser        The username to be used
   * @param dbPass        The password for the user
   * @throws SQLException Thrown if the database connection fails
   */

  public orderDbDriver(
    String hostIP,
    String hostPort,
    String databaseName,
    String dbUser,
    String dbPass
  )
    throws SQLException {
    // Create the database connection

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (Exception ex) {
      System.out.println(ex.toString());
    }
    String connString = buildConnString(hostIP, hostPort, databaseName);
    System.out.println("Connecting with the URL: " + connString);
    this.dbConn = DriverManager.getConnection(connString, dbUser, dbPass);
  }

  /**
   * A helper function to format the info into a connection string
   *
   * @param hostIP        IP of the database host
   * @param hostPort      Port of the database
   * @param databaseName  Name of the database
   * @return A formated connection string
   * @see orderDbDriver
   * @see DriverManager
   */
  private String buildConnString(
    String hostIP,
    String hostPort,
    String databaseName
  ) {
    return "jdbc:mysql://" + hostIP + ":" + hostPort + "/" + databaseName;
  }

  /**
   * Creates an entry in the database with the given data
   *
   * @param id             Product ID
   * @param quantity       Quantity of the product
   * @param wholesalePrice Wholesale price of the product
   * @param salePrice      Sale price of the product
   * @param supplierId     ID of the supplier for the item
   * @return Boolean for whether the operation was successful
   */
  public int createEntry(
    String email,
    Date date,
    String productID,
    int quantity,
    String status
  ) {
    // cannot be longer than 12 char
    if (productID.length() > 12) return 0;

    if (quantity < 0) return 0;

    try {
      //create and execute the statement
      PreparedStatement statement = dbConn.prepareStatement(
        "insert into inv.orders(email, date, productid, quantity, status) values (?,?,?,?,?)"
      );

      java.sql.Date sqlDate = new java.sql.Date(date.getTime());

      statement.setString(1, email);
      statement.setDate(2, sqlDate);
      statement.setString(3, productID);
      statement.setInt(4, quantity);
      statement.setString(5, status);

      //insert into inv.inventory(product_id, quantity, wholesale_cost, sale_price, supplier_id) values ('2FT57YS7CM97',112,112.0,112.0, 'YTCMSBPA')

      return statement.executeUpdate();
    } catch (Exception ex) {
      // Print out the reason and return null
      System.out.println(ex.toString());
      return 0;
    }
  }

  /**
   * Deletes the entry with the given ID
   *
   * @param id The id of the product
   * @return Boolean for whether the operation was successful
   */
  public boolean deleteEntry(String id) {
    // cannot be longer than 12 char
    if (id.length() > 12) return false;

    try {
      //create and execute the statement
      Statement statement = dbConn.createStatement();

      //delete from inv.inventory where product_id='2FT57YS7CM97';
      int result = statement.executeUpdate(
        String.format("DELETE FROM inv.inventory WHERE product_id='%s'", id)
      );

      return result == 1;
    } catch (Exception ex) {
      // Print out the reason and return null
      System.out.println(ex.toString());
      return false;
    }
  }

  /**
   * Updates the entry with given ID and info
   *
   * @param id             Product ID
   * @param quantity       Quantity of the product
   * @param wholesalePrice Wholesale price of the product
   * @param salePrice      Sale price of the product
   * @param supplierId     ID of the supplier for the item
   * @return boolean whether the operation completed successfully
   */
  public boolean updateEntry(
    String id,
    int quantity,
    double wholesalePrice,
    double salePrice,
    String supplierId
  ) {
    // cannot be longer than 12 char
    if (id.length() > 12 || supplierId.length() > 12) return false;
    // verify that the values are not negative
    if (quantity < 0 || wholesalePrice < 0 || salePrice < 0) return false;

    try {
      //create and execute the statement
      Statement statement = dbConn.createStatement();
      int result = statement.executeUpdate(
        String.format(
          "update inv.inventory set quantity = %d, wholesale_cost = %s, sale_price = %s where product_id = '%s'",
          quantity,
          wholesalePrice,
          salePrice,
          id
        )
      );

      return result == 1;
    } catch (Exception ex) {
      // Print out the reason and return null
      System.out.println(ex.toString());
      return false;
    }
  }
}
