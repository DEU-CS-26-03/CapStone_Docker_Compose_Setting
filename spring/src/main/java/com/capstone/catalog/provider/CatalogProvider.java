package com.capstone.catalog.provider;

import com.capstone.catalog.dto.CatalogSearchResponse;

public interface CatalogProvider {
    CatalogSearchResponse search(String query, int display, int start, String sort);
}