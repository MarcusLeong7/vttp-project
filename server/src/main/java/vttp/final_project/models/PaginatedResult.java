package vttp.final_project.models;

import java.util.List;

public class PaginatedResult<T> {

    private List<T> items;
    private int totalItems;
    private int totalPages;
    private int currentPage;

    public PaginatedResult(List<T> items, int totalItems, int totalPages, int currentPage) {
        this.items = items;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public List<T> getItems() {
        return items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
