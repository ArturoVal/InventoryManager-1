package team.moxie;

import me.tongfei.progressbar.ProgressBar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class OrderProcessor {
    invDbDriver invDriver;
    OrderDbDriver orderDriver;

    enum complete {
        FAILED,
        SUCCESS,
        ERROR,
        NO_ORDERS
    }

    public OrderProcessor(invDbDriver invDriver, OrderDbDriver orderDriver){
        this.invDriver = invDriver;
        this.orderDriver = orderDriver;
    }

    // This is roughly about 13 entries per second, which is very slow, we should research how to speed this up,
    // To do the entire table takes about 2 minutes
    public complete processOrders(LinkedList<OrderDbEntry> ordersList) {


        long start = System.nanoTime();
        if (invDriver == null || orderDriver == null) {
            throw new NullPointerException("invDriver or orderDriver is null");
        }
        if (ordersList.size() == 0) {
            return complete.NO_ORDERS;
        }


        int partitionSize = 50;
        LinkedList<List<OrderDbEntry>> partitions = new LinkedList<>();
        for (int i = 0; i < ordersList.size(); i += partitionSize) {
            partitions.add(ordersList.subList(i, Math.min(i + partitionSize, ordersList.size())));
        }
        HashMap<String, dbEntry> entryHashMap = convertToMap(invDriver.returnAllEntries());
        ProgressBar pb = new ProgressBar("Processing :", ordersList.size());
        int numOfFailed = 0;

        Connection dbConnInv = invDriver.getDbConn();
        Connection dbConnOrder = orderDriver.getDbConn();

        PreparedStatement orderStatement = dbConnOrder.prepareStatement("")

        for(List<OrderDbEntry> chunk : partitions) {

            int i = 0;
            double total = 0;

            for (OrderDbEntry entry : chunk) {
                long startInner = System.nanoTime();
                dbEntry tmpInvEntry = entryHashMap.get(entry.getProductID());

                //System.out.println(entry);
                //System.out.println(tmpInvEntry);

                int quantityDiff = tmpInvEntry.getQuantity()-entry.getQuantity();

                if (quantityDiff >= 0) {
                    invDriver.updateEntry(tmpInvEntry.getId(), quantityDiff, tmpInvEntry.getWholesalePrice(), tmpInvEntry.getSalePrice(), tmpInvEntry.getSupplierId());
                    orderDriver.updateStatus(entry.getID(), "complete");
                }
                else {
                    numOfFailed++;
                }
            }
            //pb.setExtraMessage("Insufficient: " + numOfFailed);
            pb.stepBy(chunk.size());
        }

        long end = System.nanoTime();

        double elapsed = (double) (end-start) / 1000000000;
        System.out.println("");

        return complete.SUCCESS;
    }

    // Converts the linkedlist to a hashmap
    private HashMap<String, dbEntry> convertToMap(LinkedList<dbEntry> entries) {
        long start = System.nanoTime();
        HashMap<String, dbEntry> entryHashMap = new HashMap<>();
        for (dbEntry entry : entries) {
            entryHashMap.put(entry.getId(), entry);
        }
        long end = System.nanoTime();
        double elapsed = (double) (end-start) / 1000000000;
        System.out.println("It took "+ elapsed + "to convert the linked list.");
        return entryHashMap;
    }

}
