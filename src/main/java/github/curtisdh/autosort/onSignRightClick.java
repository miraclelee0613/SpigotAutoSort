package github.curtisdh.autosort;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class onSignRightClick implements Listener
{
    private Map<String, BlockData> blockDataMap;
    private int searchRadius = 5; //TODO put search radius in config
    private String MasterChest = "[ChestMain]";
    private String StorageChest = "[ChestStorage]";

    //TODO when right clicking on a storage chest attempt to find the master chest and relay that info back to player
    @EventHandler
    public void SignRightClick(PlayerInteractEvent event)
    {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (IsSignValid(block.getType()))
        {
            AutoSort.PrintWithClassName(this, "Valid Sign");
            Sign sign = (Sign) block.getState();
            String[] signContent = sign.getLines();
            for (String content : signContent)
            {
                AutoSort.PrintWithClassName(this, content);
                if (content.equalsIgnoreCase(MasterChest))
                {
                    Chest chest = GetChestFromBelowSign(block.getLocation());
                    if (chest == null)
                    {
                        player.sendMessage(ChatColor.RED + "No chest found. Is the chest directly under the sign?");
                        return;
                    }

                    Collection<Chunk> SurroundingChunks = GetSurroundingChunks(block.getChunk(), searchRadius);
                    for (Chunk chunk : SurroundingChunks)
                    {
                        for (BlockState blockState : chunk.getTileEntities())
                        {
                            if (blockState instanceof Sign)
                            {
                                Sign t = (Sign) blockState.getBlock().getState();
                                for (String message : t.getLines())
                                {
                                    if (message.equalsIgnoreCase("[ChestStorage]"))
                                    {
                                        Chest c = GetChestFromBelowSign(t.getLocation());
                                        if (c == null)
                                        {
                                            Location loc = t.getLocation();
                                            String locString = loc.getX() + " " + loc.getY() + " " + loc.getZ();
                                            player.sendMessage(ChatColor.RED + "ChestStorage sign has no Chest! " +
                                                    ChatColor.YELLOW + "Location:" + locString);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Collection<Chunk> GetSurroundingChunks(Chunk c, int searchRadius)
    {
        World world = c.getWorld();
        int baseX = c.getX();
        int baseZ = c.getZ();

        Collection<Chunk> chunksAroundPlayer = new HashSet<>();
        for (int i = 0; i < searchRadius; i++)
        {
            int[] offset = {-i, 0, i};
            for (int x : offset)
            {
                for (int z : offset)
                {
                    Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
                    chunksAroundPlayer.add(chunk);
                    AutoSort.PrintWithClassName(this, "Surrounding Chunk:" + chunk);
                }
            }
        }
        return chunksAroundPlayer;
    }

    private Chest GetChestFromBelowSign(Location loc)
    {
        Chest chest = null;
        try
        {
            chest = (Chest) loc.subtract(0, 1, 0).getBlock().getState();
        } finally
        {
            return chest;
        }
    }

    public void SetBlockDataMap(Map<String, BlockData> map)
    {
        blockDataMap = map;
    }

    private boolean IsSignValid(Material sign)
    {
        for (Map.Entry<String, BlockData> validSignEntries : blockDataMap.entrySet())
        {
            if (!validSignEntries.getValue().validSign)
            {
                return false;
            }
            if (validSignEntries.getValue().material == sign)
            {
                return true;
            }
        }
        return false;
    }
}
