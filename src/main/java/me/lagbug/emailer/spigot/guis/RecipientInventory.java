package me.lagbug.emailer.spigot.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.builders.CustomInventory;
import me.lagbug.emailer.spigot.common.utils.communication.Title;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;
import me.lagbug.emailer.spigot.utils.Email;
import me.lagbug.emailer.spigot.utils.PlaceholderManager;
import me.lagbug.emailer.spigot.utils.PlayerDataCache;

public class RecipientInventory extends CustomInventory {

	private static final Emailer plugin = Emailer.getPlugin(Emailer.class);

	public RecipientInventory() {
		super(plugin.getFile("guis/recipient.yml"));
	}

	@Override
	public void onClick(Player player, String action, ItemStack item, int slot, ClickType click) {
		Map<String, String> verified = PlayerDataCache.getVerifiedData();

		// If the action is to go back, we do that before checking for the verified players
		if (action.equals("GO_BACK")) {
			new TemplateInventory().openInventory(player);
			return;
		}
		
		// If no one is verified, we return here
		if (verified.isEmpty()) {
			player.sendMessage(plugin.getMessage("errors.empty"));
			return;
		}

		List<EmailAddress> recipients = new ArrayList<>();

		// We switch the action
		switch (action) {
		case "SEND_TO_EVERYONE":
			verified.values().forEach(address -> recipients.add(new EmailAddress(address)));
			break;
		case "SEND_TO_ONLINE":
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (verified.containsKey(p.getUniqueId().toString())) {
					recipients.add(new EmailAddress(verified.get(p.getUniqueId().toString())));
				}
			}
			break;
		case "SEND_TO_PLAYER":
			plugin.getLinking().put(player, null);
			
			// We close the inventory and destroy
			destroy();
			player.closeInventory();
			//Send the searching title to notify the player
			Title.sendForever(player, "&bPlayer search", "&7Enter a player name to continue");
			return;
		default:
			break;
		}
		
		// We the the recipients array
		EmailAddress[] arr = new EmailAddress[recipients.size()];
		arr = recipients.toArray(arr);

		// We send the email
		new Email(plugin.getSession(), plugin.getEmail(), arr).setTemplate(plugin.getEmailTemplates().get(plugin.getEditing().get(player)))
				.send();
		
		// Notify the player
		player.sendMessage(plugin.getMessage("commands.email.sent").replace("%recipients%", Integer.toString(recipients.size())));
		// And log this in the console
		CommonUtils.log("An email has been sent by " + player.getName() + " to " + recipients.size() + " recipients.");
		// Lastly, we close the inventory & destroy the GUI
		player.closeInventory();
		destroy();
	}

	@Override
	public void onUpdate(Player player) {
		PlaceholderManager.get().forEach(key -> replace(key.getKey(), key.getValue()));
	}

	@Override
	public void onClose(Player player) {
		// Empty as the GUI will auto-destroy itself.
	}
}
