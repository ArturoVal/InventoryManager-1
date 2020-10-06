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

    public complete processOrders(OrderDbEntry[] ordersArray) {
        long start = System.nanoTime();
        if (invDriver == null || orderDriver == null) {
            throw new NullPointerException("invDriver or orderDriver is null");
        }
        if (ordersArray.length == 0) {
            return complete.NO_ORDERS;
        }

        HashMap<String, dbEntry> entryHashMap = convertToMap(invDriver.returnAllEntries());

        for (OrderDbEntry entry : ordersArray) {
            dbEntry tmpInvEntry = entryHashMap.get(entry.getProductID());

            int quantityDiff = entry.getQuantity()-tmpInvEntry.getQuantity();

            if (quantityDiff >= 0) {

            }
            else {

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
