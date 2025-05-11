package dev.codingsales.Captive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PageRequestDTO {
    private Integer pageNumber;

    private Integer pageLimit;
}
