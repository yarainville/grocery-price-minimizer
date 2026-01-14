package com.grocery.grocery_price_minimizer.controller;

import com.grocery.grocery_price_minimizer.model.ProductSearchResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    private final ProductSearchService searchService;

    public SearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<ProductSearchResult> search(
            @RequestParam long basketId,
            @RequestParam String item
    ) {
        return searchService.searchWithSelections(basketId, item);
    }
}