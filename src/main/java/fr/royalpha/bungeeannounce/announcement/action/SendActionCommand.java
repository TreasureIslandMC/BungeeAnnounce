package fr.royalpha.bungeeannounce.announcement.action;

import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Royalpha
 */
public class SendActionCommand extends Command {

	public SendActionCommand() {
		super("sendaction", "bungeeannounce.command.sendaction", "bungee:sendaction");
	}

	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /sendaction <action>"));
			return;
		}
		StringBuilder actionBuilder = new StringBuilder();
		for (final String arg : args) {
			actionBuilder.append(arg).append(" ");
		}
		AnnouncementManager.sendToServer(AnnouncementManager.ACTION, sender instanceof ProxiedPlayer ? sender : null, actionBuilder.toString().trim(), null, true, "");
	}
}
