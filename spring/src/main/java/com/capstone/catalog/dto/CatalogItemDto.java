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
public class CatalogItemDto {

    private String externalItemId;
    private String title;
    private String imageUrl;
    private Integer price;
    private String brandName;
    private String sellerName;
    private List<String> categoryPath;
    private String productUrl;
    private String provider;
}