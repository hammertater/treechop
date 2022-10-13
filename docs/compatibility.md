# Compatibility

There are two ways to change how items behave when chopping:
1. [Blacklist/whitelist items](#blacklist) (*users*)
1. [API](#api) (*developers*)

## Blacklist

Items added to the blacklist will never trigger chopping mechanics. You can blacklist single items (`minecraft:dirt`), items with a certain tag (`#forge:saws`), and all items from a certain mod (`@modname`).

The blacklist can be changed to a whitelist by changing `blacklistOrWhitelist = BLACKLIST` to `blacklistOrWhitelist = WHITELIST`. When using a whitelist, only the specified items can be used to chop. This could be used to, for example, require players to use an axe in order to chop trees.

## Caveats

- Items that have special support for TreeChop (see [API](#api)) cannot be blacklisted

## API

An API is provided to allow mod developers to interface directly with TreeChop's chopping mechanics. At this time, the best ways to access the TreeChop API are to use [CurseForge API](https://support.curseforge.com/en/support/solutions/articles/9000197321-curseforge-api) or [Curse Maven](https://www.cursemaven.com/).

### Items

`Item`s can take advantage of TreeChop's chopping mechanics by implementing the [IChoppingItem](https://github.com/hammertater/treechop/blob/main/src/main/java/ht/treechop/api/IChoppingItem.java]) interface. TreeChop looks for items that implement this interface and calls their `getNumChops(ItemStack, BlockState)` method to determine the number of chops to be performed.

### Blocks

`Block`s can made choppable by implementing the [IChoppableBlock](https://github.com/hammertater/treechop/blob/main/src/main/java/ht/treechop/api/IChoppableBlock.java) interface.

### Events

TreeChop fires [ChopEvents](https://github.com/hammertater/treechop/blob/main/src/main/java/ht/treechop/common/event/ChopEvent.java) using Forge's events system. There are two sub-events:
- `DetectTreeEvent` fires when deciding whether a block is part of a tree. It can be used to override leaves detection. Canceling this event will prevent tree detection.
- `StartChopEvent` fires before chopping begins and can be used to change the number of chops. Canceling this event will prevent chopping.
- `FinishChopEvent` fires when chopping has finished.

Some examples using these events are found in https://github.com/hammertater/treechop/tree/main/src/main/java/ht/treechop/common/compat.
