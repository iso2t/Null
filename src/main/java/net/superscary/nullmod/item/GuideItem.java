package net.superscary.nullmod.item;

import guideme.GuidesCommon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.superscary.nullmod.api.item.base.BaseItem;
import net.superscary.nullmod.core.Null;
import org.jetbrains.annotations.NotNull;

public class GuideItem extends BaseItem {

    public GuideItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (level.isClientSide) {
            GuidesCommon.openGuide(player, Null.getResource("guide"));
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(usedHand));
    }

}
