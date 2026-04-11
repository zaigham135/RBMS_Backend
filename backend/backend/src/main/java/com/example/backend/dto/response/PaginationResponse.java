package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor  // ✅ required for Redis deserialization
@AllArgsConstructor
public class PaginationResponse<T> {

    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

//    public PaginationResponse(List<T> data, int page, int size,
//                              long totalElements, int totalPages) {
//        this.data = data;
//        this.page = page;
//        this.size = size;
//        this.totalElements = totalElements;
//        this.totalPages = totalPages;
//    }
}