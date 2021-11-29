package misterx.diamondgen;

import misterx.diamondgen.render.Renderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.ChunkRandom;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import java.util.*;

public class OreSim {
public ClientWorld world = MinecraftClient.getInstance().world;
    public PlayerEntity player = MinecraftClient.getInstance().player;
private final HashMap<Long, HashMap<Ore.Type, HashSet<Vec3d>>> chunkRenderers = new HashMap<>();
    
    List<Ore> oreConfig;
   int chunkRange;
  //  DynamicValue<String> seedInput;
  String version;
    String airCheck;
    String versionString;
    public Long currentSeed = null;
    private ChunkPos prevOffset = new ChunkPos(0, 0);

    public OreSim(long seed) {
        version = DiamondGen.ver;
        versionString = version;
        oreConfig = Ore.getConfig(versionString);
        airCheck = "Rescan";
        currentSeed = 3427891657823464L;
        chunkRange = DiamondGen.range;
        if(DiamondGen.active == true) {
        	hasSeedChanged();
        reload();
        } else {
        	return;
        }
    }
    
/*    public OreSim(String version) {
        //version = (MultiValue) this.config.create("Version", "1.17.1", "1.14", "1.15", "1.16", "1.17.0", "1.17.1").description("Minecraft version of the world");
        versionString = version;
        oreConfig = Ore.getConfig(versionString);
        airCheck = "Rescan";
    }
    
    public OreSim(boolean active) {
        airCheck = "Rescan";
        if(active == true) {
        	enable();
        } else {
        	disable();
        }
    }
    
    public OreSim(long seed) {
        airCheck = "Rescan";
        currentSeed = seed;
    }
    public OreSim(int range) {
        airCheck = "Rescan";
        chunkRange = range;
    }*/

     public void getStartingPos(int BlockX, int BlockZ) {
        
    }

    public void onWorldRender(MatrixStack ms) {
        if (DiamondGen.client.player == null) return;
        if (currentSeed != null) {
            int chunkX = DiamondGen.client.player.getChunkPos().x;
            int chunkZ = DiamondGen.client.player.getChunkPos().z;

            int rangeVal = chunkRange;
            for (int range = 0; range <= rangeVal; range++) {
                for (int x = -range + chunkX; x <= range + chunkX; x++) {
                    renderChunk(x, chunkZ + range - rangeVal, ms);
                }
                for (int x = (-range) + 1 + chunkX; x < range + chunkX; x++) {
                    renderChunk(x, chunkZ - range + rangeVal + 1, ms);
                }
            }
        }

    }

     
    public void onHudRender() {

    }

    private void renderChunk(int x, int z, MatrixStack ms) {
        long chunkKey = (long) x + ((long) z << 32);

        if (chunkRenderers.containsKey(chunkKey)) {
            for (Ore ore : oreConfig) {
                if (ore.enabled) {
                    if (!chunkRenderers.get(chunkKey).containsKey(ore.type)) continue;
                    BufferBuilder buffer = Renderer.renderPrepare(ore.color);
                    for (Vec3d pos : chunkRenderers.get(chunkKey).get(ore.type)) {
                        Renderer.renderOutlineIntern(pos, new Vec3d(1, 1, 1), ms, buffer);
                    }
                    buffer.end();
                    BufferRenderer.draw(buffer);
                    GL11.glDepthFunc(GL11.GL_LEQUAL);
                }
            }
        }
    }

     
    public void tick() {
        if (hasSeedChanged() || hasVersionChanged()) {
            loadVisibleChunks();
        } else if (airCheck == "Rescan") {
            if (DiamondGen.client.player == null || DiamondGen.client.world == null) return;
            long chunkX = DiamondGen.client.player.getChunkPos().x;
            long chunkZ = DiamondGen.client.player.getChunkPos().z;
            ClientWorld world = DiamondGen.client.world;
            int renderdistance = MinecraftClient.getInstance().options.viewDistance;

            //maybe another config option? But its already crowded
            int chunkCounter = 5;

            while (true) {
                for (long offsetX = prevOffset.x; offsetX <= renderdistance; offsetX++) {
                    for (long offsetZ = prevOffset.z; offsetZ <= renderdistance; offsetZ++) {
                        prevOffset = new ChunkPos((int) offsetX, (int) offsetZ);
                        if (chunkCounter <= 0) {
                            return;
                        }
                        long chunkKey = (chunkX + offsetX) + ((chunkZ + offsetZ) << 32);

                        if (chunkRenderers.containsKey(chunkKey)) {
                            chunkRenderers.get(chunkKey).values().forEach(oreSet ->
                                    oreSet.removeIf(ore ->
                                            !world.getBlockState(new BlockPos((int) ore.x, (int) ore.y, (int) ore.z)).isOpaque())
                            );
                        }
                        chunkCounter--;
                    }
                    prevOffset = new ChunkPos((int) offsetX, -renderdistance);
                }
                prevOffset = new ChunkPos(-renderdistance, -renderdistance);
            }
        }
    }

     
    public void enable() {
        hasSeedChanged();
        this.reload();
    }

     
    public void disable() {

    }

     
    public String getContext() {
        return null;
    }

