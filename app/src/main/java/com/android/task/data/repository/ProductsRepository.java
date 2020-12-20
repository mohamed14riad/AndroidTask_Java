package com.android.task.data.repository;

import android.content.Context;
import android.util.Log;

import com.android.task.data.model.Product;
import com.android.task.data.model.ProductsResponse;
import com.android.task.data.model.SearchResponse;
import com.android.task.helpers.JsonHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class ProductsRepository {

    private static final String TAG = "ProductsRepository";

    private Context mContext;

    public ProductsRepository(Context context) {
        this.mContext = context;
    }

    public Single<SearchResponse> searchProducts(String query, int offset, int limit) {
        return Single.create(emitter -> {

            List<Product> products = new ArrayList<>();

            if (query.length() > 2) {
                products.addAll(getProducts(query, offset, limit));
            }

            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setSearchQuery(query);
            searchResponse.setProducts(products);

            emitter.onSuccess(searchResponse);
        });
    }

    private List<Product> getProducts(String query, int offset, int limit) {
        List<Product> products = new ArrayList<>();

        String jsonResponse = JsonHelper.readJSONFromAsset(mContext);
        if (jsonResponse == null) {
            return products;
        }

        try {
            ProductsResponse productsResponse = new Gson().fromJson(jsonResponse, ProductsResponse.class);

            if (productsResponse.getProducts() != null && !productsResponse.getProducts().isEmpty()) {

                List<Product> tempList = new ArrayList<>();
                for (int i = 0; i < productsResponse.getProducts().size(); i++) {
                    if (productsResponse.getProducts().get(i).getTitle().toLowerCase().contains(query)) {
                        tempList.add(productsResponse.getProducts().get(i));
                    }
                }

                if (!tempList.isEmpty() && offset < tempList.size()) {
                    if (offset + limit <= tempList.size()) {
                        products.addAll(tempList.subList(offset, offset + limit));
                    } else {
                        products.addAll(tempList.subList(offset, tempList.size()));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "searchProducts: ", e);
        }

        return products;
    }
}
