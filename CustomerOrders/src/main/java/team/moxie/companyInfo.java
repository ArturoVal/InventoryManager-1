package team.moxie;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;


public class companyInfo {
    private OrderDbDriver orderDriver;
    private invDbDriver productDriver;
    private final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
    public void dailyReport (OrderDbDriver orderDriver,invDbDriver productDriver) {

        this.productDriver = productDriver;
        this.orderDriver = orderDriver;

        LinkedList <dbEntry> products = productDriver.returnAllEntries();
        LinkedList <OrderDbEntry> orders = orderDriver.returnAllEntries();

        System.out.println("----- Daily report -----");
        double currentTotal = sumAssets(products);
        System.out.println(" Current total assets: " + CURRENCY.format( currentTotal));
        int num = numOrders(orders);
        System.out.println("There are currently " + num + " orders");
        double orderTotal = totalOrderCost(orders, products);
        System.out.println("The orders total " + CURRENCY.format(orderTotal));
    }
    public double sumAssets(LinkedList <dbEntry> products) {
        double value = 0;
        //System.out.println(products.size());
        for (dbEntry product: products){
            double cost = product.getQuantity() * product.getWholesalePrice();
            value += cost;

        }
        return value;
    }
    public int numOrders(LinkedList <OrderDbEntry> orders){
        return orders.size();
        //return 1529;
    }
    public double totalOrderCost(LinkedList <OrderDbEntry> orders, LinkedList <dbEntry> products){

        double value = 0;
        int cnt = 0;
        for (OrderDbEntry order : orders){
            int quantity = order.getQuantity();
            String productid = order.getProductID();
            OrderProcessor processor = new OrderProcessor(productDriver,orderDriver);
            HashMap<String, dbEntry> productMap = processor.convertToMap(products);
            dbEntry product = productMap.get(productid);

            if(product != null){
                double cost = quantity *product.getSalePrice();
                value += cost;
            }
            if((++cnt) % 100 == 0){
                System.out.println("Summing cost of order " + cnt + "/" + orders.size());
            }
            //if(cnt>1529){return value;}
        }
        return value;
    }
}
