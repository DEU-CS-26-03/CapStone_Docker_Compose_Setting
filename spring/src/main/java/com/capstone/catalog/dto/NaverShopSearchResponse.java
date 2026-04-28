package com.capstone.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverShopSearchResponse {
    private int total;
    private int start;
    private int display;
    private List<NaverShopItem> items;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverShopItem {
        private String title;
        private String link;
        private String image;
        private String lprice;
        private String brand;
        private String mallName;
        private String productId;
        private String category1;
        private String category2;
        private String category3;
        private String category4;
    }
}