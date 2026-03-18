package net.iso2t.nullmod.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.menu.QuarryMenu;

import java.util.function.Supplier;

public class NMenus {

    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, Null.MODID);

    public static final Supplier<MenuType<QuarryMenu>> QUARRY_MENU = REGISTRY.register("quarry", () -> IMenuTypeExtension.create(QuarryMenu::new));

}
