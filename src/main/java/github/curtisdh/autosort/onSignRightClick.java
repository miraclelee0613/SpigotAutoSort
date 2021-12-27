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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class onSignRightClick implements Listener
{
    private Map<String, BlockData> blockDataMap;
    private int SearchRadius = 5; //TODO put search radius in config
    private String MasterChest = "[ChestMain]";
    private String StorageChest = "[ChestStorage]";
    private boolean SortToAnyEmptySlots = false;

    //TODO when right clicking on a storage chest attempt to find the master chest and relay that info back to player
    @EventHandler
    public void SignRightClick(PlayerInteractEvent event) //TODO event gets fired if sign is destroyed.
    {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (!IsSignValid(block.getType()))
        {
            return;
        }
        AutoSort.PrintWithClassName(this, "Valid Sign");
        Sign sign = (Sign) block.getState();
        String[] signContent = sign.getLines();
        for (String content : signContent)
        {
            AutoSort.PrintWithClassName(this, content);
            if (content.equalsIgnoreCase(MasterChest))
            {
                Chest mainChest = GetChestFromBelowSign(block.getLocation());
                if (mainChest == null)
                {
                    player.sendMessage(ChatColor.RED + "No chest found. Is the chest directly under the sign?");
                    return;
                }
                Collection<Sign> signs = SearchChunksForRelevantSigns(
                        GetSurroundingChunks(block.getChunk(), SearchRadius));
                if (signs.isEmpty())
                {
                    player.sendMessage(ChatColor.RED + "No nearby " + StorageChest + " signs");
                    return;
                }
                for (Sign s : signs)
                {
                    Chest storageChest = GetChestFromBelowSign(s.getLocation());
                    if (storageChest == null)
                    {
                        Location loc = sign.getLocation();
                        String locString = loc.getX() + " " + loc.getY() + " " + loc.getZ();
                        player.sendMessage(ChatColor.RED + StorageChest + " sign has no Chest! " +
                                ChatColor.YELLOW + "Location:" + locString);
                        continue;
                    }
                    //TODO Only move items if storage chest has enough room
                    //TODO only move items from main chest that already exist in the storage chest


                    //determine if an empty slot is available
                    //determine what items can be added to existing stacks & the quantity


                    Inventory storageChestInv = storageChest.getInventory();
                    Collection<ItemStack> StorageChestAvailableItemStacks = new HashSet<>();
                    for (ItemStack storageChestItemStack : storageChestInv.getContents())
                    {
                        if (storageChestItemStack == null)
                        {
                            continue;
                        }
                        if (storageChestItemStack.getAmount() < storageChestItemStack.getMaxStackSize())
                        {
                            StorageChestAvailableItemStacks.add(storageChestItemStack);
                        }
                    }


                    for (ItemStack item : mainChest.getInventory().getContents())
                    {
                        if (item == null) //Empty slot
                        {
                            continue;
                        }
                        int stackCount = item.getAmount();
                        //Calc difference
                        for (ItemStack storageItem : StorageChestAvailableItemStacks)
                        {
                            if (storageItem.getType() == item.getType())
                            {
                                int difference = storageItem.getMaxStackSize() - storageItem.getAmount();
                                item.setAmount(stackCount - difference);
                                storageItem.setAmount(storageItem.getAmount() + difference);
                            }
                        }
                        SortToEmptySlots(mainChest, storageChest, item);
                    }

                }

            }
        }
    }

    private void SortToEmptySlots(Chest mainChest, Chest storageChest, ItemStack item)
    {
        if (ChestHasEmptySpace(storageChest))
        {
            if (ChestContainsSameMaterial(storageChest, mainChest))
            {
                storageChest.getInventory().addItem(item);
                mainChest.getInventory().removeItem(item);
            }
            if (SortToAnyEmptySlots)
            {
                storageChest.getInventory().addItem(item);
                mainChest.getInventory().removeItem(item);
            }
        }
    }

    private boolean ChestHasEmptySpace(Chest chest) // Probably need to rework this, doesnt determine how much room
    {
        for (ItemStack storageStack : chest.getInventory().getContents())
        {
            if (storageStack == null)
            {
                return true;
            }
        }
        return false;
    }

    private boolean ChestContainsSameMaterial(Chest mainChest, Chest storageChest)
    {
        for (ItemStack item : storageChest.getInventory().getContents())
        {
            if (item == null)
                continue;
            for (ItemStack mainItemStack : mainChest.getInventory().getContents())
            {
                if (mainItemStack == null)
                    continue;
                if (item.getType() == mainItemStack.getType())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Collection<Sign> SearchChunksForRelevantSigns(Collection<Chunk> chunks)
    {
        Collection<Sign> signs = new HashSet<>();
        for (Chunk chunk : chunks)
        {
            for (BlockState blockState : chunk.getTileEntities())
            {
                if (blockState instanceof Sign)
                {
                    Sign sign = (Sign) blockState.getBlock().getState();
                    for (String message : sign.getLines())
                    {
                        if (message.equalsIgnoreCase(StorageChest))
                        {
                            signs.add(sign);
                        }
                    }
                }
            }
        }
        return signs;
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

    public void LoadChestSettings(int searchRadius, String mainChest, String storageChest)
    {
        StorageChest = storageChest;
        SearchRadius = searchRadius;
        MasterChest = mainChest;
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
