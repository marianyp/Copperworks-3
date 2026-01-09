package dev.mariany.copperworks.screen.search;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class SearchEntry {
    public static final int MAX_TERM_BYTES = 256;
    private static final int UTF8_CONTINUATION_MASK = 0xC0;
    private static final int UTF8_CONTINUATION_VALUE = 0x80;

    public static final PacketCodec<ByteBuf, SearchEntry> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SearchEntry::getSlot,
            PacketCodecs.STRING.collect(PacketCodecs.toCollection(HashSet::new)), SearchEntry::getTerms,
            SearchEntry::new
    );

    private final int slot;
    private final Set<String> terms;

    public SearchEntry(@Nullable World world, StackWithSlot stackWithSlot) {
        this(stackWithSlot.slot(), world, stackWithSlot.stack());
    }

    public SearchEntry(int slot, @Nullable World world, ItemStack stack) {
        this(
                slot,
                stack.getTooltip(Item.TooltipContext.create(world), null, TooltipType.BASIC)
                     .stream()
                     .map(Text::getString)
                     .toList()
        );
    }

    public SearchEntry(int slot, Collection<String> terms) {
        this.slot = slot;
        this.terms = terms.stream()
                          .map(term -> Formatting.strip(term).trim().toLowerCase(Locale.ROOT))
                          .map(SearchEntry::limitByBytes)
                          .filter(term -> !term.isBlank())
                          .collect(Collectors.toSet());
    }

    private static String limitByBytes(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        if (bytes.length <= MAX_TERM_BYTES) {
            return input;
        }

        int end = MAX_TERM_BYTES;

        while (end > 0 && (bytes[end] & UTF8_CONTINUATION_MASK) == UTF8_CONTINUATION_VALUE) {
            end--;
        }

        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }

    public Set<String> getTerms() {
        return this.terms;
    }

    public int getSlot() {
        return this.slot;
    }

    @Override
    public String toString() {
        return "SearchEntry{" +
                "slot=" + this.slot +
                ", terms=" + this.terms +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof SearchEntry otherEntry) {
            return this.slot == otherEntry.slot && terms.equals(otherEntry.terms);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.slot, this.terms);
    }
}