    private boolean hasSeedChanged() {
        Long tempSeed;
        try {
            tempSeed = this.currentSeed;
        } catch (Exception e) {
            tempSeed = this.currentSeed;
        }
        if (tempSeed != 69420 && !tempSeed.equals(this.currentSeed)) {
            this.currentSeed = tempSeed;
            chunkRenderers.clear();
            return true;
        }
        return false;
    }

    private boolean hasVersionChanged() {
        if (!versionString.equals(version)) {
            versionString = version;
            this.oreConfig = Ore.getConfig(versionString);
            //update ores in gui. fix this not being called when module is off
            chunkRenderers.clear();
            return true;
        }
        return false;
    }

    private void loadVisibleChunks() {
        int renderdistance = MinecraftClient.getInstance().options.viewDistance;

        if (DiamondGen.client.player == null) return;
        int playerChunkX = DiamondGen.client.player.getChunkPos().x;
        int playerChunkZ = DiamondGen.client.player.getChunkPos().z;

        for (int i = playerChunkX - renderdistance; i < playerChunkX + renderdistance; i++) {
            for (int j = playerChunkZ - renderdistance; j < playerChunkZ + renderdistance; j++) {
                doMathOnChunk(i, j);
            }
        }
    }



    public void reload() {
        chunkRenderers.clear();
        loadVisibleChunks();
    }

    public void doMathOnChunk(int chunkX, int chunkZ) {
        if (currentSeed == null) {
            this.disable();
            return;
        }
        long chunkKey = (long) chunkX + ((long) chunkZ << 32);

        ClientWorld world = DiamondGen.client.world;

        if (chunkRenderers.containsKey(chunkKey) || world == null)
            return;

        if (world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) return;

        chunkX = chunkX << 4;
        chunkZ = chunkZ << 4;

        ChunkRandom random = new ChunkRandom();
        HashMap<Ore.Type, HashSet<Vec3d>> h = new HashMap<>();

        long populationSeed = random.setPopulationSeed(currentSeed, chunkX, chunkZ);

        Identifier id = world.getRegistryManager().get(Registry.BIOME_KEY)
                .getId(world.getBiomeAccess().getBiomeForNoiseGen(new ChunkPos(chunkX >> 4, chunkZ >> 4)));
        if (id == null) {
       //     Client.notifyUser("Something went wrong, you may have some mods that mess with world generation");
           
            return;
        }
        String biomeName = id.getPath();
        String dimensionName = ((DimensionTypeCaller)world.getDimension()).getInfiniburn().getPath();

        for (Ore ore : oreConfig) {

            if (!dimensionName.endsWith(ore.dimension)) continue;

            HashSet<Vec3d> ores = new HashSet<>();

            int index;
            if (ore.index.containsKey(biomeName)) {
                index = ore.index.get(biomeName);
            } else {
                index = ore.index.get("default");
            }
            if (index < 0)
                continue;

            random.setDecoratorSeed(populationSeed, index, ore.step);

            int repeat = ore.count.get(random);

            if (biomeName.equals("basalt_deltas") && (ore.type == Ore.Type.GOLD_NETHER || ore.type == Ore.Type.QUARTZ)) {
                repeat *= 2;
            }

            for (int i = 0; i < repeat; i++) {

                int x = random.nextInt(16) + chunkX;
                int z;
                int y;
                if (versionString.equals("1.14")) {
                    y = ore.depthAverage ? random.nextInt(ore.maxY) + random.nextInt(ore.maxY) - ore.maxY : random.nextInt(ore.maxY - ore.minY);
                    z = random.nextInt(16) + chunkZ;
                } else {
                    z = random.nextInt(16) + chunkZ;
                    y = ore.depthAverage ? random.nextInt(ore.maxY) + random.nextInt(ore.maxY) - ore.maxY : random.nextInt(ore.maxY - ore.minY);
                }
                y += ore.minY;

                switch (ore.generator) {
                    case DEFAULT -> ores.addAll(generateNormal(world, random, new BlockPos(x, y, z), ore.size));
                    case EMERALD -> {
                        if (airCheck == "Off" || world.getBlockState(new BlockPos(x, y, z)).isOpaque())
                            ores.add(new Vec3d(x, y, z));
                    }
                    case NO_SURFACE -> ores.addAll(generateHidden(world, random, new BlockPos(x, y, z), ore.size));
                    default -> DiamondGen.log(" has some unknown generator. Fix it!");
                }
            }
            if (!ores.isEmpty())
                h.put(ore.type, ores);
        }
        chunkRenderers.put(chunkKey, h);
    }

    // ====================================
    // Mojang code
    // ====================================

