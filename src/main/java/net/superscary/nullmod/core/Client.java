package net.superscary.nullmod.core;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.superscary.nullmod.client.screen.QuarryScreen;
import net.superscary.nullmod.registries.NMenus;

public class Client extends Base {

    public Client(ModContainer container, IEventBus bus) {
        super(container, bus);

        bus.addListener(this::registerScreens);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(NMenus.QUARRY_MENU.get(), QuarryScreen::new);
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

}
