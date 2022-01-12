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
    private int SearchRadius = 5;
    private String MasterChest = "[ChestMain]";
    private String StorageChest = "[ChestStorage]";
    private boolean SortToAnyEmptySlots = false;

    @EventHandler
    public void SignRightClick(PlayerInteractEvent event) //TODO event gets fired if sign is destroyed.
    {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block == null)
        {
            return;
        }
        if (!IsSignValid(block.getType()))
        {
            return;
        }
        Sign sign = (Sign) block.getState();
        String[] signContent = sign.getLines();
        for (String content : signContent)
        {
            if (content.equalsIgnoreCase(MasterChest))
            {
                Chest mainChest = GetChestFromBelowSign(block.getLocation());
                if (mainChest == null)
                {
                    player.sendMessage(ChatColor.RED + "No chest found. Is the chest directly under the sign?");
                    return;
                }
                Collection<Sign> signs = SearchChunksForStringOnSign(
                        GetSurroundingChunks(block.getChunk(), SearchRadius),
                        StorageChest);
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
                        String locString = GetCoordinateStringFromLocation(loc);
                        player.sendMessage(ChatColor.RED + StorageChest + " sign has no Chest! " +
                                ChatColor.YELLOW + "Location:" + locString);
                        continue;
                    }
                    SortItems(mainChest, storageChest);
                }
                continue;
            }
            //Player right clicked a storage sign
            if (content.equalsIgnoreCase(StorageChest))
            {
                Chest StorageChest = GetChestFromBelowSign(sign.getLocation());
                if (StorageChest == null)
                {
                    player.sendMessage(ChatColor.RED + "No chest found. Is the chest directly under the sign?");
                    return;
                }
                Collection<Sign> signs = SearchChunksForStringOnSign(GetSurroundingChunks(
                        block.getChunk(), SearchRadius)
                        , MasterChest);
                if (signs.isEmpty())
                {
                    player.sendMessage(ChatColor.RED + MasterChest +
                            ChatColor.YELLOW + " Sign not found. Is the sign in within "
                            + ChatColor.RED + SearchRadius + ChatColor.YELLOW + " chunks?");
                    return;
                }
                for (Sign s : signs)
                {
                    player.sendMessage(ChatColor.GREEN + "Found Sign:"
                            + MasterChest + " at:" + ChatColor.YELLOW + GetCoordinateStringFromLocation(s.getLocation()));

                }
            }
        }
    }

    private String GetCoordinateStringFromLocation(Location location)
    {
        String str;
        str = location.getX() + " " + location.getY() + " " + location.getZ();
        return str;
    }

    private void SortItems(Chest mainChest, Chest storageChest)
    {
        for (ItemStack item : mainChest.getInventory().getContents())
        {
            if (item == null) //Empty slot
            {
                continue;
            }
            List<ItemStack> StorageChestAvailableItemStacks =
                    getValidStorageItemStacks(storageChest, item.getType());

            while (item.getAmount() > 0 && !StorageChestAvailableItemStacks.isEmpty())
            {

                int stackCount = item.getAmount();

                ItemStack storageItem = StorageChestAvailableItemStacks.get(0);
                int availableRoom = storageItem.getMaxStackSize() - storageItem.getAmount();
                int difference = stackCount - availableRoom;

                if (stackCount > availableRoom)
                {
                    item.setAmount(difference);
                    storageItem.setAmount(availableRoom + difference);
                    StorageChestAvailableItemStacks.remove(0);
                    continue;
                }
                item.setAmount(0);
                storageItem.setAmount(storageItem.getAmount() + stackCount);
                StorageChestAvailableItemStacks.remove(0);
            }
            if (ChestContainsSameMaterial(mainChest, storageChest))
            {
                SortToEmptySlots(mainChest, storageChest, item);
            }
        }
    }

    private List<ItemStack> getValidStorageItemStacks(Chest storageChest, Material mat)
    {
        Inventory storageChestInv = storageChest.getInventory();
        List<ItemStack> StorageChestAvailableItemStacks = new ArrayList<>();
        for (ItemStack storageChestItemStack : storageChestInv.getContents())
        {
            if (storageChestItemStack == null)
            {
                continue;
            }
            if (storageChestItemStack.getAmount() < storageChestItemStack.getMaxStackSize()
                    && storageChestItemStack.getType() == mat)
            {
                StorageChestAvailableItemStacks.add(storageChestItemStack);
            }
        }
        return StorageChestAvailableItemStacks;
    }

    private void SortToEmptySlots(Chest mainChest, Chest storageChest, ItemStack item)
    {
        if (ChestHasEmptySpace(storageChest) && MaterialIsInChest(item.getType(), storageChest))
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

    private boolean MaterialIsInChest(Material material, Chest storageChest)
    {
        for (ItemStack mainItemStack : storageChest.getInventory().getContents())
        {
            if (mainItemStack == null)
                continue;
            if (material == mainItemStack.getType())
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

    private Collection<Sign> SearchChunksForStringOnSign(Collection<Chunk> chunks, String str)
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
                        if (message.equalsIgnoreCase(str))
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

    public void LoadChestSettings(int searchRadius, String mainChest, String storageChest, boolean sortToAnyEmptySlots)
    {
        StorageChest = storageChest;
        SearchRadius = searchRadius;
        MasterChest = mainChest;
        SortToAnyEmptySlots = sortToAnyEmptySlots;
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
