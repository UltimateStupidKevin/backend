package de.technikerarbeit.backend.mastergames.dto;

import java.util.List;

public class MasterGamePageResponse {

    public List<MasterGameListItemView> items;
    public int page;
    public int size;
    public long totalElements;
    public int totalPages;

    public MasterGamePageResponse(
            List<MasterGameListItemView> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
