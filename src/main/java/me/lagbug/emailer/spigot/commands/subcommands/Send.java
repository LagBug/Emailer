package me.lagbug.emailer.spigot.commands.subcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.commands.SubCommand;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;
import me.lagbug.emailer.spigot.utils.Email;
import me.lagbug.emailer.spigot.utils.Permissions;
import me.lagbug.emailer.spigot.utils.PlayerDataCache;

public class Send extends SubCommand {
	
	private final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	public Send() {
		super("send:sendemail:sendmail:s", Permissions.SEND);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender player, String[] args) {
		if (args.length <= 2) {
			player.sendMessage(plugin.getMessage("wrongUsage").replace("%usage%", "/emailer send <player/*/**> <template>"));
			return;
		}
		
		String to = args[1];
		String templateId = args[2];
		
		if (!plugin.getEmailTemplates().containsKey(templateId)) {
			player.sendMessage(plugin.getMessage("errors.templateNotFound"));
			return;
		}
		
		Map<String, String> verified = PlayerDataCache.getVerifiedData();
		
		if (verified.isEmpty()) {
			player.sendMessage(plugin.getMessage("errors.empty"));
			return;
		}
		
		List<EmailAddress> recipients = new ArrayList<>();
		
		switch (to) {
		case "*":
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (verified.containsKey(p.getUniqueId().toString())) {
					recipients.add(new EmailAddress(verified.get(p.getUniqueId().toString())));
				}
			}
			break;
		case "**":
			verified.values().forEach(address -> recipients.add(new EmailAddress(address)));
			break;
		default:
			OfflinePlayer p = Bukkit.getOfflinePlayer(to);
			
			if (!verified.containsKey(p.getUniqueId().toString())) {
				player.sendMessage(plugin.getMessage("commands.verify.notVerified").replace("%player%", p.getName()));
				return;
			}
			
			recipients.add(new EmailAddress(verified.get(p.getUniqueId().toString())));			
			break;
		}
		
		EmailAddress[] arr = new EmailAddress[recipients.size()];
		arr = recipients.toArray(arr);
		
		new Email(plugin.getSession(), plugin.getEmail(), arr).setTemplate(plugin.getEmailTemplates().get(templateId)).send();
		player.sendMessage(plugin.getMessage("commands.email.sent").replace("%recipients%", Integer.toString(recipients.size())));
		CommonUtils.log("An email has been sent by " + player.getName() + " to " + recipients.size() + " recipients.");
		
		Email email = new Email(plugin.getSession(), plugin.getEmail(), arr);
		email.setTemplate(plugin.getEmailTemplates().get(templateId));
		email.send();
	}
}