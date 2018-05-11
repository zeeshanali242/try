package controllers.KycUserController;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class SendKycSubmissionMail {
	public void sendMail(String user_email,String user_name) throws MalformedURLException {
		
		String from = "team@trynex.in";

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", "smtp.gmail.com");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.debug", "true");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, "osscube123");
			}
		});

		// compose the message
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(user_email));
			message.setSubject("KYC Details Submitted Successfully");
			// URL url = new URL("/home/syncrasy/Downloads/logo.png");
			BodyPart body = new MimeBodyPart();

			Template t = Velocity.getTemplate("/conf/KycPending.vm");
			System.out.println("====File successfully loaded from scala folder====");
			/* create a context and add data */
			VelocityContext context = new VelocityContext();
			context.put("name", user_name);
			// context.put("cid", url);
			/* now render the template into a StringWriter */
			StringWriter writer = new StringWriter();
			t.merge(context, writer);
			// message.setText("Hello, this is example of sending email ");

			body.setContent(writer.toString(), "text/html");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(body);

			message.setContent(multipart, "text/html");
			// Send message
			Transport.send(message);
			System.out.println("message sent successfully....");

		} catch (MessagingException mex) {
			mex.printStackTrace();
		}

	}

}

