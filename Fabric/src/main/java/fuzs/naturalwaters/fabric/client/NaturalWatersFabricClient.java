package fuzs.naturalwaters.fabric.client;

import fuzs.naturalwaters.common.NaturalWaters;
import fuzs.naturalwaters.common.client.NaturalWatersClient;
import fuzs.naturalwaters.common.client.color.block.WaterTintSource;
import fuzs.naturalwaters.common.client.packs.OpaqueWaterPackResources;
import fuzs.naturalwaters.common.client.renderer.CustomBiomeColors;
import fuzs.naturalwaters.common.config.ClientConfig;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluids;

public class NaturalWatersFabricClient implements ClientModInitializer {
    /**
     * @see net.minecraft.client.renderer.block.FluidStateModelSet#WATER_MODEL
     */
    private static final FluidModel.Unbaked TRANSPARENT_WATER_MODEL = new FluidModel.Unbaked(FluidStateModelSet.WATER_MODEL.stillMaterial(),
            FluidStateModelSet.WATER_MODEL.flowingMaterial(),
            FluidStateModelSet.WATER_MODEL.overlayMaterial(),
            new WaterTintSource());
    /**
     * @see net.minecraft.client.renderer.block.FluidStateModelSet#WATER_MODEL
     */
    private static final FluidModel.Unbaked OPAQUE_WATER_MODEL = new FluidModel.Unbaked(new Material(
            OpaqueWaterPackResources.WATER_STILL),
            new Material(OpaqueWaterPackResources.WATER_FLOW),
            FluidStateModelSet.WATER_MODEL.overlayMaterial(),
            new WaterTintSource());

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(NaturalWaters.MOD_ID, NaturalWatersClient::new);
        ColorResolverRegistry.register(CustomBiomeColors.WATER_COLOR_RESOLVER);
        ColorResolverRegistry.register(CustomBiomeColors.WATER_OPACITY_RESOLVER);
        if (NaturalWaters.CONFIG.get(ClientConfig.class).waterSurfaceOpacity) {
            FluidRenderingRegistry.register(Fluids.WATER, Fluids.FLOWING_WATER, OPAQUE_WATER_MODEL);
        } else if (NaturalWaters.CONFIG.get(ClientConfig.class).waterSurfaceColor) {
            FluidRenderingRegistry.register(Fluids.WATER, Fluids.FLOWING_WATER, TRANSPARENT_WATER_MODEL);
        }
    }
}
