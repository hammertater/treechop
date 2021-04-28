# Compatibility

There are three ways to change how items behave when chopping:
1. [Overrides](#overrides) (*users*)
1. [Blacklist](#blacklist) (*users*)
1. [API](#api) (*developers*)

## Overrides

In the `treechop-common.toml` configuration file, under the `compatibility.general` section, there is a list named `itemsToOverride`. This list is used for two purposes: 1) to disable item behaviors that conflict with the chopping mechanic, and 2) to change the number of chops that trigger when breaking a log with specific items. Adding an item to the list will disable its default block-breaking behavior *when breaking log blocks* (see [Caveats](#caveats)) and, unless specified, the item's "number of chops" will be 1.

Maybe another mod adds an item called `coolmod:super_axe` that fells whole trees by breaking a single block. Maybe we'd rather have that super axe use the chopping mechanic instead. Or maybe that super axe actually conflicts with TreeChop and causes a crash. In any case, we can override the item's behavior like so:

`itemsToOverride = ["coolmod:super_axe"]`

Since it is a *super* axe, though, we can allow it to do more chops than usual by adding a `chops=N` qualifier:

`itemsToOverride = ["coolmod:super_axe?chops=2"]`

Now, breaking a log block with the super axe will cause two chops instead of one! But what if chopping is disabled? The super axe will revert to its default behavior of breaking the whole tree. We can stop that by adding an `always` qualifier:

`itemsToOverride = ["coolmod:super_axe?chops=2,override=always"]`

With the `always` qualifier, the super axe's default behavior will always be suppressed when breaking log blocks, even when chopping is disabled. Some items actually have non-conflicting behavior, though, that we don't want to suppress; maybe there's an axe that makes a pretty effect when breaking blocks. In this case, we can add a `never` qualifier:

`itemsToOverride = ["coolmod:pretty_axe?chops=2,override=never"]`

Now the pretty axe will still make its pretty effects when chopping, and each break will trigger two chops.

We aren't limited to only altering single items. Whole item tags and mods can be selected too.
- To override all items containing a certain tag: `itemsToOverride = ["#modname:tag"]`
- To override all items belonging to a certain mod: `itemsToOverride = ["@modname"]`

The `chops=N`, `always`, and `never` qualifiers also work when specifying tags and mods. For example, `itemsToOverride = ["#modname:tag?chops=3,override=never", "@modname?override=always"]`.

### Caveats

Some items might cause unexpected behavior when added to the overrides list.

- In Java, the `onBlockStartBreak` method will not be called for items in the overrides list *when breaking a log block*. This does not suppress any other item methods, such as `onBlockDestroyed`. Items that do not rely on `onBlockStartBreak` will require special support (see [API](#api)).
- Items that have special support for TreeChop (see [API](#api)) cannot be blacklisted or overridden

## Blacklist

Items added to the blacklist will never trigger chopping mechanics. The same syntax used for [Overrides](#overrides) can be used when specifying items to blacklist. That is, we can blacklist single items (`minecraft:dirt`), items with a certain tag (`forge:saws`), and all items from a certain mod (`@modname`).

The blacklist can be changed to a whitelist by changing `blacklistOrWhitelist = BLACKLIST` to `blacklistOrWhitelist = WHITELIST`. When using a whitelist, only the specified items can be used to chop. This could be used to, for example, require players to use an axe in order to chop trees.

## API

An API is provided to allow mod developers to interface directly with TreeChop's chopping mechanics. At this time, the best ways to access the TreeChop API are to use [CurseForge API](https://support.curseforge.com/en/support/solutions/articles/9000197321-curseforge-api) or [Curse Maven](https://www.cursemaven.com/).

### Items

`Item`s can take advantage of TreeChop's chopping mechanics by implementing the [IChoppingItem](https://github.com/hammertater/treechop/blob/main/src/main/java/ht/treechop/api/IChoppingItem.java]) interface. TreeChop looks for items that implement this interface and calls their `getNumChops(ItemStack, BlockState)` method to determine the number of chops to be performed.

### Blocks

`Block`s can made choppable by implementing the [IChoppableBlock](https://github.com/hammertater/treechop/blob/main/src/main/java/ht/treechop/api/IChoppableBlock.java) interface.

### Events

TreeChop fires [ChopEvents](https://github.com/hammertater/treechop/blob/main/src/main/java/ht/treechop/common/event/ChopEvent.java) using Forge's events system. There are two sub-events:
- `StartChopEvent` fires before chopping begins and can be used to change the number of chops. Canceling this event will prevent chopping.
- `FinishChopEvent` fires when chopping has finished.
