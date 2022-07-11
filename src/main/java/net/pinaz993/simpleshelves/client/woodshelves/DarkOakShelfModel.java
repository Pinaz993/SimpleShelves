package net.pinaz993.simpleshelves.client.woodshelves;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.pinaz993.simpleshelves.client.UnbakedShelfModel;

@Environment(EnvType.CLIENT)
public class DarkOakShelfModel extends UnbakedShelfModel {
    @Override
    public SpriteIdentifier getShelfSpriteID() {
        return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:block/dark_oak_planks"));
    }
}
