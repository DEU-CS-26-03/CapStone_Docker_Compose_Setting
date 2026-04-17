package com.capstone.catalog.provider;

import com.capstone.catalog.dto.CatalogItemDto;
import com.capstone.catalog.dto.CatalogSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NaverCatalogProvider implements CatalogProvider {

    private final RestClient restClient = RestClient.create();

    @Value("${external.naver.client-id}")
    private String clientId;

    @Value("${external.naver.client-secret}")
    private String clientSecret;

    @Override
    public CatalogSearchResponse search(String query, int display, int start, String sort) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://openapi.naver.com/v1/search/shop.json")
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("start", start)
                .queryParam("sort", sort)
                .toUriString();

        NaverShopSearchResponse response = restClient.get()
                .uri(url)
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(NaverShopSearchResponse.class);

        List<CatalogItemDto> items = response.getItems().stream()
                .map(item -> CatalogItemDto.builder()
                        .externalItemId(item.getProductId())
                        .title(item.getTitle().replaceAll("<[^>]*>", ""))
                        .imageUrl(item.getImage())
                        .price(parsePrice(item.getLprice()))
                        .brandName(item.getBrand())
                        .sellerName(item.getMallName())
                        .productUrl(item.getLink())
                        .categoryPath(List.of(
                                item.getCategory1(),
                                item.getCategory2(),
                                item.getCategory3(),
                                item.getCategory4()
                        ).stream().filter(v -> v != null && !v.isBlank()).toList())
                        .provider("NAVER")
                        .build())
                .toList();

        return CatalogSearchResponse.builder()
                .provider("NAVER")
                .query(query)
                .total(response.getTotal())
                .start(response.getStart())
                .display(response.getDisplay())
                .items(items)
                .build();
    }

    private Integer parsePrice(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value);
    }
}