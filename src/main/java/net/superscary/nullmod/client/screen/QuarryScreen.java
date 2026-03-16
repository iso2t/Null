package net.superscary.nullmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.superscary.nullmod.api.util.MouseUtil;
import net.superscary.nullmod.client.renderer.EnergyDisplayTooltipArea;
import net.superscary.nullmod.core.Null;
import net.superscary.nullmod.item.BiomeMarkerItem;
import net.superscary.nullmod.menu.QuarryMenu;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

public class QuarryScreen extends AbstractContainerScreen<QuarryMenu> {

    private static final ResourceLocation TEXTURE = Null.getResource("textures/gui/quarry.png");

    private final int energyLeft = 158;
    private final int energyWidth = 8;
    private final int energyTop = 9;
    private final int energyHeight = 64;

    private final EnergyDisplayTooltipArea energyDisplayTooltipArea;

    private static final int TERMINAL_LEFT = 32;
    private static final int TERMINAL_TOP = 14;
    private static final int TERMINAL_BOTTOM = 68;

    public QuarryScreen(QuarryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.energyDisplayTooltipArea = new EnergyDisplayTooltipArea(158, 73, menu.getEnergyView());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderTerminalText(graphics);
    }

    private void renderTerminalText(GuiGraphics graphics) {
        int rectX = leftPos + TERMINAL_LEFT;
        int rectY = topPos + TERMINAL_TOP;
        int rectH = TERMINAL_BOTTOM - TERMINAL_TOP;

        int color = 0x00FF00;

        var lines = getTerminalLines();
        int lineSpacing = 2;

        float scale = 0.75f;

        int maxW = 0;
        for (Component line : lines) {
            maxW = Math.max(maxW, font.width(line));
        }
        int totalH = (lines.size() * font.lineHeight) + ((lines.size() - 1) * lineSpacing);

        float scaledRectH = rectH / scale;

        float baseX = rectX / scale;
        float baseY = rectY / scale;

        float startY = baseY + Math.max(0.0f, (scaledRectH - totalH) / 2.0f);

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        float y = startY;
        for (Component line : lines) {
            int w = font.width(line);
            float x = baseX + Math.max(0.0f, 3);
            graphics.drawString(font, line, (int) x, (int) y, color, false);
            y += font.lineHeight + lineSpacing;
        }
        graphics.pose().popPose();
    }

    private List<Component> getTerminalLines() {
        var marker = menu.getSlot(0).getItem();
        var biomeId = BiomeMarkerItem.getBiomeId(marker);
        String biomeText = menu.getFrameBlockEntity().getCurrentBiome();

        BigInteger blocksMinedText = null;
        var be = menu.getFrameBlockEntity();
        if (be != null && be.getBlocksMined() != null) {
            blocksMinedText = be.getBlocksMined();
        }

        Component powerState = getPowerState();

        return List.of(
                Component.translatable("gui.nullmod.quarry.term.header"),
                Component.empty(),
                //Component.translatable("gui.nullmod.quarry.term.head", menu.getMineX(), menu.getMineY(), menu.getMineZ()),
                Component.translatable("gui.nullmod.quarry.term.biome", biomeText),
                Component.translatable("gui.nullmod.quarry.term.power", powerState),
                Component.translatable("gui.nullmod.quarry.term.blocks_mined", String.format("%,d", blocksMinedText))
        );
    }

    private @NotNull Component getPowerState() {
        Component powerState;
        if (menu.isRedstoneDisabledSynced()) {
            powerState = Component.translatable("gui.nullmod.quarry.power.redstone_off");
        } else if (!menu.isFormedSynced()) {
            powerState = Component.translatable("gui.nullmod.quarry.power.unformed");
        } else if (!menu.isActiveSynced()) {
            powerState = Component.translatable("gui.nullmod.quarry.power.idle");
        } else {
            powerState = Component.translatable("gui.nullmod.quarry.power.active");
        }
        return powerState;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderEnergyAreaTooltips(graphics, mouseX, mouseY, x, y);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        energyDisplayTooltipArea.render(graphics, leftPos + energyLeft, topPos + energyTop);
        renderTooltip(graphics, mouseX, mouseY);
    }

    protected boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }

    private void renderEnergyAreaTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (isMouseAboveArea(mouseX, mouseY, x, y, energyLeft, energyTop, energyWidth, energyHeight)) {
            guiGraphics.renderTooltip(this.font, getEnergyTooltips(), Optional.empty(), mouseX - x, mouseY - y);
        }
    }

    public List<Component> getEnergyTooltips() {
        IEnergyStorage power = menu.getEnergyView();
        DecimalFormat format = new DecimalFormat("#,###");
        return List.of(Component.literal(format.format(power.getEnergyStored()) + " / " + format.format(power.getMaxEnergyStored()) + " FE"));
    }
}
