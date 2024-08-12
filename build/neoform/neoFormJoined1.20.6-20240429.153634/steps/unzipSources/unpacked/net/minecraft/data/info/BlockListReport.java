package net.minecraft.data.info;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockListReport implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public BlockListReport(PackOutput p_251533_, CompletableFuture<HolderLookup.Provider> p_330926_) {
        this.output = p_251533_;
        this.registries = p_330926_;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput p_236197_) {
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("blocks.json");
        return this.registries
            .thenCompose(
                p_332053_ -> {
                    JsonObject jsonobject = new JsonObject();
                    RegistryOps<JsonElement> registryops = p_332053_.createSerializationContext(JsonOps.INSTANCE);
                    p_332053_.lookupOrThrow(Registries.BLOCK)
                        .listElements()
                        .forEach(
                            p_330336_ -> {
                                JsonObject jsonobject1 = new JsonObject();
                                StateDefinition<Block, BlockState> statedefinition = p_330336_.value().getStateDefinition();
                                if (!statedefinition.getProperties().isEmpty()) {
                                    JsonObject jsonobject2 = new JsonObject();

                                    for (Property<?> property : statedefinition.getProperties()) {
                                        JsonArray jsonarray = new JsonArray();

                                        for (Comparable<?> comparable : property.getPossibleValues()) {
                                            jsonarray.add(Util.getPropertyName(property, comparable));
                                        }

                                        jsonobject2.add(property.getName(), jsonarray);
                                    }

                                    jsonobject1.add("properties", jsonobject2);
                                }

                                JsonArray jsonarray1 = new JsonArray();

                                for (BlockState blockstate : statedefinition.getPossibleStates()) {
                                    JsonObject jsonobject3 = new JsonObject();
                                    JsonObject jsonobject4 = new JsonObject();

                                    for (Property<?> property1 : statedefinition.getProperties()) {
                                        jsonobject4.addProperty(property1.getName(), Util.getPropertyName(property1, blockstate.getValue(property1)));
                                    }

                                    if (jsonobject4.size() > 0) {
                                        jsonobject3.add("properties", jsonobject4);
                                    }

                                    jsonobject3.addProperty("id", Block.getId(blockstate));
                                    if (blockstate == p_330336_.value().defaultBlockState()) {
                                        jsonobject3.addProperty("default", true);
                                    }

                                    jsonarray1.add(jsonobject3);
                                }

                                jsonobject1.add("states", jsonarray1);
                                String s = p_330336_.getRegisteredName();
                                JsonElement jsonelement = BlockTypes.CODEC
                                    .codec()
                                    .encodeStart(registryops, p_330336_.value())
                                    .getOrThrow(
                                        p_331670_ -> new AssertionError("Failed to serialize block " + s + " (is type registered in BlockTypes?): " + p_331670_)
                                    );
                                jsonobject1.add("definition", jsonelement);
                                jsonobject.add(s, jsonobject1);
                            }
                        );
                    return DataProvider.saveStable(p_236197_, jsonobject, path);
                }
            );
    }

    @Override
    public final String getName() {
        return "Block List";
    }
}
