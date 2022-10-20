package me.lagbug.emailer.spigot.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.global.EmailTemplate;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.api.events.EmailSendEvent;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;

public class Email {

	private final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	private Message message;
	private EmailAddress fromAddress;
	private final EmailAddress[] toAddress;
	
	private EmailTemplate template;
	private String playerName, code;
	private final boolean finished = false;
	
	public Email(Session session, EmailAddress fromAddress,  EmailAddress[] toAddress) {
		this.toAddress = toAddress;
		set(session, fromAddress);
	}

	public Email(Session session, EmailAddress fromAddress, EmailAddress toAddress) {
		set(session, fromAddress);

		this.toAddress = new EmailAddress[]{ toAddress };
	}
	
	private void set(Session session, EmailAddress fromAddress) {
		this.fromAddress = fromAddress;
		message = new MimeMessage(session);
	}
	
	public Email setTemplate(EmailTemplate template) {
		this.template = template;
		return this;
	}
	
	public Email setPlayer(Player player) {
		this.playerName = player.getName();
		return this;
	}
	
	public Email setCode(String code) {
		this.code = code;
		return this;
	}
	
	public void send() {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				message.setFrom(fromAddress);
				message.setRecipients(Message.RecipientType.TO, toAddress);
				message.setSubject(template.getSubject());
				message.setHeader("Content-Type","text/html; charset=\"utf-8\"");
				message.setHeader("Content-Transfer-Encoding", "quoted-printable");

				switch (template.getType()) {
				case HTML:
					File htmlFile = new File(plugin.getDataFolder() + File.separator + "html", template.getHtmlFile());
					if (!htmlFile.exists()) { plugin.saveResource("html" + File.separator + template.getHtmlFile(), false); }

					try {
						BufferedReader reader = new BufferedReader(new FileReader(htmlFile));
						String contents = "", line = "";
						while ((line = reader.readLine()) != null) {
						  contents += line;
						}

						reader.close();

						if (playerName != null && code != null) {
							contents = contents.replace("%player%", playerName).replace("%code%", code);
						}
						message.setContent(contents, "text/html; charset=utf-8");
					} catch (IOException ex) {
						ex.printStackTrace();
					}

					break;
				case TEXT:
					String text = "";
					for (String cline : template.getText()) {
						text += cline + "\n";
					}

					if (playerName != null && code != null) { text = text.replace("%player%", playerName).replace("%code%", code); }
					message.setText(text);
					break;
				}

				EmailSendEvent ese = new EmailSendEvent(getInstance());
				if (ese.isCancelled()) {
					return;
				}

				Transport.send(message);
			} catch (MessagingException ex) {
				CommonUtils.log("An error has occurred and the email was not sent: " + ex.getMessage());
				ex.printStackTrace();
			}
		});
	}
	
	private Email getInstance() {
		return this;
	}
	
	public boolean isFinished() {
		return finished;
	}
}