package com.capstone.catalog.controller;

import com.capstone.catalog.dto.CatalogSearchResponse;
import com.capstone.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/search")
    public CatalogSearchResponse search(
            @RequestParam String q,
            @RequestParam(required = false) Integer display,
            @RequestParam(required = false) Integer start,
            @RequestParam(required = false) String sort
    ) {
        return catalogService.search(q, display, start, sort);
    }
}