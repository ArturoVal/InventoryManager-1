package team.moxie;
import java.util.Properties;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public class EmailSend {
    public static Properties emailProperties;
    public static Session mailSession;
    public static MimeMessage emailMessage;

    public static void SMTP_setup() {//configures gmails SMTP server for sending emails
        String emailPort = "587";//gmail's smtp port
        emailProperties = System.getProperties();
        emailProperties.put("mail.smtp.port", emailPort);
        emailProperties.put("mail.smtp.host", "smtp.gmail.com");
        emailProperties.put("mail.smtp.auth", "true");
        emailProperties.put("mail.smtp.starttls.enable", "True");
    }

    //passed an address and message to create new email object
    public static void createEmail(String address, String sub, String message) throws AddressException, MessagingException {


        String recipient =  address;//"teammoxieRus@gmail.com" ;//receiving address, can be array for multiple addresses
        String subject = sub;//"Send test";
        String content = message;//"Testing the SMTP server for sending mail. If you're reading this, the test was a success!!";

        mailSession = Session.getDefaultInstance(emailProperties, null);
        emailMessage = new MimeMessage(mailSession);

        // for (int i = 0; i < recipient.length; i++) {
        emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        //}

        emailMessage.setSubject(subject);
        emailMessage.setContent(content, "text/html");//for a html email
    }

    public static void sendEmail(Properties config) throws AddressException, MessagingException
    {


        String emailHost = "smtp.gmail.com";//host server
        String fromUser = config.getProperty("gmailName");//sending email
        String fromUserEmailPassword = config.getProperty("gmailPass");//respective email password
        Transport transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromUser, fromUserEmailPassword);
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
        System.out.println("Email sent successfully.");
    }
}
