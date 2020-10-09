package team.moxie;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;

public class OrderProcessor {
  invDbDriver invDriver;
  OrderDbDriver orderDriver;

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

  public LinkedList<OrderDbEntry> loadOrders(
    LinkedList<OrderDbEntry> ordersList
  ) {
    Connection dbConn = orderDriver.getDbConn();

    try {
      int curr = 0;

      StringBuilder builderOrders = new StringBuilder();
      builderOrders.append("INSERT INTO orders VALUES \n");

      for (OrderDbEntry order : ordersList) {
        String sqlDateString = new SimpleDateFormat("yyyy-MM-dd 00:00:00")
        .format(order.getDate());

        java.sql.Date sqlDate = new java.sql.Date(order.getDate().getTime());

        if (curr == 0) {
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

          builderOrders.append(part);
        }
        curr++;
      }

      String insertOrders = builderOrders.toString();

      Statement preparedStatement = dbConn.createStatement();
      preparedStatement.execute(insertOrders, Statement.RETURN_GENERATED_KEYS);

      int i = 0;
      ResultSet resultSet = preparedStatement.getGeneratedKeys();
      while (resultSet.next()) {
        int anInt = resultSet.getInt(1);
        ordersList.iterator().next().setID(anInt);
        i++;
      }

      return ordersList;
    } catch (SQLException throwables) {
      throwables.printStackTrace();
      return null;
    }
  }

  // This is roughly about 13 entries per second, which is very slow, we should research how to speed this up,
  // To do the entire table takes about 2 minutes
  public complete processOrders(LinkedList<OrderDbEntry> ordersList)
    throws SQLException {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    if (invDriver == null || orderDriver == null) {
      throw new NullPointerException("invDriver or orderDriver is null");
    }
    if (ordersList.size() == 0) {
      return complete.NO_ORDERS;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    HashMap<String, dbEntry> entryHashMap = convertToMap(
      invDriver.returnAllEntries()
    );

    Connection dbConnInv = invDriver.getDbConn();
    Connection dbConnOrder = orderDriver.getDbConn();

    ///////////////////////////////////////   Set up the string builders   /////////////////////////////////////////////

    StringBuilder builderOrders = new StringBuilder();
    StringBuilder builderInv = new StringBuilder();
    //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500'),
    builderOrders.append("INSERT INTO tmp_orders VALUES \n");
    //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500'),
    builderInv.append("INSERT INTO tmp_inv VALUES \n");
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////// This is the loop that ////////////////////////////////////////////////////
    /////////////////////////////////////////   does everything     ////////////////////////////////////////////////////
    int i = 0;
    for (OrderDbEntry entry : ordersList) {
      dbEntry tmpInvEntry = entryHashMap.get(entry.getProductID());

      int quantityDiff = tmpInvEntry.getQuantity() - entry.getQuantity();

      appendOrderSQLPart(builderOrders, i, entry, quantityDiff);
      appendInvSQLPart(builderInv, i, tmpInvEntry, quantityDiff);
      i++;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String createOrderTable = builderOrders.toString();
    String createInvTable = builderInv.toString();

    long start = System.nanoTime(); ////////////////////////////
    Statement statement = dbConnOrder.createStatement();

    statement.execute(createOrderTable);
    statement.execute(createInvTable);

    System.out.println("Executing join on orders");
    statement.execute(
      "UPDATE orders INNER JOIN tmp_orders ON orders.orderID = tmp_orders.orderID SET orders.status = tmp_orders.status"
    );
    System.out.println("Executing join on inventory");
    statement.execute(
      "UPDATE inventory INNER JOIN tmp_inv ON inventory.product_id = tmp_inv.product_id SET inventory.quantity = tmp_inv.quantity"
    );

    statement.execute("TRUNCATE TABLE tmp_orders");
    statement.execute("TRUNCATE TABLE tmp_inv");
    long end = System.nanoTime();

    double elapsed = (double) (end - start) / 1000000000;

    BigDecimal d = new BigDecimal(elapsed);
    String result = d.toPlainString();

    System.out.println("Done in : " + result + " s");

    return complete.SUCCESS;
  }

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

  public void appendInvSQLPart(
    StringBuilder builderOrders,
    int i,
    dbEntry entry,
    int quantityDiff
  ) {
    buildInvString(builderOrders, i, entry, quantityDiff);
  }

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

  // Converts the linkedlist to a hashmap
  public HashMap<String, dbEntry> convertToMap(LinkedList<dbEntry> entries) {
    long start = System.nanoTime();
    HashMap<String, dbEntry> entryHashMap = new HashMap<>();
    for (dbEntry entry : entries) {
      entryHashMap.put(entry.getId(), entry);
    }
    long end = System.nanoTime();
    double elapsed = (double) (end - start) / 1000000000;
    return entryHashMap;
  }
}
