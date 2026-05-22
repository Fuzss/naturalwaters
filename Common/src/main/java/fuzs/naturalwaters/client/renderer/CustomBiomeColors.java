package fuzs.naturalwaters.client.renderer;

import fuzs.naturalwaters.NaturalWaters;
import fuzs.naturalwaters.client.biome.ClientBiomeManager;
import fuzs.naturalwaters.config.ClientConfig;
import fuzs.puzzleslib.api.util.v1.ARGB;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;

public class CustomBiomeColors {
    /**
     * @see BiomeColors#WATER_COLOR_RESOLVER
     */
    public static final ColorResolver WATER_COLOR_RESOLVER = (Biome biome, double x, double y) -> {
        return ClientBiomeManager.getBiomeClientInfo(biome).waterSurfaceColor().orElseGet(biome::getWaterColor);
    };
    /**
     * @see BiomeColors
     */
    public static final ColorResolver WATER_TRANSPARENCY_RESOLVER = (Biome biome, double x, double z) -> {
        return ClientBiomeManager.getBiomeClientInfo(biome).getWaterSurfaceTransparency();
    };

    /**
     * @see BiomeColors#getAverageWaterColor(BlockAndTintGetter, BlockPos)
     */
    public static int getAverageWaterColor(BlockAndTintGetter level, BlockPos blockPos) {
        if (NaturalWaters.CONFIG.get(ClientConfig.class).waterSurfaceColor) {
            return level.getBlockTint(blockPos, WATER_COLOR_RESOLVER);
        } else {
            return BiomeColors.getAverageWaterColor(level, blockPos);
        }
    }

    /**
     * @see BiomeColors
     */
    public static float getAverageWaterTransparency(BlockAndTintGetter level, BlockPos blockPos) {
        if (NaturalWaters.CONFIG.get(ClientConfig.class).waterSurfaceTransparency) {
            int tintColor = level.getBlockTint(blockPos, WATER_TRANSPARENCY_RESOLVER);
            // Don't allow textures to become fully opaque.
            // Bedrock Edition does this also internally despite values going up to 100%.
            return ARGB.from8BitChannel(ARGB.transparent(tintColor)) * 0.95F;
        } else {
            return 1.0F;
        }
    }
}
