package team.moxie;

import java.text.NumberFormat;
import java.util.LinkedList;


public class companyInfo {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
    public static void dailyReport (OrderDbDriver orderDriver,DbDriver productDriver) {
        System.out.println("----- Daily report -----");
        double currentTotal = sumAssets(productDriver);
        System.out.println(" Current total assets: " + CURRENCY.format( currentTotal));
        int num = numOrders(orderDriver);
        System.out.println("There are currently " + num + " orders");
        double orderTotal = totalOrderCost(productDriver, orderDriver);
        System.out.println("The orders total " + CURRENCY.format(orderTotal));
    }
    public static double sumAssets(DbDriver productDriver) {
        LinkedList<dbEntry> products = productDriver.returnAllEntries();
        double value = 0;
        //System.out.println(products.size());
        for (dbEntry product: products){
            double cost = product.getQuantity() * product.getWholesalePrice();
            value += cost;

        }
        return value;
    }
    public static int numOrders(OrderDbDriver orderDriver){
        LinkedList<OrderDbEntry> orders = orderDriver.returnAllEntries();
        return orders.size();
    }
    public static double totalOrderCost(DbDriver productDriver,OrderDbDriver orderDriver){
        LinkedList <OrderDbEntry> orders = orderDriver.returnAllEntries();
        double value = 0;
        int cnt = 0;
        for (OrderDbEntry order : orders){
            int quantity = order.getQuantity();
            String productid = order.getProductID();
            dbEntry product = productDriver.searchById(productid);

            if(product != null){
                double cost = quantity *product.getSalePrice();
                value += cost;
            }
            if((++cnt) % 100 == 0){
                System.out.println("Summing cost of order " + cnt + "/" + orders.size());
            }
        }
        return value;
    }
}
