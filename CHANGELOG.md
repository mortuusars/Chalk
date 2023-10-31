### 1.6.3
- Fixed glow uses being consumed twice per one mark drawn.
- Fixed glow disappearing when playing on dedicated server. 

### 1.6.2
- Symbol Selection screen is now using a key bind for inventory instead of hardcoded E key.
- Fixed Japanese localization error that prevented it to work correctly.

### 1.6.0
- Internal changes to allow adding more colors.

### 1.5.1
- Fixed crash when drawing a symbol from offhand. 

### 1.5.0
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

### WARNING - All arrow marks, drawn in previous versions of the mod, will be changed to a dot due to some changes to Chalk Mark blockstate. 

### 1.4.0 - 2023-02-10

- Added Chalk Box quick change selected chalks: 
  - Works by Shift+Clicking while not looking at a block.
  - Shifts chalks inside the box to the left.
- Improved compatibility with mods that modify damaging items.
  - Fixes Forbidden&Arcanus Eternal modifier not preventing damage to the chalk.
- Mod sounds now use unique SoundEvents: fixes subtitles and allows changing sounds in a resource pack.

### 1.3.2 - 2022-08-22

- Reduced chalk spawn chance in chests.
- Fixed stone spawning instead of chalk in loot tables.

### 1.3.1 - 2022-08-11

- Fixed sometimes not being able to draw a mark. (Minecraft has multiple types of air. Who would have thought.)

### 1.3.0 - 2022-07-31 - 1 in a MILLION