package net.pinaz993.simpleshelves;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedstoneBookItem extends WrittenBookItem {

    private final String author;

    public RedstoneBookItem(Item.Settings settings) {
        super(settings);
        this.author = "Pinaz993";
    }

    public RedstoneBookItem(Item.Settings settings, String author){
        super(settings);
        this.author = author;

    }

    @Override
    public Text getName(ItemStack stack) {
        return new TranslatableText("Redstone");
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("book.byAuthor", this.author).formatted(Formatting.GRAY));
        tooltip.add(new TranslatableText("book.generation.0").formatted(Formatting.GRAY));
    }
}
