package team.moxie;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import me.tongfei.progressbar.ProgressBar;

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

      ProgressBar progressBar = new ProgressBar(
        "Inserting : ",
        ordersList.size()
      );

      for (OrderDbEntry order : ordersList) {
        String sqlDateString = new SimpleDateFormat("yyyy-MM-dd 00:00:00")
        .format(order.getDate());

        java.sql.Date sqlDate = new java.sql.Date(order.getDate().getTime());

        if (curr == 0) {
          String part = String.format(
            "('%s', '%s', '%s','%s','%d','processing','%d')\n",
            sqlDateString,
            order.getEmail(),
            order.getLocation(),
            order.getProductID(),
            order.getQuantity(),
            order.getID()
          );

          builderOrders.append(part);
        } else {
          String part = String.format(
            ",('%s', '%s', '%s','%s','%d','processing','%d')\n",
            sqlDateString,
            order.getEmail(),
            order.getLocation(),
            order.getProductID(),
            order.getQuantity(),
            order.getID()
          );

          builderOrders.append(part);
        }
        progressBar.step();
        curr++;
      }

      String insertOrders = builderOrders.toString();

      PreparedStatement preparedStatement = dbConn.prepareStatement(
        insertOrders,
        Statement.RETURN_GENERATED_KEYS
      );
      preparedStatement.execute();

      int i = 0;
      ResultSet resultSet = preparedStatement.getGeneratedKeys();
      while (resultSet.next()) {
        int anInt = resultSet.getInt(1);

        ordersList.get(i).setID(anInt);
        i++;
      }

      progressBar.close();

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
    long start = System.nanoTime();
    if (invDriver == null || orderDriver == null) {
      throw new NullPointerException("invDriver or orderDriver is null");
    }
    if (ordersList.size() == 0) {
      return complete.NO_ORDERS;
    }

    HashMap<String, dbEntry> entryHashMap = convertToMap(
      invDriver.returnAllEntries()
    );

    ProgressBar progressBar = new ProgressBar(
      "Processing :",
      ordersList.size()
    );

    Connection dbConnInv = invDriver.getDbConn();
    Connection dbConnOrder = orderDriver.getDbConn();

    StringBuilder builderOrders = new StringBuilder();
    builderOrders.append("INSERT INTO tmp_orders VALUES \n");
    //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500'),

    int i = 0;
    double total = 0;
    //

    for (OrderDbEntry entry : ordersList) {
      long startInner = System.nanoTime();
      dbEntry tmpInvEntry = entryHashMap.get(entry.getProductID());

      //System.out.println(entry);
      //System.out.println(tmpInvEntry);

      int quantityDiff = tmpInvEntry.getQuantity() - entry.getQuantity();

      if (quantityDiff >= 0) {
        String sqlDateString = new SimpleDateFormat("yyyy-MM-dd 00:00:00")
        .format(entry.getDate());
        //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500')
        if (i == 0) {
          String part = String.format(
            "('%s', '%s', '%s','%s','%d','complete','%d')\n",
            sqlDateString,
            entry.getEmail(),
            entry.getLocation(),
            entry.getProductID(),
            entry.getQuantity(),
            entry.getID()
          );
          builderOrders.append(part);
        } else {
          String part = String.format(
            ",('%s', '%s', '%s','%s','%d','complete','%d')\n",
            sqlDateString,
            entry.getEmail(),
            entry.getLocation(),
            entry.getProductID(),
            entry.getQuantity(),
            entry.getID()
          );
          builderOrders.append(part);
        }
      } else {
        System.out.println("Insufficient");
        String sqlDateString = new SimpleDateFormat("yyyy-MM-dd 00:00:00")
        .format(entry.getDate());
        //INSERT INTO tmp_orders VALUES ('yyyy-MM-dd hh:mm:ss', 'fake@fake.com', '11111','003S3SQT7KI2','10','complete','1500')
        if (i == 0) {
          String part = String.format(
            "('%s', '%s', '%s','%s','%d','insufficient','%d')\n",
            sqlDateString,
            entry.getEmail(),
            entry.getLocation(),
            entry.getProductID(),
            entry.getQuantity(),
            entry.getID()
          );
          builderOrders.append(part);
        } else {
          String part = String.format(
            ",('%s', '%s', '%s','%s','%d','insufficient','%d')\n",
            sqlDateString,
            entry.getEmail(),
            entry.getLocation(),
            entry.getProductID(),
            entry.getQuantity(),
            entry.getID()
          );
          builderOrders.append(part);
        }
      }
      progressBar.step();
      i++;
    }
    //pb.setExtraMessage("Insufficient: " + numOfFailed);

    String createOrderTable = builderOrders.toString();

    //UPDATE orders INNER JOIN tmp_orders ON orders.orderID = tmp_orders.orderID SET orders.status = tmp_orders.status

    PreparedStatement preparedStatement = dbConnOrder.prepareStatement(
      createOrderTable
    );
    preparedStatement.execute();

    PreparedStatement preparedStatement2 = dbConnOrder.prepareStatement(
      "UPDATE orders INNER JOIN tmp_orders ON orders.orderID = tmp_orders.orderID SET orders.status = 'insufficient' where tmp_orders.status = 'insufficient'"
    );
    preparedStatement2.execute();

    preparedStatement2 =
      dbConnOrder.prepareStatement(
        "UPDATE orders INNER JOIN tmp_orders ON orders.orderID = tmp_orders.orderID SET orders.status = 'complete' where tmp_orders.status = 'complete'"
      );
    preparedStatement2.execute();

    PreparedStatement preparedStatement3 = dbConnOrder.prepareStatement(
      "TRUNCATE TABLE tmp_orders"
    );
    preparedStatement3.execute();

    progressBar.close();

    return complete.SUCCESS;
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
