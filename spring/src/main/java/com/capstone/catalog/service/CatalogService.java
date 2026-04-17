package com.capstone.catalog.service;

import com.capstone.catalog.dto.CatalogSearchResponse;
import com.capstone.catalog.provider.CatalogProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogProvider catalogProvider;

    public CatalogSearchResponse search(String query, Integer display, Integer start, String sort) {
        int safeDisplay = display == null ? 10 : Math.min(display, 50);
        int safeStart = start == null ? 1 : start;
        String safeSort = (sort == null || sort.isBlank()) ? "sim" : sort;
        return catalogProvider.search(query, safeDisplay, safeStart, safeSort);
    }
}