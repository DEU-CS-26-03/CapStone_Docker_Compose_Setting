// CategoryResponse.java
package com.capstone.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryResponse {
    private String code;
    private String name;
    private int depth;
    private String parentCode;
    private List<CategoryResponse> children;
}