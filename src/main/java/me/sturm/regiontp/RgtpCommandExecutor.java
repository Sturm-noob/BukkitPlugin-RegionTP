package me.sturm.regiontp;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RgtpCommandExecutor implements CommandExecutor, TabCompleter {

    private RegionContainer regionContainer;
    private boolean isRegionTabComplete;

    public RgtpCommandExecutor(RegionContainer regionContainer, boolean isRegionTabComplete) {
        this.regionContainer = regionContainer;
        this.isRegionTabComplete = isRegionTabComplete;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return Lang.HELP.sendMessage(sender);
        }
        Player player;
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                return Lang.ONLY_PLAYER.sendMessage(sender);
            }
            player = (Player) sender;
        }
        else {
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                return Lang.PLAYER_NF.sendMessage(sender, "%player%", args[1]);
            }
        }
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) {
            return Lang.RG_MANAGER_NF.sendMessage(sender);
        }
        ProtectedRegion region = regionManager.getRegion(args[0]);
        if (region == null) {
            return Lang.REGION_NF.sendMessage(sender, "%region%", args[0]);
        }
        Location location = getCenterOfRegion(player.getWorld(), region, player.getLocation().getPitch(), player.getLocation().getYaw());

        if (args.length == 1 && !sender.hasPermission("rgtp.self")) {
            return Lang.NO_PERMS.sendMessage(sender);
        }
        if (args.length == 2 && !sender.hasPermission("rgtp.others")) {
            return Lang.NO_PERMS.sendMessage(sender);
        }
        player.teleport(location);
        return Lang.TELEPORT.sendMessage(sender, "%region%", args[0]);
    }

    public Location getCenterOfRegion(World world, ProtectedRegion protectedRegion, float pitch, float yaw) {
        BlockVector3 min = protectedRegion.getMinimumPoint();
        BlockVector3 max = protectedRegion.getMaximumPoint();
        int x = min.getX() + (max.getX() - min.getX()) / 2;
        int z = min.getZ() + (max.getZ() - min.getZ()) / 2;
        double y = world.getHighestBlockYAt(x, z);
        return new Location(world, x, y, z, pitch, yaw);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();

        if (this.isRegionTabComplete && args.length == 1 &&
                (sender.hasPermission("rgtp.self") || sender.hasPermission("rgtp.others"))) {
            if (!(sender instanceof Player)) {
                return result;
            }
            World world = ((Player) sender).getWorld();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
            if (regionManager != null) {
                result.addAll(regionManager.getRegions().keySet());
            }
        }
        if (args.length == 2 && sender.hasPermission("rgtp.others")) {
            result.addAll(Bukkit.matchPlayer(args[1]).stream().map(Player::getName).toList());
        }
        return result;
    }
}
