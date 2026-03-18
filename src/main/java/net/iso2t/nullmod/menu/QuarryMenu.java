package net.iso2t.nullmod.menu;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.iso2t.nullmod.block.entity.QuarryFrameBlockEntity;
import net.iso2t.nullmod.registries.NMenus;
import net.iso2t.nullmod.registries.NBlocks;

public class QuarryMenu extends AbstractContainerMenu {

    @Getter
    private final Level level;

    @Getter
    private final BlockPos pos;
    private final IItemHandler quarrySlots;

    private int syncedEnergy;
    private int syncedMaxEnergy;

    private int syncedMineX;
    private int syncedMineY;
    private int syncedMineZ;

    private int syncedFormed;
    private int syncedRedstoneDisabled;
    private int syncedActive;

    @Getter
    private final IEnergyStorage energyView = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return syncedEnergy;
        }

        @Override
        public int getMaxEnergyStored() {
            return syncedMaxEnergy;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public QuarryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level(), buf.readBlockPos());
    }

    public QuarryMenu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        super(NMenus.QUARRY_MENU.get(), containerId);
        this.level = level;
        this.pos = pos;

        QuarryFrameBlockEntity be = level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity frame ? frame : null;
        this.quarrySlots = be == null ? null : be.getQuarrySlots();

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                QuarryFrameBlockEntity frame = getFrameBlockEntity();
                return frame == null ? 0 : frame.getEnergyStorage().getEnergyStored();
            }

            @Override
            public void set(int value) {
                syncedEnergy = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                QuarryFrameBlockEntity frame = getFrameBlockEntity();
                return frame == null ? 0 : frame.getMineX();
            }

            @Override
            public void set(int value) {
                syncedMineX = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                QuarryFrameBlockEntity frame = getFrameBlockEntity();
                return frame == null ? 0 : frame.getMineY();
            }

            @Override
            public void set(int value) {
                syncedMineY = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                QuarryFrameBlockEntity frame = getFrameBlockEntity();
                return frame == null ? 0 : frame.getMineZ();
            }

            @Override
            public void set(int value) {
                syncedMineZ = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                QuarryFrameBlockEntity frame = getFrameBlockEntity();
                return frame != null && frame.isFormed() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedFormed = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return level.hasNeighborSignal(pos) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedRedstoneDisabled = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                QuarryFrameBlockEntity frame = getFrameBlockEntity();
                return frame != null && frame.isActive() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedActive = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                QuarryFrameBlockEntity frame = getFrameBlockEntity();
                return frame == null ? 0 : frame.getEnergyStorage().getMaxEnergyStored();
            }

            @Override
            public void set(int value) {
                syncedMaxEnergy = value;
            }
        });

        if (this.quarrySlots != null) {
            addSlot(new BiomeMarkerSlot(this.quarrySlots, 0, 8, 15));
            addSlot(new EnchantBookSlot(this.quarrySlots, 1, 8, 33));
            addSlot(new EnchantBookSlot(this.quarrySlots, 2, 8, 51));
        } else {
            addSlot(new DisabledSlot(8, 15));
            addSlot(new DisabledSlot(8, 33));
            addSlot(new DisabledSlot(8, 51));
        }

        int startX = 8;
        int startY = 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        int hotbarY = startY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, startX + col * 18, hotbarY));
        }
    }

    public QuarryFrameBlockEntity getFrameBlockEntity() {
        return level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity frame ? frame : null;
    }

    public int getMineX() {
        return syncedMineX;
    }

    public int getMineY() {
        return syncedMineY;
    }

    public int getMineZ() {
        return syncedMineZ;
    }

    public boolean isFormedSynced() {
        return syncedFormed != 0;
    }

    public boolean isRedstoneDisabledSynced() {
        return syncedRedstoneDisabled != 0;
    }

    public boolean isActiveSynced() {
        return syncedActive != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            int containerSlots = 3;

            if (index < containerSlots) {
                if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, pos), player, NBlocks.QUARRY_FRAME.getBlock());
    }

    private static class DisabledSlot extends Slot {
        private static final SimpleContainer DUMMY = new SimpleContainer(1);

        public DisabledSlot(int x, int y) {
            super(DUMMY, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean hasItem() {
            return false;
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
        }
    }

    private static class EnchantBookSlot extends SlotItemHandler {
        public EnchantBookSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!stack.is(Items.ENCHANTED_BOOK)) return false;
            ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
            ItemEnchantments enchants = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (var entry : enchants.entrySet()) {
                Enchantment enchantment = entry.getKey().value();
                if (!enchantment.isPrimaryItem(tool)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class BiomeMarkerSlot extends SlotItemHandler {
        public BiomeMarkerSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return key != null && key.getNamespace().equals("nullmod") && key.getPath().equals("biome_marker");
        }
    }
}
