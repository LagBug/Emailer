package me.lagbug.emailer.spigot.guis;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import me.lagbug.emailer.global.EmailTemplate;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.builders.InventoryBuilder;
import me.lagbug.emailer.spigot.common.builders.ItemBuilder;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;

public class TemplateInventory implements Listener {

	private static final Emailer plugin = Emailer.getPlugin(Emailer.class);
	private final FileConfiguration config = plugin.getTemplatesGuiFile();
	private Inventory inventory = null;

	public TemplateInventory() {
		Bukkit.getPluginManager().registerEvents(this, CommonUtils.getPlugin());
	}

	public void openInventory(Player player) {
		InventoryBuilder inventoryBuilder = new InventoryBuilder();

		// We set the title on the GUI
		inventoryBuilder.setTitle(ChatColor.translateAlternateColorCodes('&', config.getString("title")));

		int size = plugin.getEmailTemplates().size();
		// We set the slots to the nearest round up of 9
		inventoryBuilder.setSlots((size >= 54) ? 54 : size + (9 - size % 9) * Math.min(1, size % 9));

		int slot = 0;
		for (String emailName : plugin.getEmailTemplates().keySet()) {
			if (emailName.equalsIgnoreCase("verify")) {
				continue;
			}
			
			EmailTemplate emailTemplate = plugin.getEmailTemplates().get(emailName);

			// We capitalize the first letter of the id
			emailName = emailName.substring(0, 1).toUpperCase() + emailName.substring(1);

			List<String> contents = new ArrayList<>();
			List<String> list = emailTemplate.getText();

			contents.add("");
			contents.add("&7Subject: &6" + emailTemplate.getSubject());
			contents.add("&7Type: &b" + emailTemplate.getType().toString());
			contents.add("&7Contents: &b");

			for (int i = 0; i < (list.size() <= 3 ? list.size() : 3); i++) {
				String current = list.get(i);

				if (!current.isEmpty()) {
					if (current.length() > 40) {
						current = current.substring(0, 34) + "...";
					}

					contents.add(" &b" + current);
				}
			}

			// Then create a new ItemStack with the information given
			inventoryBuilder.addItem(
					new ItemBuilder(Material.PAPER).setDisplayName("&b" + emailName).setLore(contents).build(), slot);
			slot++;
		}

		inventory = inventoryBuilder.build();
		player.openInventory(inventory);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		// If any of these conditions is true, we return
		if (e.getCurrentItem() == null || e.getClickedInventory() == null || e.getInventory() == null
				|| e.getCurrentItem().getType().equals(Material.AIR) || !(e.getWhoClicked() instanceof Player)
				|| !e.getClickedInventory().equals(inventory)) {
			return;
		}

		// We get an instance of the player
		Player player = (Player) e.getWhoClicked();
		
		// Cancel the event
		e.setCancelled(true);
		
		// Put the player & the email template on the map
		plugin.getEditing().put(player,
				ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase()));
		
		// And open the recipients inventory
		new RecipientInventory().openInventory(player);
	}
}
