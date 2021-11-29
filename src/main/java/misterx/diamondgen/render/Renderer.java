package misterx.diamondgen.render;

import com.mojang.blaze3d.systems.RenderSystem;
import misterx.diamondgen.DiamondGen;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Renderer {

    


    protected Vec3d toVec3d(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

public static void renderOutlineInternNoTranslate(Vec3d start, Vec3d dimensions, MatrixStack stack, BufferBuilder buffer) {
        Vec3d end = start.add(dimensions);
        Matrix4f matrix = stack.peek().getModel();
        float x1 = (float) start.x;
        float y1 = (float) start.y;
        float z1 = (float) start.z;
        float x2 = (float) end.x;
        float y2 = (float) end.y;
        float z2 = (float) end.z;

        buffer.vertex(matrix, x1, y1, z1).next();
        buffer.vertex(matrix, x1, y1, z2).next();
        buffer.vertex(matrix, x1, y1, z2).next();
        buffer.vertex(matrix, x2, y1, z2).next();
        buffer.vertex(matrix, x2, y1, z2).next();
        buffer.vertex(matrix, x2, y1, z1).next();
        buffer.vertex(matrix, x2, y1, z1).next();
        buffer.vertex(matrix, x1, y1, z1).next();

        buffer.vertex(matrix, x1, y2, z1).next();
        buffer.vertex(matrix, x1, y2, z2).next();
        buffer.vertex(matrix, x1, y2, z2).next();
        buffer.vertex(matrix, x2, y2, z2).next();
        buffer.vertex(matrix, x2, y2, z2).next();
        buffer.vertex(matrix, x2, y2, z1).next();
        buffer.vertex(matrix, x2, y2, z1).next();
        buffer.vertex(matrix, x1, y2, z1).next();

        buffer.vertex(matrix, x1, y1, z1).next();
        buffer.vertex(matrix, x1, y2, z1).next();

        buffer.vertex(matrix, x2, y1, z1).next();
        buffer.vertex(matrix, x2, y2, z1).next();

        buffer.vertex(matrix, x2, y1, z2).next();
        buffer.vertex(matrix, x2, y2, z2).next();

        buffer.vertex(matrix, x1, y1, z2).next();
        buffer.vertex(matrix, x1, y2, z2).next();
    }

    public static void renderOutlineIntern(Vec3d start, Vec3d dimensions, MatrixStack stack, BufferBuilder buffer) {
        Camera c = Atomic.client.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        start = start.subtract(camPos);
        Vec3d end = start.add(dimensions);
        Matrix4f matrix = stack.peek().getModel();
        float x1 = (float) start.x;
        float y1 = (float) start.y;
        float z1 = (float) start.z;
        float x2 = (float) end.x;
        float y2 = (float) end.y;
        float z2 = (float) end.z;

        buffer.vertex(matrix, x1, y1, z1).next();
        buffer.vertex(matrix, x1, y1, z2).next();
        buffer.vertex(matrix, x1, y1, z2).next();
        buffer.vertex(matrix, x2, y1, z2).next();
        buffer.vertex(matrix, x2, y1, z2).next();
        buffer.vertex(matrix, x2, y1, z1).next();
        buffer.vertex(matrix, x2, y1, z1).next();
        buffer.vertex(matrix, x1, y1, z1).next();

        buffer.vertex(matrix, x1, y2, z1).next();
        buffer.vertex(matrix, x1, y2, z2).next();
        buffer.vertex(matrix, x1, y2, z2).next();
        buffer.vertex(matrix, x2, y2, z2).next();
        buffer.vertex(matrix, x2, y2, z2).next();
        buffer.vertex(matrix, x2, y2, z1).next();
        buffer.vertex(matrix, x2, y2, z1).next();
        buffer.vertex(matrix, x1, y2, z1).next();

        buffer.vertex(matrix, x1, y1, z1).next();
        buffer.vertex(matrix, x1, y2, z1).next();

        buffer.vertex(matrix, x2, y1, z1).next();
        buffer.vertex(matrix, x2, y2, z1).next();

        buffer.vertex(matrix, x2, y1, z2).next();
        buffer.vertex(matrix, x2, y2, z2).next();

        buffer.vertex(matrix, x1, y1, z2).next();
        buffer.vertex(matrix, x1, y2, z2).next();
    }

    //you can call renderOutlineIntern multiple times to save performance
    public static void renderOutline(Vec3d start, Vec3d dimensions, Color color, MatrixStack stack) {
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        BufferBuilder buffer = renderPrepare(color);

        renderOutlineIntern(start, dimensions, stack, buffer);

        buffer.end();
        BufferRenderer.draw(buffer);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static void renderOutlineNoTransform(Vec3d start, Vec3d dimensions, Color color, MatrixStack stack) {
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        BufferBuilder buffer = renderPrepare(color);

        renderOutlineInternNoTranslate(start, dimensions, stack, buffer);

        buffer.end();
        BufferRenderer.draw(buffer);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static BufferBuilder renderPrepare(Color color) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.lineWidth(2f);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION);
        return buffer;
    }

    public static void renderFilled(Vec3d start, Vec3d dimensions, Color color, MatrixStack stack) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        Camera c = Atomic.client.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        start = start.subtract(camPos);
        Vec3d end = start.add(dimensions);
        Matrix4f matrix = stack.peek().getModel();
        float x1 = (float) start.x;
        float y1 = (float) start.y;
        float z1 = (float) start.z;
        float x2 = (float) end.x;
        float y2 = (float) end.y;
        float z2 = (float) end.z;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        buffer.begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();

        buffer.end();

        BufferRenderer.draw(buffer);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static void line(Vec3d start, Vec3d end, Color color, MatrixStack matrices) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        Camera c = Atomic.client.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        start = start.subtract(camPos);
        end = end.subtract(camPos);
        Matrix4f matrix = matrices.peek().getModel();
        float x1 = (float) start.x;
        float y1 = (float) start.y;
        float z1 = (float) start.z;
        float x2 = (float) end.x;
        float y2 = (float) end.y;
        float z2 = (float) end.z;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();

        buffer.end();

        BufferRenderer.draw(buffer);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static void gradientLineScreen(Color start, Color end, double x, double y, double x1, double y1) {
        float g = start.getRed() / 255f;
        float h = start.getGreen() / 255f;
        float k = start.getBlue() / 255f;
        float f = start.getAlpha() / 255f;
        float g1 = end.getRed() / 255f;
        float h1 = end.getGreen() / 255f;
        float k1 = end.getBlue() / 255f;
        float f1 = end.getAlpha() / 255f;
        Matrix4f m = new MatrixStack().peek().getModel();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(m, (float) x, (float) y, 0f).color(g, h, k, f).next();
        bufferBuilder.vertex(m, (float) x1, (float) y1, 0f).color(g1, h1, k1, f1).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static Vec3d getCrosshairVector() {

        Camera camera = Atomic.client.gameRenderer.getCamera();

        ClientPlayerEntity player = Atomic.client.player;

        float f = 0.017453292F;
        float pi = (float) Math.PI;

        assert player != null;
        float f1 = MathHelper.cos(-player.getYaw() * f - pi);
        float f2 = MathHelper.sin(-player.getYaw() * f - pi);
        float f3 = -MathHelper.cos(-player.getPitch() * f);
        float f4 = MathHelper.sin(-player.getPitch() * f);

        return new Vec3d(f2 * f3, f4, f1 * f3).add(camera.getPos());
    }

    public static void fill(MatrixStack matrices, Color c, double x1, double y1, double x2, double y2) {
        int color = c.getRGB();
        double j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }
        Matrix4f matrix = matrices.peek().getModel();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void fillGradientH(MatrixStack matrices, Color c2, Color c1, double x1, double y1, double x2, double y2) {
        float r1 = c1.getRed() / 255f;
        float g1 = c1.getGreen() / 255f;
        float b1 = c1.getBlue() / 255f;
        float a1 = c1.getAlpha() / 255f;
        float r2 = c2.getRed() / 255f;
        float g2 = c2.getGreen() / 255f;
        float b2 = c2.getBlue() / 255f;
        float a2 = c2.getAlpha() / 255f;

        double j;

        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }
        Matrix4f matrix = matrices.peek().getModel();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(r1, g1, b1, a1).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(r2, g2, b2, a2).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(r2, g2, b2, a2).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(r1, g1, b1, a1).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

    }

    public static void fill(Color c, double x1, double y1, double x2, double y2) {
        fill(new MatrixStack(), c, x1, y1, x2, y2);
    }

    public static void lineScreenD(Color c, double x, double y, double x1, double y1) {
        float g = c.getRed() / 255f;
        float h = c.getGreen() / 255f;
        float k = c.getBlue() / 255f;
        float f = c.getAlpha() / 255f;
        Matrix4f m = new MatrixStack().peek().getModel();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(m, (float) x, (float) y, 0f).color(g, h, k, f).next();
        bufferBuilder.vertex(m, (float) x1, (float) y1, 0f).color(g, h, k, f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    public static int lerp(int o, int i, double p) {
        return (int) Math.floor(i + (o - i) * MathHelper.clamp(p, 0, 1));
    }

    public static double lerp(double i, double o, double p) {
        return (i + (o - i) * MathHelper.clamp(p, 0, 1));
    }

    public static Color lerp(Color a, Color b, double c) {
        return new Color(lerp(a.getRed(), b.getRed(), c),
                lerp(a.getGreen(), b.getGreen(), c),
                lerp(a.getBlue(), b.getBlue(), c),
                lerp(a.getAlpha(), b.getAlpha(), c));
    }

    /**
     * @param original       the original color
     * @param redOverwrite   the new red (or -1 for original)
     * @param greenOverwrite the new green (or -1 for original)
     * @param blueOverwrite  the new blue (or -1 for original)
     * @param alphaOverwrite the new alpha (or -1 for original)
     * @return the modified color
     */
    public static Color modify(Color original, int redOverwrite, int greenOverwrite, int blueOverwrite, int alphaOverwrite) {
        return new Color(redOverwrite == -1 ? original.getRed() : redOverwrite, greenOverwrite == -1 ? original.getGreen() : greenOverwrite, blueOverwrite == -1 ? original.getBlue() : blueOverwrite, alphaOverwrite == -1 ? original.getAlpha() : alphaOverwrite);
    }

}
