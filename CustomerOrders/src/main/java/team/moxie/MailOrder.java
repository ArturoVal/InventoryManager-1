package team.moxie;

import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Properties;

public class MailOrder {
    public static ReceiveMail receiver;
    public static ArrayList<OrderDbEntry> messages = null;
    //java.util.Date date = Calendar.getInstance().getTime();

    public static void checkMail(OrderDbDriver orderDriver, Properties props) {
        //connect to gmail server
        assert props != null;
        String host = props.getProperty("emailUrl");
        String mailStoreType = props.getProperty("serverType");
        String username = props.getProperty("gmailName");
        String password = props.getProperty("gmailPass");


        try {//read all email from server
            receiver = new ReceiveMail(host, mailStoreType, username, password);
            messages = receiver.getMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert messages != null;
        for (OrderDbEntry message : messages) {             //for all emails:
            System.out.println(message);                    //print contents of email
            System.out.println(message.getDate());
            boolean placeOrder = ReceiveMail.isOrder;       //determine if email is
            boolean cancelOrder = ReceiveMail.isCancel;     // order or cancellation

            if (placeOrder) {                               // for order:
                System.out.println("Placing the order...");
                orderDriver.createEntry(                    //create order database entry
                        message.getEmail(),
                        message.getDate(),
                        message.getProductID(),
                        message.getQuantity(),
                        "processing");
                System.out.println("Order placed.");
                EmailSend.sendConfirmation(                 //send email confirmation
                        message.getEmail(),
                        message.getQuantity(),
                        message.getProductID(),
                        message.getLocation(),
                        props
                );

            }
            else if (cancelOrder) {                                 // for cancellation of order:
                System.out.println("Canceling order...");
                if (orderDriver.deleteOrder(message.getEmail(),     //delete order form database
                        message.getProductID()))                    //if order is deleted from the order database
                {
                    EmailSend.sendCancellation(                     //send email confirmation
                            message.getEmail(),
                            message.getQuantity(),
                            message.getProductID(),
                            props
                    );
                    System.out.println("Order canceled.");
                } else {                                            //if order cannot be deleted from database
                    EmailSend.sendCancelError(
                            message.getEmail(),
                            message.getQuantity(),
                            message.getProductID(),
                            props
                    );
                    System.out.println("Unable to cancel.");
                }
            }
        }
    }

}