package net.superscary.nullmod.core;

import guideme.Guide;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.superscary.nullmod.client.screen.QuarryScreen;
import net.superscary.nullmod.registries.NMenus;

public class Client extends Base {

    @Getter
    private final Guide guide;

    public Client(ModContainer container, IEventBus bus) {
        super(container, bus);

        bus.addListener(this::registerScreens);

        guide = createGuide();
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(NMenus.QUARRY_MENU.get(), QuarryScreen::new);
    }

    private Guide createGuide() {
        return Guide.builder(Null.getResource("guide"))
                .defaultNamespace("nullmod")
                .build();
    }

    @Override
    public void openGuide(PageAnchor anchor) {
        GuidesCommon.openGuide(Minecraft.getInstance().player, guide.getId(), anchor);
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

}
