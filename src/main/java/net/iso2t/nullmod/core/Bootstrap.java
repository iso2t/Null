package net.iso2t.nullmod.core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Null.MODID)
public class Bootstrap {

    public Bootstrap(ModContainer container, IEventBus bus) {
        switch (FMLEnvironment.dist) {
            case CLIENT -> new Client(container, bus);
            case DEDICATED_SERVER -> new Server(container, bus);
        }
    }

}
