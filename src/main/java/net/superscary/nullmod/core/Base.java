package net.superscary.nullmod.core;

import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.superscary.nullmod.block.entity.QuarryFrameBlockEntity;
import net.superscary.nullmod.block.entity.SatelliteBlockEntity;
import net.superscary.nullmod.registries.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public abstract class Base implements Null {

    @Getter
    static Null instance;

    public Base(ModContainer container, IEventBus bus) {
        if (instance != null) throw new IllegalStateException("Already initialized!");
        instance = this;

        bus.addListener(NTab::initializeExternal);
        bus.addListener(this::registerCapabilities);
        register(bus);

        bus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB)
                NTab.initialize(BuiltInRegistries.CREATIVE_MODE_TAB);
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                NBlockEntities.QUARRY_FRAME_ENTITY.get(),
                (QuarryFrameBlockEntity blockEntity, Direction context) -> blockEntity.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                NBlockEntities.QUARRY_FRAME_ENTITY.get(),
                (QuarryFrameBlockEntity blockEntity, Direction context) -> blockEntity.getItemHandler()
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                NBlockEntities.QUARRY_FRAME_ENTITY.get(),
                (QuarryFrameBlockEntity blockEntity, Direction context) -> blockEntity.getFluidTank()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                NBlockEntities.SATELLITE_ENTITY.get(),
                (SatelliteBlockEntity blockEntity, Direction context) -> {
                    var controller = blockEntity.getController();
                    return controller == null ? null : controller.getEnergyStorage();
                }
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                NBlockEntities.SATELLITE_ENTITY.get(),
                (SatelliteBlockEntity blockEntity, Direction context) -> {
                    var controller = blockEntity.getController();
                    return controller == null ? null : controller.getItemHandler();
                }
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                NBlockEntities.SATELLITE_ENTITY.get(),
                (SatelliteBlockEntity blockEntity, Direction context) -> {
                    var controller = blockEntity.getController();
                    return controller == null ? null : controller.getFluidTank();
                }
        );
    }

    private void register(@NotNull IEventBus bus) {
        NItems.REGISTRY.register(bus);
        NBlocks.REGISTRY.register(bus);
        NBlockEntities.REGISTRY.register(bus);
        NMenus.REGISTRY.register(bus);
    }

    @Override
    public Collection<ServerPlayer> getPlayers() {
        var server = getCurrentServer();
        return server == null ? Collections.emptyList() : server.getPlayerList().getPlayers();
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

}
