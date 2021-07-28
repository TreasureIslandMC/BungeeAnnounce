package fr.royalpha.bungeeannounce;

import co.aikar.commands.BungeeCommandManager;
import fr.royalpha.bungeeannounce.command.BungeeAnnounceCommand;
import fr.royalpha.bungeeannounce.handler.Logger;
import fr.royalpha.bungeeannounce.handler.PlayerAnnouncer;
import fr.royalpha.bungeeannounce.handler.PlayerAnnouncer.ConnectionType;
import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import fr.royalpha.bungeeannounce.manager.MsgManager;
import fr.royalpha.bungeeannounce.task.ScheduledAnnouncement;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bstats.bungeecord.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Royalpha
 */
public class BungeeAnnouncePlugin extends Plugin implements Listener {

	private static BungeeAnnouncePlugin instance;
	private static Logger logSystem;

	private ConfigManager configManager;
	private List<ScheduledAnnouncement> scheduledAnnouncement;
	private MsgManager msgManager;
	
	public BungeeAnnouncePlugin() {
		this.scheduledAnnouncement = new ArrayList<>();
	}

	public MsgManager getMsgManager() {
		return msgManager;
	}

	@Override
	public void onEnable() {
		instance = this;
		msgManager = new MsgManager();
		loadConfigFile();
		initializeLogSystem();
		loadConfigContent();

		registerCommands();
		registerListeners();

		initializeMetrics();
	}


	/*
	 * To do better, the load method should be executed in the onEnable to avoid repeating lines of code. However, since it is only used for the BAReload command, I decided to separate it from the onEnable.
	 */
	public void load() {
		this.configManager = new ConfigManager(this);
		logSystem = new Logger(this);
		
		this.scheduledAnnouncement = this.configManager.loadScheduledAnnouncement();
		this.configManager.loadAutoPlayerAnnouncement();
	}
	private void loadConfigFile() {
		this.configManager = new ConfigManager(this);
	}
	private void initializeLogSystem() {
		logSystem = new Logger(this);
	}
	private void loadConfigContent() {
		this.scheduledAnnouncement = this.configManager.loadScheduledAnnouncement();
		this.configManager.loadAutoPlayerAnnouncement();
	}

	private void registerCommands(){
		BungeeCommandManager bungeeCommandManager = new BungeeCommandManager(this);
		bungeeCommandManager.registerCommand(new BungeeAnnounceCommand(this));
	}

	private void registerListeners() {
		getProxy().getPluginManager().registerListener(this, this);
	}

	private void initializeMetrics() {
		Metrics metrics = new Metrics(this, 8662);
	}

	@EventHandler
	public void onConnect(final net.md_5.bungee.api.event.ServerConnectedEvent event) {
		final ProxiedPlayer player = event.getPlayer();
		List<PlayerAnnouncer> autoPlayerAnnouncements = PlayerAnnouncer.getAnnouncementList(player, event.getServer(), ConnectionType.CONNECT_SERVER);
		if (!autoPlayerAnnouncements.isEmpty()) {
			for (PlayerAnnouncer playerAnnouncer : autoPlayerAnnouncements)
				getProxy().getScheduler().schedule(this, () -> AnnouncementManager.sendToServer(playerAnnouncer.getAnnouncement(), getProxy().getConsole(), player, playerAnnouncer.getMessage(), playerAnnouncer.getBroadcastServers(), false, "", playerAnnouncer.getOptionalTitleArgs()), 500, TimeUnit.MILLISECONDS);
		}

	}

	
	@EventHandler
	public void onDisconnect(final net.md_5.bungee.api.event.PlayerDisconnectEvent event) {
		final ProxiedPlayer player = event.getPlayer();
		List<PlayerAnnouncer> autoPlayerAnnouncements = PlayerAnnouncer.getAnnouncementList(player, event.getPlayer().getServer(), ConnectionType.LEAVE_PROXY);
		if (!autoPlayerAnnouncements.isEmpty()) {
			for (PlayerAnnouncer playerAnnouncer : autoPlayerAnnouncements)
				getProxy().getScheduler().schedule(this, () -> AnnouncementManager.sendToServer(playerAnnouncer.getAnnouncement(), getProxy().getConsole(), player, playerAnnouncer.getMessage(), playerAnnouncer.getBroadcastServers(), false, "", playerAnnouncer.getOptionalTitleArgs()), 500, TimeUnit.MILLISECONDS);
		}
	}
	
	public List<ScheduledAnnouncement> getScheduledAnnouncement() {
		return this.scheduledAnnouncement;
	}
	
	public ConfigManager getConfigManager() {
		return this.configManager;
	}

	public static ProxyServer getProxyServer() {
		return instance.getProxy();
	}
	
	public static Logger getLoggerSystem() {
		return logSystem;
	}
}
