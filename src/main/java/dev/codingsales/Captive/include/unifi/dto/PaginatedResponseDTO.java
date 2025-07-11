package dev.codingsales.Captive.include.unifi.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResponseDTO<T> {
    private int offset;
    private int limit;
    private int count;
    private int totalCount;
    private List<T> data;
}
