package me.sturm.regiontp;

import com.sk89q.worldguard.WorldGuard;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionTPPlugin extends JavaPlugin {

    private RgtpCommandExecutor rgtpCommandExecutor;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Lang.init(this);
        this.rgtpCommandExecutor = new RgtpCommandExecutor(
                WorldGuard.getInstance().getPlatform().getRegionContainer(),
                this.getConfig().getBoolean("is-region-tab-complete")
        );
        getCommand("rgtp").setExecutor(rgtpCommandExecutor);
        getCommand("rgtp").setTabCompleter(rgtpCommandExecutor);
    }

}
