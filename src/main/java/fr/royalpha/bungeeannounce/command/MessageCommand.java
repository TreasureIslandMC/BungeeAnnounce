package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.bungee.contexts.OnlinePlayer;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;


public class MessageCommand extends BaseCommand {
	private final BungeeAnnouncePlugin plugin;

	public MessageCommand(final BungeeAnnouncePlugin plugin) {
		this.plugin = plugin;
	}


	@CommandAlias("msg")
	//@CommandCompletion("@players @nothing")
	public void onMessage(final ProxiedPlayer sender, final OnlinePlayer receiver, final String... message){
		StringBuilder msgBuilder = new StringBuilder();
		for(String msg: message)
			msgBuilder.append(msg).append(" ");
		if (msgBuilder.toString().trim().equals(""))
			return;

		//final ProxiedPlayer to = ProxyServer.getInstance().getPlayer(receiver);
		if(!receiver.getPlayer().isConnected()) {
			sender.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString()));
			return;
		}
		plugin.getMsgManager().message(sender, receiver.getPlayer(), msgBuilder.toString());
	}



}
