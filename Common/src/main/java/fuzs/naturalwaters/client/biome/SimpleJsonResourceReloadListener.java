package fuzs.naturalwaters.client.biome;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Copied from Minecraft 26.1.
 */
public abstract class SimpleJsonResourceReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DynamicOps<JsonElement> ops;
    private final Codec<T> codec;
    private final FileToIdConverter lister;

    protected SimpleJsonResourceReloadListener(HolderLookup.Provider registries, Codec<T> codec, ResourceKey<? extends Registry<T>> registryKey) {
        this(registries.createSerializationContext(JsonOps.INSTANCE),
                codec,
                FileToIdConverter.json(Registries.elementsDirPath(registryKey)));
    }

    protected SimpleJsonResourceReloadListener(Codec<T> codec, FileToIdConverter lister) {
        this(JsonOps.INSTANCE, codec, lister);
    }

    private SimpleJsonResourceReloadListener(DynamicOps<JsonElement> ops, Codec<T> codec, FileToIdConverter lister) {
        this.ops = ops;
        this.codec = codec;
        this.lister = lister;
    }

    protected Map<ResourceLocation, T> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, T> result = new HashMap<>();
        scanDirectory(manager, this.lister, this.ops, this.codec, result);
        return result;
    }

    public static <T> void scanDirectory(ResourceManager manager, ResourceKey<? extends Registry<T>> registryKey, DynamicOps<JsonElement> ops, Codec<T> codec, Map<ResourceLocation, T> result) {
        scanDirectory(manager, FileToIdConverter.json(Registries.elementsDirPath(registryKey)), ops, codec, result);
    }

    public static <T> void scanDirectory(ResourceManager manager, FileToIdConverter lister, DynamicOps<JsonElement> ops, Codec<T> codec, Map<ResourceLocation, T> result) {
        for (Entry<ResourceLocation, Resource> entry : lister.listMatchingResources(manager).entrySet()) {
            ResourceLocation location = entry.getKey();
            ResourceLocation id = lister.fileToId(location);

            try (Reader reader = entry.getValue().openAsReader()) {
                codec.parse(ops, parse(reader)).ifSuccess(parsed -> {
                    if (result.putIfAbsent(id, (T) parsed) != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                    }
                }).ifError(error -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", id, location, error));
            } catch (IllegalArgumentException | IOException | JsonParseException var14) {
                LOGGER.error("Couldn't parse data file '{}' from '{}'", id, location, var14);
            }
        }
    }

    /**
     * Copied from Minecraft 26.1.
     */
    public static JsonElement parse(Reader reader) throws JsonIOException, JsonSyntaxException {
        try {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(false);
            JsonElement element = JsonParser.parseReader(jsonReader);
            if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonSyntaxException("Did not consume the entire document.");
            } else {
                return element;
            }
        } catch (NumberFormatException | MalformedJsonException var3) {
            throw new JsonSyntaxException(var3);
        } catch (IOException var4) {
            throw new JsonIOException(var4);
        }
    }
}
