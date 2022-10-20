package me.lagbug.emailer.spigot.guis;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.builders.CustomInventory;
import me.lagbug.emailer.spigot.common.utils.communication.Title;
import me.lagbug.emailer.spigot.utils.Checker;
import me.lagbug.emailer.spigot.utils.PlaceholderManager;

public class PlayerManagerInventory extends CustomInventory {

	private static final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	public PlayerManagerInventory() {
		super(plugin.getFile("guis/player_manager.yml"));
	}

	private boolean close = true;
	
	@Override
	public void onClick(Player player, String action, ItemStack item, int slot, ClickType click) {
		switch (action) {
		case "FORCE_VERIFY":
			// We destroy the current GUI
			close = false;
			player.closeInventory();
			destroy();
			
			//Send the searching title to notify the player
			Title.sendForever(player, "&bPlayer Linking", "&7Enter an email to continue");
			break;
		case "UN_VERIFY":
			OfflinePlayer target = plugin.getLinking().get(player);
			Checker checker = new Checker(target.getUniqueId());
			
			if (!checker.isVerified()) {
				player.sendMessage(plugin.getMessage("commands.verify.notVerified").replace("%player%", target.getName()));
				return;
			}
			
			checker.unVerify();
			player.sendMessage(plugin.getMessage("commands.unverify.success").replace("%player%", target.getName()));
			break;
		case "GO_BACK":
			new HomeInventory().openInventory(player);
			break;
		default:
			break;
		}
	}

	@Override
	public void onUpdate(Player player) {
		PlaceholderManager.get(plugin.getLinking().get(player)).forEach(key -> replace(key.getKey(), key.getValue()));
	}

	@Override
	public void onClose(Player player) {
		if (close) plugin.getLinking().remove(player);
	}
}
