package dev.trinsdar.bicclipboard.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class ClipboardEditBox extends EditBox {
    public ClipboardEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
       if (this.isVisible()) {
           int color = this.textColor;
           int j = this.cursorPos - this.displayPos;
           int k = this.highlightPos - this.displayPos;
           String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
           boolean flag = j >= 0 && j <= s.length();
           boolean renderCursor = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
           int l = this.getX();
           int i1 = this.getY();
           int j1 = l;
           if (k > s.length()) {
               k = s.length();
           }

           if (!s.isEmpty()) {
               String s1 = flag ? s.substring(0, j) : s;
               j1 = guiGraphics.drawString(this.font, this.formatter.apply(s1, this.displayPos), l, i1, color, false);
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
               guiGraphics.drawString(this.font, this.formatter.apply(s.substring(j), this.cursorPos), j1, i1, color, false);
           }

           if (this.hint != null && s.isEmpty() && !this.isFocused()) {
               guiGraphics.drawString(this.font, this.hint, j1, i1, color, false);
           }

           if (!endOfText && this.suggestion != null) {
               guiGraphics.drawString(this.font, this.suggestion, k1 - 1, i1, -8355712, false);
           }

           if (renderCursor) {
               if (endOfText) {
                   guiGraphics.fill(RenderType.guiOverlay(), k1, i1 - 1, k1 + 1, i1 + 1 + 9, -16777216);
               } else {
                   guiGraphics.drawString(this.font, "_", k1, i1, color, false);
               }
           }

           if (k != j) {
               int l1 = l + this.font.width(s.substring(0, k));
               this.renderHighlight(guiGraphics, k1, i1 - 1, l1 - 1, i1 + 1 + 9);

           }
       }
    }
}
