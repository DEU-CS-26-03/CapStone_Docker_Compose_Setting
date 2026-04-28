// Cm29CatalogSearchRequest.java
package com.capstone.web_api.cm29.dto;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Cm29CatalogSearchRequest {
    private String query;
    private int page = 1;
    private int size = 20;
}