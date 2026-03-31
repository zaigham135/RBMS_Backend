package com.example.backend.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class PaginationResponse<T> {

    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PaginationResponse(List<T> data, int page, int size,
                              long totalElements, int totalPages) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}