# Fated Inventory
## The Altar of Fate
![The Altar of Fate along with a loot chest in the temple structure in a Plains Village](https://cdn.modrinth.com/data/cached_images/6257546cf1f2e7bfb6141a197c386cace8601b74.png)
Fated Inventory is centered around one block, the Altar of Fate, which can be found in villages or crafted. When you interact with the Altar of Fate, the items currently in your inventory will be sealed to your fate. If you then die, any items that were sealed to your fate **that remain in your inventory when you died** will not be dropped. Instead, they remain as part of your fate and can be collected by interacting with the Altar of Fate again (it does not need to be the same altar). This also works with experience. If you want to clear your fate for whatever reason, you can interact with the Altar of Fate using shears.

### Charging
By default, the Altar of Fate can be used at no cost other than obtaining it. There is a configuration option to enable a charging mechanic similar to the respawn anchor. Interacting with the Altar of Fate using Glowstone will increase its charge by 1, for a maximum of 4 charges (the current charge level is indicated by a dial on the altar, similar to the respawn anchor). When enabled, you will need at least one charge to seal your fate, and retrieving your items will cost one charge.

### Container Items
Items that store other items, such as Shulker Boxes, Bundles, and Backpacks are too complex to be fully sealed with your fate. Thus they will only be sealed with your fate if their contents do not change in between being sealed and dying. If you ever can't find an item in the Altar of Fate, check to see if it was dropped at your death location instead.

## Compatibility
Fated Inventory includes compatibility with [Trinkets](https://modrinth.com/mod/trinkets), [Curios](https://modrinth.com/mod/curios), and [Accessories](https://modrinth.com/mod/accessories) - items placed in the inventory slots added by those mods should correctly be saved and retrieved using the Altar of Fate.

Fated Inventory may be incompatible with some gravestone mods, from testing [You're in Grave Danger](https://modrinth.com/mod/yigd) and the graves from [Quark Oddities](https://modrinth.com/mod/quark-oddities) works as expected.

Fated Inventory may have compatibility issues with mods that alter death mechanics.

## Configuration
Fated Inventory is highly customizable, providing several configuration options and datapack item tags. Note that not all of these configuration options are present on all versions, in particular the 1.20.1 versions are missing some features.
### Config File
The config file (`fated_inventory.json`) is not automatically synced between clients, make sure that the server and all clients have identical config files, otherwise unexpected behavior may occur.
- `fateStoresXp` - whether experience is saved when your fate is sealed, defaults to true
- `fatedAltarRequiresCharges` - whether the Altar of Fate requires charges to function, defaults to false
- `showMessageOnRespawn` - whether to show a message when you respawn with items in the Altar of Fate that can be retrieved - defaults to true
- `generateAltarBuildingsInVillages` - whether buildings containing an Altar of Fate should be added to village generation - defaults to true
- `villageAltarBuildingWeight` - weight for generating village altar buildings - higher numbers are more common, must be an integer - defaults to 2
- `anyNonstackableAllowsModifiedComponents` - whether all nonstackable items should persist in the Altar of Fate even if their components have changed between being added and retrieved - defaults to false
- `anyDurabilityItemAllowsModifiedComponents` - whether all items with durability bars should persist in the Altar of Fate even if their components have changed between being added and retrieved - defaults to true
- `experimentalFlattenContainerItems` - **experimental**, whether to attempt to flatten container items (Shulker Boxes, Bundles, Backpacks, etc.) to better track items in them - defaults to false
### Item Tags
- `fated_inventory:charges_fated_altar` - items that can be used to charge to Altar of Fate when charging is enabled - defaults to Glowstone
  - Note that this item will also be generated in the loot chests in some of the village altar buildings added by this mod - to disable that, modify the loot table `fated_inventory:chests/altar_building`
- `fated_inventory:not_saved_in_altar` - items that will not be saved with the Altar of Fate - defaults to a few appropriate items from other mods
- `fated_inventory:allow_modified_components` - items that considered to be the same item in the Altar of Fate even if their item components (durability, enchantments, etc.) have changes - only works for non-stackable items - defaults to tools, armor, and enchantable items (as determined by the `c:tools`, `c:armors`, and `c:enchantables` item tags)
### Enchantments Tags
- `fated_inventory:not_saved_in_altar` - enchantments that will prevents the enchanted item from being saved with the Altar of Fate - defaults to the soulbound enchantments from several mods that add it