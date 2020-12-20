package com.android.task.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductsResponse {

    @SerializedName("products")
    @Expose
    private List<Product> products;

    public ProductsResponse() {
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
