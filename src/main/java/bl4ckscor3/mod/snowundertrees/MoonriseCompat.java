package bl4ckscor3.mod.snowundertrees;

import java.util.function.Consumer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class MoonriseCompat {
	public static void chunkRunner(ServerLevel level, Consumer<LevelChunk> action) {
		//((ChunkSystemServerLevel) level).moonrise$getTickingChunks().forEach(cah -> action.accept(cah.chunk()));
	}
}
