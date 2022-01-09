package fr.royalpha.bungeeannounce.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.task.ScheduledAnnouncement;
import fr.royalpha.bungeeannounce.util.BAUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * @author Royalpha
 */
public class ConfigManager {
	private final BungeeAnnouncePlugin plugin;
	private Configuration config;
	private Configuration ignored;
	private Configuration toggled;

	public ConfigManager(final BungeeAnnouncePlugin plugin) {
		this.plugin = plugin;
		if (!plugin.getDataFolder().exists())
			plugin.getDataFolder().mkdirs();
		final File configFile = new File(plugin.getDataFolder(), "config.yml");
		final File ignoredFile = new File(plugin.getDataFolder(), "ignored.yml");
		final File toggledFile = new File(plugin.getDataFolder(),"toggled.yml");
		generateFile(configFile,"config.yml");
		generateFile(ignoredFile,"ignored.yml");
		generateFile(toggledFile,"toggled.yml");
		try {
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
			this.ignored = ConfigurationProvider.getProvider(YamlConfiguration.class).load(ignoredFile);
			this.toggled = ConfigurationProvider.getProvider(YamlConfiguration.class).load(toggledFile);
		} catch (IOException e) {
			new ExceptionManager(e).register(plugin, true);
		}
		Field.init(this.config);
	}


	private void generateFile(final File file,final String fileName) {
		if (!file.exists()) {
			getLogger().info("Generating configuration file "+ fileName);
			try (InputStream in = plugin.getResourceAsStream(fileName)) {
				Files.copy(in, file.toPath());
			} catch (IOException e) {
				new ExceptionManager(e).register(plugin, true);
				getLogger().warning("Error when generating configuration file !");
			} finally {
				getLogger().info("Configuration file was generated with success !");
			}
		}
	}

	public List<ScheduledAnnouncement> loadScheduledAnnouncement() {
		List<ScheduledAnnouncement> output = new ArrayList<>();
		int i = 0;
		
		Configuration schedulerSection = this.config.getSection("scheduler");
		for (String taskName : schedulerSection.getKeys()) {
			try {
				String type = schedulerSection.getString(taskName + ".type", "");
				
				AnnouncementManager announcement = AnnouncementManager.getAnnouncement(type);
				if (announcement == null) {
					getLogger().log(Level.WARNING, "Error when loading announcement \"%s\", the field 'type' wasn't recognized.", taskName);
					continue;
				}
				
				String message = schedulerSection.getString(taskName + ".message", "<No message was set for this announcement>");
				List<String> servers = schedulerSection.getStringList(taskName + ".servers");
				String permission = schedulerSection.getString(taskName + ".permission", "");
				int delay = schedulerSection.getInt(taskName + ".delay", 5);
				int interval = schedulerSection.getInt(taskName + ".interval", 10);
				Integer[] optionalTitleArgs = BAUtils.getOptionalTitleArgsFromConfig(announcement, type);
			
				if (interval < 0) {
					getLogger().info("The scheduled announcement \"" + taskName + "\" has a negative interval. So it was frozen. In other words, the only way to broadcast it is to use the command: /forceBroadcast " + taskName);
				} else {
					output.add(new ScheduledAnnouncement(this.plugin, announcement, message, servers, permission, delay, interval, optionalTitleArgs));
				}
				i++;
				
			} catch (Exception ex) {
				getLogger().warning("Error when loading announcement \"" + taskName + "\" in config.yml");
				new ExceptionManager(ex).register(this.plugin, true);
			}
		}
		if (i > 0)
			getLogger().log(Level.INFO, i + " scheduled announcement" + (i > 1 ? "s" : "") + " " + (i > 1 ? "were" : "was") + " correctly loaded.");
		return output;
	}

	public Logger getLogger() {
		return this.plugin.getLogger();
	}
	
	public enum Field {
		@Deprecated ENABLE_PRIVATE_MESSAGING("enable-private-message", Boolean.class, true),
		@Deprecated COMMAND_FOR_PRIVATE_MESSAGING("command-for-private-message", String.class, "msg"),
		REGISTER_LOGS("enable-announcement-logs", Boolean.class, false),
		ANNOUNCE_PREFIX("announce-prefix", String.class, ""),
		ACTION_PREFIX("action-prefix", String.class, ""),
		SUBTITLE_PREFIX("subtitle-prefix", String.class, ""),
		TITLE_PREFIX("title-prefix", String.class, ""),
		WARN_PREFIX("warn-prefix", String.class, "&f[&4&lWARN&f] &b"),
		PM_SENT("private-message-send", String.class, "&3Send to &e%RECEIVER%: &d%MESSAGE%"),
		PM_RECEIVED("private-message-received", String.class, "&3Received from &e%SENDER%: &d%MESSAGE%"),
		PM_PLAYER_NOT_ONLINE("private-message-player-not-online", String.class, "&c%PLAYER% is unreachable :("),
		PM_SENDER_EQUALS_RECEIVER("private-message-sender-equals-receiver", String.class, "&7Are you schizophrenic ? :O"),
		REPLY_INFO("reply-info", String.class, "&7Use &a/reply &7to respond to &b%SENDER%"),
		TOGGLE_MESSAGE_ON("private-message-toggle-on",String.class,"&6Receiving message &cenabled"),
		TOGGLE_MESSAGE_OFF("private-message-toggle-off",String.class,"&6Receiving message &cdisabled"),
		IGNORE_PLAYER_ON("private-message-ignore-on",String.class,"&6You will no longer receive messages from &c%PLAYER%."),
		IGNORE_PLAYER_OFF("private-message-ignore-off",String.class,"&6You will now receive messages from &c%PLAYER%."),
		MESSAGES_OFF("private-message-toggled",String.class,"&cThis player has turned off messages."),
		MESSAGE_IGNORED("private-message-ignored",String.class,"&cYou cannot send messages to a player you have ignored."),
		MESSAGE_TOGGLED("private-message-self-toggle",String.class,"&6You cannot send messages as you have toggled off messages.");

		private final String configField;
		private final Class<?> type;
		private final Object def;
		private Object value;
		
		Field(String configField, Class<?> type, Object def) {
			this.configField = configField;
			this.type = type;
			this.def = def;
		}
		
		public String getField() {
			return this.configField;
		}
		
		public Class<?> getType() {
			return this.type;
		}
		
		public boolean getBoolean() {
			return (boolean) this.value;
		}
		
		public String getString() {
			return (String) this.value;
		}
		
		public Object getDefault() {
			return this.def;
		}
		
		private void setValue(Object obj) {
			this.value = obj;
		}
		
		public static void init(Configuration config) {
			for (Field field : values()) {
				if (field.getType() == String.class) {
					field.setValue(ChatColor.translateAlternateColorCodes('&', config.getString(field.getField(), (String) field.getDefault())));
				} else if (field.getType() == Boolean.class) {
					field.setValue(config.getBoolean(field.getField(), (boolean) field.getDefault()));
				}
			}
		}
	}
	
	public Configuration getConfig() {
		return this.config;
	}

	public Configuration getIgnored() {
		return ignored;
	}

	public Configuration getToggled() {
		return toggled;
	}
}
