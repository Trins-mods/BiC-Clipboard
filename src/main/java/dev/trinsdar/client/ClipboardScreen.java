package dev.trinsdar.client;

import dev.trinsdar.BCClipboard;
import dev.trinsdar.CheckboxState;
import dev.trinsdar.clipboard.ClipboardContent;
import dev.trinsdar.clipboard.ClipboardContent.Page;
import dev.trinsdar.clipboard.ClipboardSyncPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClipboardScreen extends Screen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(BCClipboard.ID, "textures/gui/clipboard.png");
    private final ItemStack stack;
    private ClipboardContent data;
    private final CheckboxButton[] checkboxes = new CheckboxButton[ClipboardContent.MAX_LINES];
    private final ClipboardEditBox[] lines = new ClipboardEditBox[ClipboardContent.MAX_LINES];
    private ClipboardEditBox titleBox;
    private PageButton forwardButton;
    private PageButton backButton;

    public ClipboardScreen(ItemStack stack) {
        super(stack.getHoverName());
        this.stack = stack;
        this.data = ClipboardContent.fromStack(stack);
    }

    @Override
    public void onClose() {
        super.onClose();
        BCClipboard.INSTANCE.sendToServer(new ClipboardSyncPacket(data));
    }

    @Override
    protected void init() {
        int x = (width - 192) / 2;
        titleBox = addRenderableWidget(new ClipboardEditBox(getMinecraft().font, x + 57, 14, 72, 8, Component.empty()));
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
            lines[i] = addRenderableWidget(new ClipboardEditBox(getMinecraft().font, x + 45, 15 * i + 28, 109, 8, Component.empty()));
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
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, $ -> onClose()).bounds(width / 2 - 100, 196, 200, 20).build());
        updateContents();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(BACKGROUND, (width - 192) / 2, 2, 0, 0, 192, 192);
        super.render(graphics, mouseX, mouseY, partialTicks);
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
            super(new Builder(Component.empty(), onPress).bounds(x, y, 14, 14));
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
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (getSprite() != null) {
                guiGraphics.blit(getSprite(), getX(), getY(), 0, 0, 14, 14, 14, 14);
            }
        }

        @Nullable
        protected ResourceLocation getSprite() {
            return switch (state) {
                case EMPTY -> null;
                case CHECK -> ClipboardReadOnlyRenderer.CHECK_TEXTURE;
                case X -> ClipboardReadOnlyRenderer.X_TEXTURE;
            };
        }
    }
}
