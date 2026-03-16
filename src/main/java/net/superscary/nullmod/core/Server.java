package net.superscary.nullmod.core;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public class Server extends Base {

    public Server(ModContainer container, IEventBus bus) {
        super(container, bus);
    }

    @Override
    public Level getClientLevel() {
        return null;
    }

}
