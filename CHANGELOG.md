# Changelog

## 2.0.1 - 2026-09-06
- Added config option to disable glowing mark emitting particles

## 2.0.0 - 2026-08-24
Note:
_This release heavily changes existing chalk systems._<br>
_Update process should be smooth for the most part, but some marks could be lost in the process._

_In any case: **backup your world before updating**._

---

Chalk
- 16 chalks of specific colors are replaced with a single, dyeable **Chalk** item
  - Chalks are dyed similarly to a **Leather Armor**, allowing many combinations of colors
  - _Old items are still in the mod and will be converted to the new system on use_
- Chalk is now edible
  - Chalk doesn't have any nutritional value but gives 30 seconds of Strength effect
- Chalk colors in creative inventory and in JEI are now sorted the same way as in vanilla

Mark
- Mark block now supports up to 6 marks in a single block space, one for each face
  - This allows drawing in corners, for example
- Drawing the same mark, but with a different color on top of already existing mark will blend their colors together
- Improved orientation of marks drawn on the bottom side of a block

Symbols
- Added new symbols:
  - **Back** - unlocked by `This Way` advancement
  - **Sword**, **Shovel**, **Axe**, **Hoe** - unlocked by `Isn't It Iron Pick` advancement
  - **Sun** - unlocked by the new `Consumed By the Light` advancement
  - **Moon** - unlocked by `Alone In the Darkness` advancement
  - **Star** - unlocked by the new `Guiding Star` advancement
  - **Note** - unlocked by `Sound of Music` advancement
  - **Hook** - unlocked by `Fishy Business` advancement
  - **Bottle** - unlocked by `Local Brewery` advancement
  - **Face** - unlocked by `Hired Help` advancement
  - **Grass** - unlocked by the new `Painting the Grass` advancement
  - **10** exclusive symbols for patreon supporters (Gold+): **Cat**, **Dog**, **Dino**, **Crown**, **Coin**, **Gem**, **Cake**, **Lambda**, **Flame** and **Smirk**
- Symbols are now data-driven
- Overhauled the symbol selection interface:
  - Added proper layout and scrolling to support any number of symbols
  - Added symbol groups
  - Added smooth animations
  - Symbol name is now shown in a tooltip, instead of the action bar
  - Overlay now shows all symbols if player is in creative mode, instead of only unlocked
- **Skull** symbol unlocking is changed from `Bound by Bone` to `Sniper Duel` advancement
- Changed sprite of the **House** symbol
- Improved player's view when choosing a symbol (fixed a couple of jittering issues)

Chalk Box
- Interface:
  - Changed amount of slots from **8** to **9**, arranged in a **3x3** grid
  - Changed glowings slot to be on the right side and updated glow-related visuals
  - Added tooltip to the glow bar to show how much glow is left
- Reworked chalk selection mechanic:
  - Any slot with chalk can now be selected to draw with, instead of always the first one in order
  - Chalks are selected by clicking on the slot while holding [Alt]
- Item now shows durability bar of the selected chalk
- Item texture has been changed slightly
- Item texture now has a small sheen animation when the box has glow
- [Right-Clicking] the box in inventory without item now removes selected chalk, instead of opening the interface
- Changed **Chalk Box** crafting recipe to require less paper
- Added **Chalk Box** crafting recipe from Cardboard, if Create is installed

Advancements
- Added `Consumed By The Light` and `Guiding Star` advancements
- `Alone In The Darkness` advancement is no longer hidden
- Added Trial Chambers to the list of structures that unlock `This Way` advancement
- Added `chalk:mark_glowing` advancement trigger
- Removed `Bound By Bone` advancement

Misc
- Updated mod icon
- Bundled Mortaar library with the mod
  - Mortaar is a library mod that will be used to aid in mod development and reduce code duplication
  - As a player, you will not see any difference, apart from an additional mod in the mod menu

Config
- Added `symbol_unlocking` server config option
- Added `chalk_box.show_durability_bar` server config option
- Added `symbol_selection_groups_sorting` client config option
- Added server config options related to new chalk behavior
- Moved `ChalkBoxGlowingEnabled` and `ChalkBoxAmountPerGlowingItem` options from common to server config
- Changed casing of all config options from 'PascalCase' to 'snake_case'
- Removed whole `Symbols` category in common config; All symbol properties are now data-driven
- Removed `GlowingMarkLightLevel` common config option; Glowing marks now always have a light level of 5
- Removed `ChalkDurability` common config option; Chalk durability can be configured with `minecraft:max_damage` component on the item
- Removed `ChalkSymbolOffsets` client config option; Offset is part of the symbol definition.

