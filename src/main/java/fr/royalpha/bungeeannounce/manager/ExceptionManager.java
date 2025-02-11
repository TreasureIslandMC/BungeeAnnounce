package fr.royalpha.bungeeannounce.manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Royalpha
 */
public class ExceptionManager {
	
	private final Throwable throwable;

	public ExceptionManager(java.lang.Exception ex) {
		this.throwable = ex;
	}
	
	public ExceptionManager(Throwable th) {
		this.throwable = th;
	}
	
	public boolean register(Plugin plugin, boolean cast) {
		if (cast) {
			plugin.getLogger().warning("An error occured: " + throwable.getMessage());
			plugin.getLogger().info("For more information or any help, please contact the developer.");
		}
		String timeLog = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        File folder = new File(plugin.getDataFolder(), "reports/");
        if (!folder.exists()) 
        	folder.mkdirs();
        File logFile = new File(plugin.getDataFolder(), "reports/" + timeLog + ".txt");

        StringWriter errors = new StringWriter();
        throwable.printStackTrace(new PrintWriter(errors));
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
			writer.write(errors.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        return true;
	}
}
