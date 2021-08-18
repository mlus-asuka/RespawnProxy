package vip.fubuki.rm.respawnproxy;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

//import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

//import java.util.Collection;
import java.util.UUID;

public final class RespawnProxy extends JavaPlugin implements Listener {
    String defaultworld;
    Player teleported;
    Boolean teleport;
    Location teleportation;
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        defaultworld=getConfig().getString("defaultworld");
        this.getServer().getPluginManager().registerEvents(this,this);
        getLogger().info("重生代理插件开始工作。");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandSendEvent event) {
       String commands = String.valueOf(event.getCommands());
       Player p=event.getPlayer();
        if(commands.equals("rp reload")){
            reloadConfig();
            defaultworld=getConfig().getString("defaultworld");
            p.sendMessage("已重新加载配置文件");
        }
        else if(commands.equals("rp setworld")){
            getConfig().set("defaultworld",p.getWorld().getName());
            saveConfig();
            p.sendMessage("已将当前所在世界"+p.getWorld().getName()+"设为默认复活世界。");
        }
    }



    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        String commands = event.getCommand();
        if(commands.equals("rp reload")){
            reloadConfig();
            defaultworld=getConfig().getString("defaultworld");
            getLogger().info("已重新加载配置文件");
        }
        else if(commands.equals("rp setworld")) {
               getLogger().warning("非控制台命令!此命令仅限玩家使用。");
        }
    }



    @EventHandler(ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        int []position=new int[3];
        Block bed=event.getBed();
        Player who=event.getPlayer();
        UUID uuid=who.getUniqueId();
        Location location=event.getBed().getLocation();
        position[0]=bed.getX();
        position[1]=bed.getY();
        position[2]=bed.getZ();
        getConfig().set(uuid+".name",who.getName());
        getConfig().set(uuid+".X",position[0]);
        getConfig().set(uuid+".Y",position[1]);
        getConfig().set(uuid+".Z",position[2]);
        getConfig().set(uuid+".location",location);
        saveConfig();
        who.sendMessage("你的重生位置已被复活代理插件记录。");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        //FileConfiguration config = getConfig();
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        World world = p.getWorld();
        String worldname=world.getName();
        Location location;
        location= getConfig().getLocation(uuid + ".location");
        if(!worldname.equals(defaultworld)){
            p.sendMessage("你出生在了主世界之外，复活代理插件取消工作。");
        }
        else {
            if (location != null) {
                if (location.getX() != 0 || location.getY() != 0 || location.getZ() != 0) {
                    teleport=true;
                    teleported=p;
                    teleportation=location;
                    Timer timer = new Timer();
                    timer.run();
                }
            }
            else{
                p.sendMessage("你的重生位置未被复活代理插件记录或已丢失。");
            }
        }
    }

    public class Timer implements Runnable {
        private Thread t;

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                    if(teleport=true) {
                        teleported.teleport(teleportation);
                        teleported.sendMessage("你的重生位置已被复活代理插件矫正。");
                        teleport=false;
                    }


            } catch (InterruptedException e) {
                System.out.println("Thread  interrupted.");
            }

        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
