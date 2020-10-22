package team.moxie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;

public class ReceiveMail {
  private final Folder emailFolder;
  private final Store emailStore;
  static boolean isOrder=false;
  static boolean isCancel=false;

  public ReceiveMail(
    String pop3Host,
    String storeType,
    String user,
    String password
  )
    throws MessagingException {
    Properties properties = new Properties();
    //use SMTP to send and receive emails
    String emailPort = "587"; //gmail's smtp port
    properties = System.getProperties();
    properties.put("mail.smtp.port", emailPort);
    properties.put("mail.smtp.host", "smtp.gmail.com");
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "True");
    //properties.put("mail.pop3.host", pop3Host);
    //properties.put("mail.pop3.port", "995");
    //properties.put("mail.pop3.starttls.enable", "true");
    Session emailSession = Session.getDefaultInstance(properties);

    emailStore = emailSession.getStore(storeType);
    emailStore.connect(pop3Host, user, password);

    emailFolder = emailStore.getFolder("INBOX");
    System.out.println(emailFolder);
    emailFolder.open(Folder.READ_ONLY);
  }

  public ArrayList<OrderDbEntry> getMessages()
    throws MessagingException, IOException {
    Message[] messages = emailFolder.getMessages();
    System.out.println(
      "You have " + messages.length + " new order(s) submitted via email."
    );
    String[] entry = new String[5];
    ArrayList<OrderDbEntry> entries = new ArrayList<>();
    int numberOfEntries = 0;

    for (Message message : messages) {
      Multipart tempMultipart = (Multipart) message.getContent();
      BodyPart bp = tempMultipart.getBodyPart(0);
      System.out.println(bp.toString());
      System.out.println("---------------------------------");
      System.out.println("Email Number " + (numberOfEntries + 1));
      String sub = message.getSubject().toUpperCase();
      System.out.println("Subject: " + sub);
      Address[] address = message.getFrom();
      String email = address[0].toString();
      int emailIndex = email.indexOf("<") + 1;
      email = email.substring(emailIndex, email.length() - 1);
      Date date = message.getSentDate();
      if(sub.equals("PLACE ORDER"))   isOrder=true;
      else if(sub.equals("CANCEL ORDER")) isCancel = true;

        entry[1] = email;
        String[] content = bp.getContent().toString().toUpperCase().split(",");

        /*
         * The following foreach statement is used for formatting each string within an "entry" array.
         * It removes "\n" endings, other special characters, and white spaces from the "content" array
         * for formatting consistency." It then stores the address, supply id, and quantity ordered and saves
         * them in the "entry" array under indices 2,3,and 4.
         */
        int counter = 2;

        for (String each : content) {
          if (counter == 2) {
            each = each.replace("\n", "");
          } else if (counter == 4) {
            System.out.println(each);
            each = each.replaceAll("[^\\d.]", "");
          } else {
            each = each.replace(" ", "");
            each = each.replace("\n", "");
            each = each.replace("[^\\x00-\\x7F]", "");
            each = each.replace("[\\p{Cntrl}&&[^\r\n\t]]", "");
            each = each.replace("\\p{C}", "");
          }
          entry[counter] = each;
          counter++;
        }

        int Quantity = Integer.parseInt(entry[4]);

        OrderDbEntry dbEntry = new OrderDbEntry(
                date,
                entry[1],
                entry[2],
                entry[3],
                Quantity
        );

        //Adds each order to the ArrayList for database updates.
        entries.add(dbEntry);
      }



    emailFolder.close(false);
    emailStore.close();
    return entries;
  }

}
