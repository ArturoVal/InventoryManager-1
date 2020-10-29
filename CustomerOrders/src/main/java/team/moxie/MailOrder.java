package team.moxie;

import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Properties;

public class MailOrder {
    public static ReceiveMail receiver;
    public static ArrayList<OrderDbEntry> messages = null;
    //java.util.Date date = Calendar.getInstance().getTime();

    public static void checkMail(OrderDbDriver orderDriver, Properties props) {
        assert props != null;
        String host = props.getProperty("emailUrl");
        String mailStoreType = props.getProperty("serverType");
        String username = props.getProperty("gmailName");
        String password = props.getProperty("gmailPass");

        try {
            receiver = new ReceiveMail(host, mailStoreType, username, password);
            messages = receiver.getMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }


        assert messages != null;
        for (OrderDbEntry message : messages) {
            System.out.println(message);
            System.out.println(message.getDate());
            boolean placeOrder = ReceiveMail.isOrder;
            boolean cancelOrder = ReceiveMail.isCancel;

            if (placeOrder) {

                System.out.println("Placing the order...");
                orderDriver.createEntry(
                        message.getEmail(),
                        message.getDate(),
                        message.getProductID(),
                        message.getQuantity(),
                        "processing"
                );
                System.out.println("Order placed.");
                //send email confirmation
                EmailSend.sendConfirmation(
                        message.getEmail(),
                        message.getQuantity(),
                        message.getProductID(),
                        message.getLocation(),
                        props
                );

            } else if (cancelOrder) {
                System.out.println("Canceling order...");
                if (orderDriver.deleteOrder(message.getEmail(), message.getProductID())) {
                    EmailSend.sendCancellation(
                            message.getEmail(),
                            message.getQuantity(),
                            message.getProductID(),
                            props
                    );
                    System.out.println("Order canceled.");
                } else {
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