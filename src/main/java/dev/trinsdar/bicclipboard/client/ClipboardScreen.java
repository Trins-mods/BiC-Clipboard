package dev.trinsdar.bcclipboard.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.trinsdar.bcclipboard.BCClipboard;
import dev.trinsdar.bcclipboard.BCClipboardUtils;
import dev.trinsdar.bcclipboard.clipboard.CheckboxState;
import dev.trinsdar.bcclipboard.clipboard.ClipboardContent;
import dev.trinsdar.bcclipboard.clipboard.ClipboardContent.Page;
import dev.trinsdar.bcclipboard.clipboard.ClipboardSyncPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClipboardScreen extends Screen {

    private ClipboardContent data;
    private final CheckboxButton[] checkboxes = new CheckboxButton[ClipboardContent.MAX_LINES];
    private final ClipboardEditBox[] lines = new ClipboardEditBox[ClipboardContent.MAX_LINES];
    private ClipboardEditBox titleBox;
    private PageButton forwardButton;
    private PageButton backButton;

    public ClipboardScreen(ItemStack stack) {
        super(stack.getHoverName());
        this.data = ClipboardContent.fromStack(stack);
    }

    @Override
    public void onClose() {
        super.onClose();
        List<Page> pages = new ArrayList<>(data.pages());
        for (int i = pages.size() - 1; i > 0; i--) {
            if (i == data.active()) break;
            if (!pages.get(i).equals(Page.DEFAULT)) break;
            pages.remove(i);
        }
        data = data.setPages(pages);
        BCClipboard.INSTANCE.sendToServer(new ClipboardSyncPacket(data));
    }

    @Override
    public void tick() {
        titleBox.tick();
        for (EditBox box : lines) {
            box.tick();
        }
    }

    @Override
    protected void init() {
        int x = (width - 192) / 2;
        titleBox = addRenderableWidget(new ClipboardEditBox(getMinecraft().font, x + 57, 14, 72, 8, new TextComponent("")));
        titleBox.setTextColor(0);
        titleBox.setBordered(false);
        //titleBox.setTextShadow(false);
        titleBox.setResponder(e -> data = data.setTitle(e));
        for (int i = 0; i < ClipboardContent.MAX_LINES; i++) {
            final int j = i; // I love Java
            checkboxes[i] = addRenderableWidget(new CheckboxButton(x + 30, 15 * i + 26, e -> {
                List<Page> pages = new ArrayList<>(data.pages());
                ClipboardContent.Page page = pages.get(data.active());
                List<CheckboxState> checkboxes = new ArrayList<>(page.checkboxes());
                checkboxes.set(j, ((CheckboxButton) e).getState());
                pages.set(data.active(), page.setCheckboxes(checkboxes));
                data = data.setPages(pages);
            }));
            lines[i] = addRenderableWidget(new ClipboardEditBox(getMinecraft().font, x + 45, 15 * i + 28, 109, 8, new TextComponent("")));
            lines[i].setTextColor(0);
            lines[i].setBordered(false);
            //lines[i].setTextShadow(false);
            lines[i].setResponder(e -> {
                List<ClipboardContent.Page> pages = new ArrayList<>(data.pages());
                ClipboardContent.Page page = pages.get(data.active());
                List<String> lines = new ArrayList<>(page.lines());
                lines.set(j, e);
                pages.set(data.active(), page.setLines(lines));
                data = data.setPages(pages);
            });
        }
        forwardButton = addRenderableWidget(new PageButton(x + 116, 159, true, $ -> {
            data = data.nextPage();
            updateContents();
        }, false));
        backButton = addRenderableWidget(new PageButton(x + 43, 159, false, $ -> {
            data = data.prevPage();
            updateContents();
        }, false));
        addRenderableWidget(new Button(width / 2 - 100, 196, 200, 20, CommonComponents.GUI_DONE, $ -> onClose()));
        updateContents();
    }


    public static void drawTexture(PoseStack stack, ResourceLocation loc, int left, int top, int x, int y, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, loc);
        blit(stack, left, top, x, y, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        drawTexture(stack, BCClipboardUtils.BACKGROUND_GUI, (width - 192) / 2, 2, 0, 0, 192, 192, 256, 256);
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0 && forwardButton.visible) {
            forwardButton.onPress();
            return true;
        }
        if (delta > 0 && backButton.visible) {
            backButton.onPress();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        if (listener != this.getFocused()){
            for (EditBox box : lines) {
                if (box != listener || listener == null){
                    box.setFocus(false);
                }
            }
            if (titleBox != listener || listener == null){
                titleBox.setFocus(false);
            }
        }
        super.setFocused(listener);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        return switch (keyCode) {
            case GLFW.GLFW_KEY_PAGE_UP -> {
                backButton.onPress();
                yield true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                forwardButton.onPress();
                yield true;
            }
            default -> false;
        };
    }

    private void updateContents() {
        backButton.visible = data.active() > 0;
        titleBox.setValue(data.title());
        ClipboardContent.Page page = data.pages().get(data.active());
        for (int i = 0; i < checkboxes.length; i++) {
            checkboxes[i].setState(page.checkboxes().get(i));
            lines[i].setValue(page.lines().get(i));
        }
    }

    private static class CheckboxButton extends Button {
        private CheckboxState state = CheckboxState.EMPTY;

        public CheckboxButton(int x, int y, OnPress onPress) {
            super(x, y, 14, 14, new TextComponent(""), onPress);
        }

        public CheckboxState getState() {
            return state;
        }

        public void setState(CheckboxState state) {
            this.state = state;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            state = switch (state) {
                case EMPTY -> CheckboxState.CHECK;
                case CHECK -> CheckboxState.X;
                case X -> CheckboxState.EMPTY;
            };
            super.onClick(mouseX, mouseY);
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (getSprite() != null){
                drawTexture(poseStack, getSprite(), x, y, 0, 0, 14, 14, 14, 14);
            }
        }

        @Nullable
        protected ResourceLocation getSprite() {
            return switch (state) {
                case EMPTY -> null;
                case CHECK -> BCClipboardUtils.CHECK_TEXTURE;
                case X -> BCClipboardUtils.X_TEXTURE;
            };
        }
    }
}
