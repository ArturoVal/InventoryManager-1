package team.moxie;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderProcessor {
  invDbDriver invDriver;
  OrderDbDriver orderDriver;

  // enum to express success of processing
  enum complete {
    FAILED,
    SUCCESS,
    ERROR,
    NO_ORDERS
  }

  public OrderProcessor(invDbDriver invDriver, OrderDbDriver orderDriver) {
    this.invDriver = invDriver;
    this.orderDriver = orderDriver;
  }

  /** Takes a list of order objects and adds them to the table, also gets the generated
   * IDs and adds them to the list and returns it
   *
   * @param ordersList a list of orders to be added to the orders table
   * @return the same list but with the generated keys added
   * @see OrderDbEntry
   */
  public LinkedList<OrderDbEntry> loadOrders(
    LinkedList<OrderDbEntry> ordersList
  ) {
    Connection dbConn = orderDriver.getDbConn();

    try {
      int curr = 0;

      // A string builder to create the query
      StringBuilder builderOrders = new StringBuilder();

      // This is the sql query that inserts rows into the database
      builderOrders.append("INSERT INTO orders VALUES \n");

      for (OrderDbEntry order : ordersList) {
        // The date needs to be formatted in plain text to be understood by MySql
        // This takes a SimpleDateFormat from java.text in order to format the date properly
        String sqlDateString = new SimpleDateFormat("yyyy-MM-dd 00:00:00")
        .format(order.getDate());

        // If it is the first one, it doesnt need a comma at the beginning
        if (curr == 0) {

          // This part is a row in the database, it will be appended after VALUES and added all at once
          String part =
            "('" +
            sqlDateString +
            "', '" +
            order.getEmail() +
            "', '" +
            order.getLocation() +
            "','" +
            order.getProductID() +
            "','" +
            order.getQuantity() +
            "','processing','" +
            order.getID() +
            "')\n";

          // append the string builder with this entry
          builderOrders.append(part);
        } else {
          String part =
            ",('" +
            sqlDateString +
            "', '" +
            order.getEmail() +
            "', '" +
            order.getLocation() +
            "','" +
            order.getProductID() +
            "','" +
            order.getQuantity() +
            "','processing','" +
            order.getID() +
            "')\n";

          // append the string builder with this entry
          builderOrders.append(part);
        } // Else
        curr++;
      } // For order in orderslist

      // convert the string builder to a string. This is now the query that contains all the rows formatted
      // into a sql query
      String insertOrders = builderOrders.toString();

      // Create the statement using the database connection and execute the query
      Statement statement = dbConn.createStatement();
      // Tell the database to return the keys it generated, this is so the orderentries can be updated to contain the
      // generated IDs it needs
      statement.execute(insertOrders, Statement.RETURN_GENERATED_KEYS);

      // Use the generated keys from the returned result set and set the ID's in all of the entries
      // note that it uses the iterator.next() exposed by the list so there it is O(n) complexity
      int i = 0;
      ResultSet resultSet = statement.getGeneratedKeys();
      while (resultSet.next()) {
        int anInt = resultSet.getInt(1);
        ordersList.iterator().next().setID(anInt);
        i++;
      }

      // return the updated list of orders (contains the generated keys)
      return ordersList;
    } catch (SQLException throwables) {
      throwables.printStackTrace();
      return null;
    }
  }

  /** This is where the orders are actually processed and updates on the tables
   *
   * This is roughly about 13 entries per second, which is very slow, we should research how to speed this up,
   * To do the entire table takes about 2 minutes
   * Note that the orderslist must have been updated to include the generated keys from the database
   * @param ordersList a list of OrderDbEntry
   * @return returns an enum to describe the success
   * @throws SQLException something real bad happened with the sql connections
   * @see OrderDbEntry
   */
  public complete processOrders(LinkedList<OrderDbEntry> ordersList)
    throws SQLException {
    // Make sure the drivers are initialized
    if (invDriver == null || orderDriver == null) {
      throw new NullPointerException("invDriver or orderDriver is null");
    }
    // Make sure there are orders in the list
    if (ordersList.size() == 0) {
      return complete.NO_ORDERS;
    }

    // Convert the list to a map for faster access
    HashMap<String, dbEntry> entryHashMap = convertToMap(
      invDriver.returnAllEntries()
    );

    // Technically this can be done with 1 connection how it is now
    // But it might be faster to use both connections in parrallel so I leave that here
    Connection dbConnInv = invDriver.getDbConn();
    Connection dbConnOrder = orderDriver.getDbConn();

    // Setting up the string builders and queries to append the values to
    StringBuilder builderOrders = new StringBuilder();
    StringBuilder builderInv = new StringBuilder();
    //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500'),
    builderOrders.append("INSERT INTO tmp_orders VALUES \n");
    //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500'),
    builderInv.append("INSERT INTO tmp_inv VALUES \n");


    ///////////////////////////////////////// This is the loop that ////////////////////////////////////////////////////
    /////////////////////////////////////////   does everything     ////////////////////////////////////////////////////
    // create a hashmap to add the products to
    HashMap<String, dbEntry> setOfProducts = new HashMap<>();
    int i = 0;

    for (OrderDbEntry entry : ordersList) {
      dbEntry tmpInvEntry = entryHashMap.get(entry.getProductID());
      // find out if the product is already been ordered yet
      boolean containsProduct = setOfProducts.containsKey(entry.getProductID());

      int quantityDiff = tmpInvEntry.getQuantity() - entry.getQuantity();

      // if it hasn't then the product needs to be added to the map
      // this section is just for the inventory database, the order table is updated separately
      if (!containsProduct) {
        tmpInvEntry.setQuantity(quantityDiff);
        setOfProducts.put(entry.getProductID(), tmpInvEntry);
      } else { // if it has then just update the entry that is already in the map
        dbEntry tmp = setOfProducts.get(entry.getProductID());
        int quantity = tmp.getQuantity();
        tmp.setQuantity(quantityDiff);
      }

      // append to the end of the builder
      appendOrderSQLPart(builderOrders, i, entry, quantityDiff);
      i++;
    }

    // a foreach using a lambda, the atomicinteger is just an integer that can function inside the lambda
    // this builds the inventory string
    AtomicInteger finalI = new AtomicInteger(0);
    setOfProducts.forEach(
      (key, value) -> {
        buildInvString(builderInv, finalI.get(), value, value.getQuantity());
        finalI.getAndIncrement();
      }
    );
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // convert both of the builders (the queries) into strings
    String createOrderTable = builderOrders.toString();
    String createInvTable = builderInv.toString();

    // Just to time the operation
    long start = System.nanoTime();

    // Create the statement that will be used to perform the queries
    Statement statement = dbConnOrder.createStatement();

    // insert into the temp tables
    statement.execute(createOrderTable);
    statement.execute(createInvTable);

    // Execute the joins, this is where the main tables are actually updating using the temporary tables
    // These could possibly be done in parallel by using 2 different connections, but that would
    // require the use of a connection pool
    System.out.println("Executing join on orders");
    statement.execute(
      "UPDATE orders INNER JOIN tmp_orders ON orders.orderID = tmp_orders.orderID SET orders.status = tmp_orders.status"
    );
    System.out.println("Executing join on inventory");
    statement.execute(
      "UPDATE inventory INNER JOIN tmp_inv ON inventory.product_id = tmp_inv.product_id SET inventory.quantity = tmp_inv.quantity"
    );

    // clear the temp tables
    statement.execute("TRUNCATE TABLE tmp_orders");
    statement.execute("TRUNCATE TABLE tmp_inv");

    // Just to time the operation and print out how long it took
    long end = System.nanoTime();
    double elapsed = (double) (end - start) / 1000000000;

    BigDecimal d = new BigDecimal(elapsed);
    String result = d.toPlainString();

    System.out.println("Done in : " + result + " s");

    return complete.SUCCESS;
  }

  /** Appends the order query with the entry
   *
   * @param builderOrders string builder for the orders
   * @param i the current position
   * @param entry the order object
   * @param quantityDiff the difference in quantity, not actually used I dont think
   */
  public void appendOrderSQLPart(
    StringBuilder builderOrders,
    int i,
    OrderDbEntry entry,
    int quantityDiff
  ) {
    String sqlDateString = new SimpleDateFormat("yyyy-MM-dd 00:00:00")
    .format(entry.getDate());
    buildOrderString(builderOrders, i, entry, sqlDateString);
  }


  /**
   *
   * @param builderOrders the string builder to add to
   * @param i the current position
   * @param entry the order object
   * @param sqlDateString the date formatted as a string that can be understood by MySql
   */
  private void buildOrderString(
    StringBuilder builderOrders,
    int i,
    OrderDbEntry entry,
    String sqlDateString
  ) {
    //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500')
    String part;
    if (i == 0) {
      part =
        "('" +
        sqlDateString +
        "', '" +
        entry.getEmail() +
        "', '" +
        entry.getLocation() +
        "','" +
        entry.getProductID() +
        "','" +
        entry.getQuantity() +
        "','complete','" +
        entry.getID() +
        "')\n";
    } else {
      part =
        ",('" +
        sqlDateString +
        "', '" +
        entry.getEmail() +
        "', '" +
        entry.getLocation() +
        "','" +
        entry.getProductID() +
        "','" +
        entry.getQuantity() +
        "','complete','" +
        entry.getID() +
        "')\n";
    }
    builderOrders.append(part);
  }

  /** This method appends the parts to the inventory builder
   *
   * @param builderOrders the string builder to add to
   * @param i the current position
   * @param entry the inventory object
   * @param quantity the quantity to remove
   */
  private void buildInvString(
    StringBuilder builderOrders,
    int i,
    dbEntry entry,
    int quantity
  ) {
    //INSERT INTO tmp_inv VALUES ('product_id','quantity','wholesale_cost','sale_price','supplier_id');
    String part;
    if (i == 0) {
      part =
        "('" +
        entry.getId() +
        "', '" +
        quantity +
        "','" +
        entry.getWholesalePrice() +
        "','" +
        entry.getSalePrice() +
        "','" +
        entry.getSupplierId() +
        "')\n";
    } else {
      part =
        ",('" +
        entry.getId() +
        "', '" +
        quantity +
        "','" +
        entry.getWholesalePrice() +
        "','" +
        entry.getSalePrice() +
        "','" +
        entry.getSupplierId() +
        "')\n";
    }
    builderOrders.append(part);
  }

  /** Converts a list of entries to a map using the ID as the key
   *
   * @param entries a linked list of entries to convert to a map
   * @return a hashmap containing all the entries from the list
   */
  public HashMap<String, dbEntry> convertToMap(LinkedList<dbEntry> entries) {
    long start = System.nanoTime();
    HashMap<String, dbEntry> entryHashMap = new HashMap<>();
    for (dbEntry entry : entries) {
      entryHashMap.put(entry.getId(), entry);
    }
    long end = System.nanoTime();
    return entryHashMap;
  }
}
