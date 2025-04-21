package dev.trinsdar.bicclipboard.clipboard;

import dev.trinsdar.bicclipboard.BiCClipboardUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ClipboardContent(String title, int active, List<Page> pages) {
    public static final int MAX_PAGES = 50;
    public static final int MAX_LINES = 9;
    public static final ClipboardContent DEFAULT = new ClipboardContent("", 0, List.of(Page.DEFAULT));

    public ClipboardContent setTitle(String title) {
        return new ClipboardContent(title, active, pages);
    }

    public ClipboardContent setActive(int active) {
        return new ClipboardContent(title, active, pages);
    }

    public boolean canHaveNewPage() {
        return active < pages.size() - 1 || pages.size() - 1 < MAX_PAGES;
    }

    public ClipboardContent nextPage() {
        if (active >= pages.size() - 1 && canHaveNewPage()) {
            List<Page> list = new ArrayList<>(pages);
            list.add(Page.DEFAULT);
            return setActive(list.size() - 1).setPages(list);
        } else return active >= pages.size() - 1 ? this : setActive(active + 1);
    }

    public ClipboardContent prevPage() {
        return active == 0 ? this : setActive(active - 1);
    }

    public ClipboardContent setPages(List<Page> pages) {
        return new ClipboardContent(title, active, pages);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("title", title);
        tag.putInt("active", active);
        ListTag list = new ListTag();
        for (Page page : pages) {
            list.add(page.serialize());
        }
        tag.put("pages", list);
        return tag;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(title);
        buf.writeVarInt(active);
        buf.writeVarInt(pages.size());
        for (Page page : pages) {
            page.encode(buf);
        }
    }

    public static ClipboardContent fromStack(ItemStack stack) {
        CompoundTag clipboardTag = stack.getTagElement("clipboardContent");
        return clipboardTag != null ? ClipboardContent.deserialize(clipboardTag) : ClipboardContent.DEFAULT;
    }

    public static ClipboardContent deserialize(CompoundTag tag) {
        String title = tag.getString("title");
        int active = tag.getInt("active");
        ListTag list = tag.getList("pages", Tag.TAG_COMPOUND);
        List<Page> pages = new ArrayList<>();
        for (Tag t : list) {
            if (t instanceof CompoundTag compoundTag) {
                pages.add(Page.deserialize(compoundTag));
            }
        }
        return new ClipboardContent(title, active, pages);
    }

    public static ClipboardContent decode(FriendlyByteBuf buf) {
        String title = buf.readUtf();
        int active = buf.readVarInt();
        int pageCount = buf.readVarInt();
        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            pages.add(Page.decode(buf));
        }
        return new ClipboardContent(title, active, pages);
    }


    public record Page(List<CheckboxState> checkboxes, List<String> lines){
        public static final Page DEFAULT = new Page(new ArrayList<>(MAX_LINES), new ArrayList<>(MAX_LINES));
        public Page(List<CheckboxState> checkboxes, List<String> lines) {
            this.checkboxes = BiCClipboardUtils.extend(checkboxes, MAX_LINES, CheckboxState.EMPTY);
            this.lines = BiCClipboardUtils.extend(lines, MAX_LINES, "");
        }
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            ListTag checkboxesTag = new ListTag();
            for (CheckboxState checkbox : checkboxes) {
                checkboxesTag.add(IntTag.valueOf(checkbox.ordinal()));
            }
            tag.put("checkboxes", checkboxesTag);
            ListTag linesTag = new ListTag();
            for (String line : lines) {
                linesTag.add(StringTag.valueOf(line));
            }
            tag.put("lines", linesTag);
            return tag;
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(checkboxes.size());
            for (CheckboxState checkbox : checkboxes) {
                buf.writeVarInt(checkbox.ordinal());
            }
            buf.writeVarInt(lines.size());
            for (String line : lines) {
                buf.writeUtf(line);
            }
        }

        public static Page deserialize(CompoundTag tag) {
            ListTag checkboxesTag = tag.getList("checkboxes", Tag.TAG_INT);
            if (checkboxesTag.isEmpty()) checkboxesTag = tag.getList("checkboxes", Tag.TAG_STRING);
            List<CheckboxState> checkboxes = new ArrayList<>();
            for (Tag t : checkboxesTag) {
                if (t instanceof StringTag stringTag) {
                    checkboxes.add(CheckboxState.valueOf(stringTag.getAsString().toUpperCase()));
                } else if (t instanceof IntTag intTag) {
                    checkboxes.add(CheckboxState.values()[intTag.getAsInt()]);
                }
            }
            ListTag linesTag = tag.getList("lines", Tag.TAG_STRING);
            List<String> lines = new ArrayList<>();
            for (Tag t : linesTag) {
                if (t instanceof StringTag stringTag) {
                    lines.add(stringTag.getAsString());
                }
            }
            return new Page(checkboxes, lines);
        }

        public static Page decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<CheckboxState> checkboxes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                checkboxes.add(CheckboxState.values()[buf.readVarInt()]);
            }
            size = buf.readVarInt();
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                lines.add(buf.readUtf());
            }
            return new Page(checkboxes, lines);
        }

        public Page setCheckboxes(List<CheckboxState> checkboxes) {
            return new Page(checkboxes, lines);
        }

        public Page setLines(List<String> lines) {
            return new Page(checkboxes, lines);
        }

    }
}
