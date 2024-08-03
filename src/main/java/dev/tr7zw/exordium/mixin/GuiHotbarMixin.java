package dev.tr7zw.exordium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tr7zw.exordium.ExordiumModBase;
import dev.tr7zw.exordium.components.BufferInstance;
import dev.tr7zw.exordium.components.vanilla.HotbarComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;

@Mixin(Gui.class)
public class GuiHotbarMixin {

    @WrapOperation(method = "renderHotbarAndDecorations", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;F)V"), })
    private void renderHotbarWrapper(Gui gui, GuiGraphics guiGraphics, float f, final Operation<Void> operation) {
        BufferInstance<Void> buffer = ExordiumModBase.instance.getBufferManager()
                .getBufferInstance(HotbarComponent.getId(), Void.class);
        if (!buffer.renderBuffer(0, null)) {
            operation.call(gui, guiGraphics, f);
        }
        buffer.postRender(null);
    }

    // FIXME broken
//    @WrapOperation(method = "renderHotbarAndDecorations", at = {
//            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;)V"), })
//    private void renderHotbarWrapperSpectator(SpectatorGui gui, GuiGraphics guiGraphics,
//            final Operation<Void> operation) {
//        BufferInstance<Void> buffer = ExordiumModBase.instance.getBufferManager()
//                .getBufferInstance(HotbarComponent.getId(), Void.class);
//        if (!buffer.renderBuffer(0, null)) {
//            operation.call(gui, guiGraphics);
//        }
//        buffer.postRender(null);
//    }

}