    private ArrayList<Vec3d> generateNormal(ClientWorld world, Random random, BlockPos blockPos, int veinSize) {
        float f = random.nextFloat() * 3.1415927F;
        float g = (float) veinSize / 8.0F;
        int i = MathHelper.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d = (double) blockPos.getX() + Math.sin(f) * (double) g;
        double e = (double) blockPos.getX() - Math.sin(f) * (double) g;
        double h = (double) blockPos.getZ() + Math.cos(f) * (double) g;
        double j = (double) blockPos.getZ() - Math.cos(f) * (double) g;
        double l = (blockPos.getY() + random.nextInt(3) - 2);
        double m = (blockPos.getY() + random.nextInt(3) - 2);
        int n = blockPos.getX() - MathHelper.ceil(g) - i;
        int o = blockPos.getY() - 2 - i;
        int p = blockPos.getZ() - MathHelper.ceil(g) - i;
        int q = 2 * (MathHelper.ceil(g) + i);
        int r = 2 * (2 + i);

        for (int s = n; s <= n + q; ++s) {
            for (int t = p; t <= p + q; ++t) {
                if (o <= world.getTopY(Heightmap.Type.MOTION_BLOCKING, s, t)) {
                    return this.generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r);
                }
            }
        }

        return new ArrayList<>();
    }

    private ArrayList<Vec3d> generateVeinPart(ClientWorld world, Random random, int veinSize, double startX, double endX, double startZ,
                                              double endZ, double startY, double endY, int x, int y, int z, int size, int i) {

        BitSet bitSet = new BitSet(size * i * size);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        double[] ds = new double[veinSize * 4];

        ArrayList<Vec3d> poses = new ArrayList<>();

        int n;
        double p;
        double q;
        double r;
        double s;
        for (n = 0; n < veinSize; ++n) {
            float f = (float) n / (float) veinSize;
            p = MathHelper.lerp(f, startX, endX);
            q = MathHelper.lerp(f, startY, endY);
            r = MathHelper.lerp(f, startZ, endZ);
            s = random.nextDouble() * (double) veinSize / 16.0D;
            double m = ((double) (MathHelper.sin(3.1415927F * f) + 1.0F) * s + 1.0D) / 2.0D;
            ds[n * 4] = p;
            ds[n * 4 + 1] = q;
            ds[n * 4 + 2] = r;
            ds[n * 4 + 3] = m;
        }

        for (n = 0; n < veinSize - 1; ++n) {
            if (!(ds[n * 4 + 3] <= 0.0D)) {
                for (int o = n + 1; o < veinSize; ++o) {
                    if (!(ds[o * 4 + 3] <= 0.0D)) {
                        p = ds[n * 4] - ds[o * 4];
                        q = ds[n * 4 + 1] - ds[o * 4 + 1];
                        r = ds[n * 4 + 2] - ds[o * 4 + 2];
                        s = ds[n * 4 + 3] - ds[o * 4 + 3];
                        if (s * s > p * p + q * q + r * r) {
                            if (s > 0.0D) {
                                ds[o * 4 + 3] = -1.0D;
                            } else {
                                ds[n * 4 + 3] = -1.0D;
                            }
                        }
                    }
                }
            }
        }

        for (n = 0; n < veinSize; ++n) {
            double u = ds[n * 4 + 3];
            if (!(u < 0.0D)) {
                double v = ds[n * 4];
                double w = ds[n * 4 + 1];
                double aa = ds[n * 4 + 2];
                int ab = Math.max(MathHelper.floor(v - u), x);
                int ac = Math.max(MathHelper.floor(w - u), y);
                int ad = Math.max(MathHelper.floor(aa - u), z);
                int ae = Math.max(MathHelper.floor(v + u), ab);
                int af = Math.max(MathHelper.floor(w + u), ac);
                int ag = Math.max(MathHelper.floor(aa + u), ad);

                for (int ah = ab; ah <= ae; ++ah) {
                    double ai = ((double) ah + 0.5D - v) / u;
                    if (ai * ai < 1.0D) {
                        for (int aj = ac; aj <= af; ++aj) {
                            double ak = ((double) aj + 0.5D - w) / u;
                            if (ai * ai + ak * ak < 1.0D) {
                                for (int al = ad; al <= ag; ++al) {
                                    double am = ((double) al + 0.5D - aa) / u;
                                    if (ai * ai + ak * ak + am * am < 1.0D) {
                                        int an = ah - x + (aj - y) * size + (al - z) * size * i;
                                        if (!bitSet.get(an)) {
                                            bitSet.set(an);
                                            mutable.set(ah, aj, al);
                                            if (aj > 0 && (airCheck == "Off" || world.getBlockState(mutable).isOpaque())) {
                                                poses.add(new Vec3d(ah, aj, al));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return poses;
    }

    private ArrayList<Vec3d> generateHidden(ClientWorld world, Random random, BlockPos blockPos, int size) {

        ArrayList<Vec3d> poses = new ArrayList<>();

        int i = random.nextInt(size + 1);

        for (int j = 0; j < i; ++j) {
            size = Math.min(j, 7);
            int x = this.randomCoord(random, size) + blockPos.getX();
            int y = this.randomCoord(random, size) + blockPos.getY();
            int z = this.randomCoord(random, size) + blockPos.getZ();
            if (airCheck == "Off" || world.getBlockState(new BlockPos(x, y, z)).isOpaque())
                poses.add(new Vec3d(x, y, z));
        }

        return poses;
    }

    private int randomCoord(Random random, int size) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float) size);
    }

    public interface DimensionTypeCaller {
        Identifier getInfiniburn();
    }
}
