package io.github.mortuusars.chalk.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record PlayerSleepInfo(List<BlockPos> sleepPositions) {
    public static final Codec<PlayerSleepInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(BlockPos.CODEC).optionalFieldOf("sleepPositions", Collections.emptyList()).forGetter(PlayerSleepInfo::sleepPositions))
        .apply(instance, PlayerSleepInfo::new));

    public static PlayerSleepInfo deserialize(String serialized) {
        JsonObject json = GsonHelper.parse(serialized);
        return CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(s ->
                        Chalk.LOGGER.error("Failed to deserialize PlayerSleepInfo: " + s + "\nInput: <" + serialized + ">"))
                .orElse(Pair.of(new PlayerSleepInfo(Collections.emptyList()), null)).getFirst();
    }

    public String serialize() {
        Optional<JsonElement> encodedElement = CODEC.encodeStart(JsonOps.INSTANCE, this)
                .resultOrPartial(s -> Chalk.LOGGER.error("Failed to serialize PlayerSleepInfo: " + s + "\nInput: <" + this.toString() + ">"));
        if (encodedElement.isEmpty())
            return "";

        return encodedElement.get().toString();
    }
}
