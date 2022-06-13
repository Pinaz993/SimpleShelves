package net.pinaz993.simpleshelves;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An item that looks like a written book, but cannot be opened or placed in a lectern. When crafted, it appears to have
 * been written by the player who crafted it, and to be the original copy of said book.
 * This item exists to serve as the activation mechanism for shelf analog redstone behavior, as defined in
 * ShelfInventory and ShelfEntity.
 */
public class RedstoneBookItem extends WrittenBookItem {

    public RedstoneBookItem(Item.Settings settings) {super(settings);} // Default constructor. Nothing special.

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("Redstone"); // This item is always named 'Redstone'.
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return TypedActionResult.fail(user.getStackInHand(hand)); // Nothing happens when the player uses this item.
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS; // Nothing happens when the player uses this item on a block, including a lectern.
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbtCompound = stack.getNbt(); // Grab NBT if the stack has it, or null if not.
        if (nbtCompound != null) { // If the stack had NBT to grab...
            String string = nbtCompound.getString("author"); // Grab the NBT string value for the value 'author'.
            if (!string.isEmpty()) { // If THAT exists...
                // Add a tool tip that proclaims the author's name.
                tooltip.add((Text.translatable("book.byAuthor", string)).formatted(Formatting.GRAY));
            // Otherwise, add a tooltip that proclaims the author to be 'Pinaz993'.
            } else tooltip.add((Text.translatable("book.byAuthor", "Pinaz993")).formatted(Formatting.GRAY));
        // Otherwise, add a tooltip that proclaims the author to be 'Pinaz993'.
        } else tooltip.add(( Text.translatable("book.byAuthor", "Pinaz993")).formatted(Formatting.GRAY));
        // Add a tooltip that says this item is the original copy, much like a book would.
        tooltip.add( Text.translatable("book.generation.0").formatted(Formatting.GRAY));
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        NbtCompound nbt = new NbtCompound(); // Make a new NBT compound.
        // Set the value associated with the 'author' key to the name of the player who crafted the item.
        nbt.putString("author", player.getName().getString());
        stack.setNbt(nbt); // Apply the NBT to the item.
    }
}
