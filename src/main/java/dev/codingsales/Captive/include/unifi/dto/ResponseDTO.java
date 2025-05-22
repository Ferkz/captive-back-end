package dev.codingsales.Captive.include.unifi.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class ResponseDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    private MetaDTO meta;
    private List<Object> data;

    public void setDataList(List<Map<String, Object>> maps) {
    }
}
