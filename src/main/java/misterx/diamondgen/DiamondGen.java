package misterx.diamondgen;


import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
public class DiamondGen implements ModInitializer {
    public static int range = 100;
    public static boolean active = false;
    public static String ver = "1.17.0";
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static boolean isOpaque() {
        return opaque;
    }

    public static void setOpaque(boolean opaque) {
        DiamondGen.opaque = opaque;
    }

    private static boolean opaque = false;

    public static OreSim gen = new OreSim(0);
    @Override
    public void onInitialize() {
        clear(0);
        
    }

    public static void clear(long seed) {
        gen = new OreSim(seed);
        if (client.getInstance().player == null)
            return;
        Utils.reload();
    }
    public static void log(String log){
    	
    }
}