## 1.6.12 - 2026-06-03
- Fixed equality checking of `chalk:chalk_box_contents` component.

## 1.6.11 - 2025-12-23
- Fixed potential exploit
- Updated localization files

## 1.6.10 - 2025-06-17
- Updated localization files.

## 1.6.9 - 2024-12-10
- Chalks can now be enchanted if added to enchantment tags.
- Chalks are now using default item stack damage logic and play default sound when broken
  - Removed `item.chalk.broken` sound event.

## 1.6.8 - 2024-09-15
- Disabled log messages for wrong model data, which are causing log spam with JourneyMap installed.   

## 1.6.7 - 2024-08-04
- Fixed crash when trying to insert another Chalk Box item into a Chalk Box. 

## 1.6.6 - 2024-07-28
- Fixed crash caused by loading client-only class on the server.
- Fixed Chalk Box contents tooltip not displaying. 

## 1.6.5 - 2024-07-28
- Update to 1.21. 
- Chalk Box contents are now showing in the tooltip.
- Improved emissive rendering of glowing marks. 

## 1.6.4
- Chalk marks now have proper names with mods like WAILA or WTHIT.
- Chalks will again generate in Abandoned Mineshafts. They've been missing for a year, and no one noticed.
- Chalks of unsupported colors will no longer be created when other mod adds their colors to vanilla DyeColor list. Dye Depot, for example.

## 1.6.3
- Fixed glow uses being consumed twice per one mark drawn.
- Fixed glow disappearing when playing on dedicated server. 

## 1.6.2
- Symbol Selection screen is now using a key bind for inventory instead of hardcoded E key.
- Fixed Japanese localization error that prevented it to work correctly.

## 1.6.1
- Fixed crash when opening a loot chest. 

## 1.6.0
- Internal changes to allow adding more colors.

## 1.5.1
- Fixed crash when drawing a symbol from offhand. 

## 1.5.0
- Added new symbols: House, Checkmark, Heart, Skull, Pickaxe
  - Symbol selection UI is opened by using Chalk/Chalk Box while sneaking. 
  - Some symbols are unlocked by completing an advancement, others are unlocked by default. (_configurable_)

- Chalk Box
  - Chalk Box can now be opened by Right-Clicking it in inventory.
  - You can now insert chalks in Chalk Box by right-clicking it with item (same as bundles).
  - Using Chalk Box on the block while it is empty will now open the GUI instead of doing nothing.
  - Added sounds to closing and adding glow.

- Tags:
  - Added `chalk:chalk_cannot_draw_on` block tag which controls what blocks isn't suitable for drawing marks on.
  - Changed naming to be inline with vanilla: `chalk:chalk` -> `chalk:chalks`, etc.
  - Added chalks to `forge:chalks` tag.

- Marks are now properly placed and rotated when generated as part of a structure.
- Player's are now holding chalks pointing forward.
- Removed ability to draw already glowing marks by holding glow item in offhand. You'll need to click again to apply glow. 
- Glowing marks will now glow properly when Rubidium is installed.

- Tweaked textures
- Added several advancements.
- Added config for mark rotation offset.

## WARNING - All arrow marks, drawn in previous versions of the mod, will be changed to a dot due to some changes to Chalk Mark blockstate. 

## 1.4.0 - 2023-02-10

- Added Chalk Box quick change selected chalks: 
  - Works by Shift+Clicking while not looking at a block.
  - Shifts chalks inside the box to the left.
- Improved compatibility with mods that modify damaging items.
  - Fixes Forbidden&Arcanus Eternal modifier not preventing damage to the chalk.
- Mod sounds now use unique SoundEvents: fixes subtitles and allows changing sounds in a resource pack.

## 1.3.2 - 2022-08-22

- Reduced chalk spawn chance in chests.
- Fixed stone spawning instead of chalk in loot tables.

## 1.3.1 - 2022-08-11

- Fixed sometimes not being able to draw a mark. (Minecraft has multiple types of air. Who would have thought.)

## 1.3.0 - 2022-07-31 - 1 in a MILLION