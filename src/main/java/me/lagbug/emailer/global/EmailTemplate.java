package me.lagbug.emailer.global;

import java.util.ArrayList;
import java.util.List;

import me.lagbug.emailer.global.enums.EmailType;
import me.lagbug.emailer.spigot.Emailer;

public class EmailTemplate {

	@SuppressWarnings("unused")
	private final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	private String id, subject, htmlFile;
	private EmailType type;
	private List<String> text = new ArrayList<>();
	
	public EmailTemplate(String id, String subject, EmailType type, String htmlFile, List<String> text) {
		setId(id);
		setSubject(subject);
		setType(type);
		setHtmlFile(htmlFile);
		setText(text);
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }

	public String getHtmlFile() { return htmlFile; }
	public void setHtmlFile(String htmlFile) { this.htmlFile = htmlFile; }

	public EmailType getType() { return type; }
	public void setType(EmailType type) { this.type = type; }

	public List<String> getText() { return text; }
	public void setText(List<String> text) { this.text = text; }
}