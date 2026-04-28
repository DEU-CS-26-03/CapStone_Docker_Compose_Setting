package com.capstone.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogSearchResponse {

    private String provider;
    private String query;
    private Integer total;
    private Integer start;
    private Integer display;
    private List<CatalogItemDto> items;
}