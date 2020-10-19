package team.moxie;

import java.util.HashMap;
import java.util.LinkedList;

public class OrderProcessor {
    DbDriver invDriver;
    OrderDbDriver orderDriver;

    enum complete {
        FAILED,
        SUCCESS,
        ERROR,
        NO_ORDERS
    }

    public OrderProcessor(DbDriver invDriver, OrderDbDriver orderDriver){
        this.invDriver = invDriver;
        this.orderDriver = orderDriver;
    }

    // This is roughly about 13 entries per second, which is very slow, we should research how to speed this up,
    // To do the entire table takes about 2 minutes
    public complete processOrders(OrderDbEntry[] ordersArray) {
        long start = System.nanoTime();
        if (invDriver == null || orderDriver == null) {
            throw new NullPointerException("invDriver or orderDriver is null");
        }
        if (ordersArray.length == 0) {
            return complete.NO_ORDERS;
        }

        HashMap<String, dbEntry> entryHashMap = convertToMap(invDriver.returnAllEntries());

        int i = 0;
        double total = 0;

        for (OrderDbEntry entry : ordersArray) {
            long startInner = System.nanoTime();
            dbEntry tmpInvEntry = entryHashMap.get(entry.getProductID());

            //System.out.println(entry);
            //System.out.println(tmpInvEntry);

            int quantityDiff = tmpInvEntry.getQuantity()-entry.getQuantity();

            if (quantityDiff >= 0) {
                invDriver.updateEntry(tmpInvEntry.getId(), quantityDiff, tmpInvEntry.getWholesalePrice(), tmpInvEntry.getSalePrice(), tmpInvEntry.getSupplierId());
                orderDriver.updateStatus(entry.getID(), "complete");
                long endInner = System.nanoTime();

                double elapsedInner = (double) (endInner-startInner) / 1000000000;
                total+=elapsedInner;
                i++;
                double average = (double) (total/i);
                double perSec = 1/average;
                System.out.println(i + "/" +ordersArray.length + "  (" + elapsedInner + " s    |    "+ perSec + " per second)");

            }
            else {
                System.out.println("Not enough quantity");
            }
        }

        long end = System.nanoTime();

        double elapsed = (double) (end-start) / 1000000000;
        System.out.println("It took " + elapsed + "s to process " + ordersArray.length + " orders.");

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
