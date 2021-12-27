package github.curtisdh.autosort;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AutoSort extends JavaPlugin
{
    public static AutoSort Instance;
    private onSignRightClick SignClickEvent;
    @Override
    public void onEnable()
    {
        // Plugin startup logic
        PrintWithClassName(this, "Starting...");
        SignClickEvent = new onSignRightClick();
        Instance = this;
        LoadConfig();
        SetupCommands();
        EventsSetup();
        PrintWithClassName(this, "Done...");
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }

    private void SetupCommands()
    {
        PrintWithClassName(this,"Setting up commands...");
    }

    private void EventsSetup()
    {
        PrintWithClassName(this,"Setting up Events...");
        getServer().getPluginManager().registerEvents(SignClickEvent,this);
    }

    public void LoadConfig()
    {
        PrintWithClassName(this, "Loading Config...");
        saveDefaultConfig();
        Map<String, BlockData> blockData = new HashMap<>();
        for (String key : getConfig().getConfigurationSection("settings.valid_signs").getKeys(false))
        {
            BlockData data;
            Object validSignObj = getConfig().getConfigurationSection("settings.valid_signs."+key)
                    .getValues(true).get("valid");

            boolean validSign = validSignObj.toString().equalsIgnoreCase("true");
            key = key.toUpperCase(Locale.ROOT);
            data = new BlockData(validSign, Material.valueOf(key));
            PrintWithClassName(this,"Loading Config:"+key+" "
                    +data.validSign);
            blockData.put(key,data);
        }
        SignClickEvent.SetBlockDataMap(blockData);
        // Might be a better way to load this.
        String searchRadiusObj = getConfig().getString("settings.ChestSettings.SearchRadius.range");
        String storageChestObj = getConfig().getString("settings.ChestSettings.StorageChest.name");
        String masterChestObj = getConfig().getString("settings.ChestSettings.MasterChest.name");
        SignClickEvent.LoadChestSettings(Integer.parseInt(searchRadiusObj),masterChestObj,storageChestObj);
        saveConfig();
        PrintWithClassName(this, "-Loaded Config-");
    }

    public static void PrintWithClassName(Object ClassObject, String str)
    {
        String response = ClassObject.getClass().getName() + "::" + str;
        System.out.println(response);
    }
}
