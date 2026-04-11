package com.example.backend.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletionTrendPoint {
    private String date;
    private int count;
}
