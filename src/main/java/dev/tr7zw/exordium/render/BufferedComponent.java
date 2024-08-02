package dev.tr7zw.exordium.render;

import java.util.function.Supplier;

import org.joml.Vector3f;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import dev.tr7zw.exordium.ExordiumModBase;
import dev.tr7zw.exordium.util.BlendStateHolder;
import dev.tr7zw.exordium.util.ScreenTracker;
import dev.tr7zw.exordium.versionless.config.Config;
import lombok.Getter;
import net.minecraft.client.Minecraft;

//TODO: replace all protected to private once LegacyBuffer gets removed

public class BufferedComponent {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    @Getter
    protected static Model model = null;
    protected final Supplier<Config.ComponentSettings> settings;
    protected final RenderTarget guiTarget = new TextureTarget(100, 100, true, false);
    protected final ScreenTracker screenTracker = new ScreenTracker(guiTarget);
    protected final BlendStateHolder blendStateHolder = new BlendStateHolder();
    protected boolean forceBlending = false;

    public BufferedComponent(Supplier<Config.ComponentSettings> settings) {
        this(false, settings);
    }

    public BufferedComponent(boolean forceBlending, Supplier<Config.ComponentSettings> settings) {
        this.forceBlending = forceBlending;
        this.settings = settings;
    }

    protected static void refreshModel(int screenWidth, int screenHeight) {
        if (model != null) {
            model.close();
        }

        Vector3f[] modelData = new Vector3f[] { new Vector3f(0.0f, screenHeight, -90.0f),
                new Vector3f(screenWidth, screenHeight, -90.0F), new Vector3f(screenWidth, 0.0F, -90.0F),
                new Vector3f(0.0F, 0.0F, -90.0F), };
        Model.Vector2f[] uvData = new Model.Vector2f[] { new Model.Vector2f(0.0f, 0.0f), new Model.Vector2f(1.0f, 0.0f),
                new Model.Vector2f(1.0f, 1.0f), new Model.Vector2f(0.0f, 1.0f), };
        model = new Model(modelData, uvData);
    }

    public void captureComponent() {
        // Check for Screen size/scaling changes
        if (screenTracker.hasChanged()) {
            screenTracker.updateState();
            refreshModel(MINECRAFT.getWindow().getGuiScaledWidth(), MINECRAFT.getWindow().getGuiScaledHeight());
        }
        if (model == null) {
            refreshModel(MINECRAFT.getWindow().getGuiScaledWidth(), MINECRAFT.getWindow().getGuiScaledHeight());
        }

        guiTarget.setClearColor(0, 0, 0, 0);
        guiTarget.clear(false);
        guiTarget.bindWrite(false);
        ExordiumModBase.instance.setTemporaryScreenOverwrite(guiTarget);

        ExordiumModBase.correctBlendMode();
        if (forceBlending || settings.get().isForceBlend()) {
            ExordiumModBase.setForceBlend(true);
        }
        guiTarget.bindWrite(false);
    }

    public void renderBuffer() {
        ExordiumModBase.instance.getDelayedRenderCallManager().addBufferedComponent(this);
        // set the blendstate to what it would be if the normal render logic had run
        blendStateHolder.apply();
    }

    public void finishCapture() {
        ExordiumModBase.instance.setTemporaryScreenOverwrite(null);
        guiTarget.unbindWrite();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        if (forceBlending || settings.get().isForceBlend()) {
            ExordiumModBase.setForceBlend(false);
        }
        renderBuffer();
    }

    public int getTextureId() {
        return guiTarget.getColorTextureId();
    }

    public boolean needsBlendstateSample() {
        return !blendStateHolder.isBlendStateFetched();
    }

    public void captureBlendstateSample() {
        if (needsBlendstateSample()) {
            blendStateHolder.fetch();
        }
    }

    public boolean screenChanged() {
        return screenTracker.hasChanged();
    }

}
