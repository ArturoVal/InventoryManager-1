package team.moxie;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

  public int loadOrders(LinkedList<OrderDbEntry> ordersList) {
    Connection dbConn = orderDriver.getDbConn();

    try {
      ProgressBar pb = new ProgressBar("Inserting : ", ordersList.size());

      int curr = 0;
      for (OrderDbEntry order : ordersList) {
        PreparedStatement preparedStatement = dbConn.prepareStatement(
          "INSERT INTO orders(date, cust_email, cust_location, product_id, product_quantity, status) VALUES (?,?,?,?,?,?)",
          Statement.RETURN_GENERATED_KEYS
        );

        java.sql.Date sqlDate = new java.sql.Date(order.getDate().getTime());
        preparedStatement.setDate(1, sqlDate);
        preparedStatement.setString(2, order.getEmail());
        preparedStatement.setString(3, order.getLocation());
        preparedStatement.setString(4, order.getProductID());
        preparedStatement.setInt(5, order.getQuantity());
        preparedStatement.setString(6, order.getStatus());
        preparedStatement.execute();

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        resultSet.next();

        order.setID(resultSet.getInt(1));
        curr++;
        System.out.println(order);
        pb.step();
      }
      pb.close();
      return 1;
    } catch (SQLException throwables) {
      throwables.printStackTrace();
      return 0;
    }
  }

  // This is roughly about 13 entries per second, which is very slow, we should research how to speed this up,
  // To do the entire table takes about 2 minutes
  public complete processOrders(LinkedList<OrderDbEntry> ordersList) throws SQLException {
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

    ProgressBar pb = new ProgressBar("Processing :", ordersList.size());

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
        String sqlDateString = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
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
      i++;
      pb.step();
    }
    //pb.setExtraMessage("Insufficient: " + numOfFailed);

    String createOrderTable = builderOrders.toString();


    PreparedStatement preparedStatement = dbConnOrder.prepareStatement(createOrderTable);
    preparedStatement.execute();

    System.out.println(createOrderTable);

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
