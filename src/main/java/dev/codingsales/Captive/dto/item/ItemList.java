package dev.codingsales.Captive.dto.item;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ItemList {
    private Integer pages;

    private Integer currentPage;

    private List<?> items;
}
