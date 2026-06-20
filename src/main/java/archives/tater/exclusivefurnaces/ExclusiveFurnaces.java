package archives.tater.exclusivefurnaces;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

@Mod(ExclusiveFurnaces.MODID)
public class ExclusiveFurnaces {
    public static final String MODID = "exclusivefurnaces";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExclusiveFurnaces(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.STARTUP, Config.SPEC);
    }
}
