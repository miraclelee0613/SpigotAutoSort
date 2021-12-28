# SpigotAutoSort

## Spigot AutoSort plugin
This plugin is an AutoSorting plugin which works by creating a "dumping chest" with other multiple connected chests which act as storage, 
once the sign above the chest is interacted with (by right clicking) the contents of the chest will be sorted into chests designated as storage.

## Features
- Sign interaction to trigger sorting
- Dumping chest - The player can dump all their items into a single location and they will sorted into designated storage chests
- Automatic sorting to chests that contain the same item types (e.g. all stone blocks will be sorted into chests which contain stone blocks)
- Option to sort to any empty slot in chests
- Configurable Search distance around dumping chest (in config.yml)
- Configurable sign names (in config.yml)

## Installation
Drag and drop the AutoSort.jar into your Spigot plugin folder.

## How to use?
Place a sign onto a wall with a chest underneath with the following text '[ChestMain]' to designate a dumping chest.
To setup Storage chests use '[ChestStorage]' and be within (default 5) chunks of the dumping chest. (You can right click storage signs to verify it is in within range)

## Config options

The default config looks like the following

settings:
  valid_signs: #If there are additional signs added to minecraft you should be able to add them here.
    OAK_WALL_SIGN:
      valid: true
    SPRUCE_WALL_SIGN:
      valid: true
    BIRCH_WALL_SIGN:
      valid: true
    JUNGLE_WALL_SIGN:
      valid: true
    ACACIA_WALL_SIGN:
      valid: true
    DARK_OAK_WALL_SIGN:
      valid: true
    CRIMSON_WALL_SIGN:
      valid: true
    WARPED_WALL_SIGN:
      valid: true
  ChestSettings: # Contains all relevant chest settings
    MasterChest: # This is the name of the main storage chest that is used for dumping items into.
      name: "[ChestMain]"
    StorageChest: # This is the name of the storage chest that items will be put into
      name: "[ChestStorage]"
    SearchRadius: ## WARNING  Setting this too high may cause strain on the server ##
      range: 5 # This is the amount chunks around the player that will be searched to find storage chests.
    SortToAnyEmptySlots: false # This will add items into any chest with space.
