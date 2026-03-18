package net.iso2t.nullmod.core;

import guideme.PageAnchor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;

public interface Null {

    String NAME = "Null";
    String MODID = "nullmod";

    static Logger getLogger() {
        return LoggerFactory.getLogger(NAME);
    }

    static Null getInstance() {
        return Base.getInstance();
    }

    static ResourceLocation getResource(String name) {
        return getCustomResource(MODID, name);
    }

    static ResourceLocation getMinecraftResource(String name) {
        return ResourceLocation.withDefaultNamespace(name);
    }

    static ResourceLocation getCustomResource(String id, String name) {
        return ResourceLocation.fromNamespaceAndPath(id, name);
    }

    static Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    static Path getModsDirectory() {
        return FMLPaths.MODSDIR.get();
    }

    Collection<ServerPlayer> getPlayers();

    Level getClientLevel();

    MinecraftServer getCurrentServer();

    default void openGuide(PageAnchor anchor) {
    }

}
