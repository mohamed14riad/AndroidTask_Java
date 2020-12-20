package com.android.task.data.model;

import java.util.List;

public class SearchResponse {

    private String searchQuery;
    private List<Product> products;

    public SearchResponse() {
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
