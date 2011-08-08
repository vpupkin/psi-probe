package com.googlecode.psiprobe.tools;

import com.googlecode.psiprobe.tools.LogOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Facade for sending emails with the JavaMail API.
 * 
 * @author Mark Lewis
 */
public class Mailer {

    private Log log = LogFactory.getLog(this.getClass());
    private String from;
    private String smtp;

    public Mailer() {
        this(System.getProperty("com.googlecode.psiprobe.tools.mail.from"));
    }

    public Mailer(String from) {
        this(from, System.getProperty("mail.smtp.host"));
    }

    public Mailer(String from, String smtp) {
        this.smtp = smtp;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public String getSmtp() {
        return smtp;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public void send(MailMessage mailMessage) throws MessagingException {
        Properties props = (Properties) System.getProperties().clone();
        props.put("mail.smtp.host", smtp);
        LogOutputStream logStream = new LogOutputStream(log, LogOutputStream.LEVEL_DEBUG);
        PrintStream debugOut = new PrintStream(logStream, true);

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        session.setDebugOut(debugOut);

        MimeMessage message = createMimeMessage(session, mailMessage);
        log.debug("Sending message");
        Transport.send(message);
    }

    private MimeMessage createMimeMessage(Session session, MailMessage mailMessage) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        InternetAddress[] to = createAddresses(mailMessage.getTo());
        InternetAddress[] cc = createAddresses(mailMessage.getCc());
        InternetAddress[] bcc = createAddresses(mailMessage.getBcc());
        MimeMultipart content = new MimeMultipart("related");

        //Create attachments
        DataSource[] attachments = mailMessage.getAttachments();
        for (int i = 0; i < attachments.length; i++) {
            DataSource attachment = attachments[i];
            MimeBodyPart attachmentPart = createAttachmentPart(attachment);
            content.addBodyPart(attachmentPart);
        }
        
        //Create message body
        MimeBodyPart bodyPart = createMessageBodyPart(mailMessage.getBody(), mailMessage.isBodyHtml());
        content.addBodyPart(bodyPart);

        if (from == null) {
            message.setFrom();
        } else {
            message.setFrom(new InternetAddress(from));
        }
        message.setRecipients(Message.RecipientType.TO, to);
        message.setRecipients(Message.RecipientType.CC, cc);
        message.setRecipients(Message.RecipientType.BCC, bcc);
        message.setSubject(mailMessage.getSubject());
        message.setContent(content);
        return message;
    }

    private static InternetAddress[] createAddresses(String[] addresses) throws AddressException {
        List/*InternetAddress*/ result = new ArrayList();
        for (int i = 0; i < addresses.length; i++) {
            String address = addresses[i];
            InternetAddress[] parsedAddresses = InternetAddress.parse(address);
            for (int j = 0; j < parsedAddresses.length; j++) {
                InternetAddress parsedAddress = parsedAddresses[j];
                result.add(parsedAddress);
            }
        }
        return (InternetAddress[]) result.toArray(new InternetAddress[result.size()]);
    }
    
    private static MimeBodyPart createAttachmentPart(DataSource attachment) throws MessagingException {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(attachment));
        attachmentPart.setDisposition(MimeBodyPart.ATTACHMENT);
        attachmentPart.setFileName(attachment.getName());
        return attachmentPart;
    }

    private static MimeBodyPart createMessageBodyPart(String body, boolean html) throws MessagingException {
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText(body);
        bodyPart.setHeader("content-type", (html ? "text/html" : "text/plain"));
        return bodyPart;
    }

}
