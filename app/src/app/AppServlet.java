package app;
import java.io.IOException;

import javax.mail.Message;
import javax.servlet.http.*;
import javax.mail.*;
import javax.mail.internet.*;

import java.util.Properties;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		if(req.getQueryString().contains("fsl")){
			Entity e = new Entity("event");
			e.setProperty("eventname",req.getParameter("event"));
			e.setProperty("fsl",req.getParameter("fsl"));
			ds.put(e);
		} else if(req.getQueryString().contains("email")){
			Entity e = new Entity("contact");
			e.setProperty("event",req.getParameter("event"));
			e.setProperty("name",req.getParameter("name"));
			e.setProperty("lastname",req.getParameter("lastname"));
			e.setProperty("email",req.getParameter("email"));
			ds.put(e);
			try {
				Message msg = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
				msg.setFrom(new InternetAddress("luisolea87@gmail.com", "Freescale event contact form"));
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(req.getParameter("email"), req.getParameter("name")));
				msg.setSubject("Thanks for meeting with Freescale");
				msg.setText(req.getParameter("name")+",\n\nThank you for showing your interest in Freescale at the event "+req.getParameter("event")+". Your contact information has been saved to our servers and will be reviewed shortly.\n\n The Freescale team.");
				Transport.send(msg);
			} catch(Exception err){
				err.printStackTrace();
			}
		} else if(req.getQueryString().contains("finish")){
			
			String msg = "These are your contacts for event "+req.getParameter("event")+":\n\n";
			msg += "Name \tLast name \temail\n";
			
			Query q = new Query("event");
			q.addFilter("eventname", FilterOperator.EQUAL, req.getParameter("event"));
			Entity e = ds.prepare(q).asList(FetchOptions.Builder.withDefaults()).get(0);
			String email = (String)e.getProperty("fsl");
			email += "@freescale.com";
			q = new Query("contact");
			q.addFilter("event",FilterOperator.EQUAL, req.getParameter("event"));
			for(Entity r: ds.prepare(q).asIterable()){
				msg += (String)r.getProperty("name")+"\t";
				msg += (String)r.getProperty("lastname")+"\t";
				msg += (String)r.getProperty("email")+"\n";
			}
			try {
				Message mail = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
				mail.setFrom(new InternetAddress("luisolea87@gmail.com", "Freescale event contact form"));
				mail.addRecipient(Message.RecipientType.TO, new InternetAddress(email, "automated email"));
				mail.setSubject("Your contacts for event "+req.getParameter("event"));
				mail.setText(msg);
				Transport.send(mail);
			} catch(Exception err){
				err.printStackTrace();
			}
		}
		resp.getWriter().println("OK");
	}
}
