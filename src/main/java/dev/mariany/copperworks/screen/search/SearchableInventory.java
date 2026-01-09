package dev.mariany.copperworks.screen.search;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SearchableInventory {
    void updateSearchEntries(List<SearchEntry> entries);
    void updateSearchEntries(List<SearchEntry> entries, boolean clearExisting, boolean updateSlots);
    void updateSearchQuery(@Nullable String query);
    void onSearchQueryValidation(@Nullable String query);
}
