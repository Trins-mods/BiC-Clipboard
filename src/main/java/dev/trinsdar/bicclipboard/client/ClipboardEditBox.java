package dev.trinsdar.bicclipboard.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class ClipboardEditBox extends EditBox {
    public ClipboardEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
       if (this.isVisible()) {
           int color = this.textColor;
           int j = this.cursorPos - this.displayPos;
           int k = this.highlightPos - this.displayPos;
           String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
           boolean flag = j >= 0 && j <= s.length();
           boolean renderCursor = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
           int l = this.x;
           int i1 = this.y;
           int j1 = l;
           if (k > s.length()) {
               k = s.length();
           }

           if (!s.isEmpty()) {
               String s1 = flag ? s.substring(0, j) : s;
               j1 = this.font.draw(poseStack, this.formatter.apply(s1, this.displayPos), l, i1, color);
           }

           boolean endOfText = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
           int k1 = j1;
           if (!flag) {
               k1 = j > 0 ? l + this.width : l;
           } else if (endOfText) {
               k1 = j1 - 1;
               --j1;
           }

           if (!s.isEmpty() && flag && j < s.length()) {
               this.font.draw(poseStack, this.formatter.apply(s.substring(j), this.cursorPos), j1, i1, color);
           }

           if (renderCursor) {
               if (endOfText) {
                   GuiComponent.fill(poseStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -16777216);
               } else {
                   this.font.draw(poseStack, "_", k1, i1, color);
               }
           }

           if (k != j) {
               int l1 = l + this.font.width(s.substring(0, k));
               this.renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
           }
       }
    }
}
