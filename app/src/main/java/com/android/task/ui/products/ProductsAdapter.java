package com.android.task.ui.products;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.android.task.R;
import com.android.task.data.model.Product;
import com.android.task.databinding.ItemLoadMoreBinding;
import com.android.task.databinding.ItemProductBinding;

import java.util.ArrayList;
import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private String searchTerm;

    private List<Product> products;

    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_LOADING = 2;

    public ProductsAdapter(Context context) {
        this.mContext = context;
        this.products = new ArrayList<>();
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public void addItems(List<Product> products) {
        this.products.addAll(products);
        notifyDataSetChanged();
    }

    public Product getItem(int index) {
        return products.get(index);
    }

    public void insertItem(Product product) {
        products.add(product);
        notifyItemInserted(products.size() - 1);
    }

    public void removeItem(int index) {
        if (index <= products.size() - 1) {
            products.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void removeAll() {
        searchTerm = null;
        products.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return products.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            ItemProductBinding productBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_product, parent, false);
            return new ProductViewHolder(productBinding);
        } else {
            ItemLoadMoreBinding loadMoreBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_load_more, parent, false);
            return new LoadMoreViewHolder(loadMoreBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProductViewHolder) {
            ProductViewHolder holder = (ProductViewHolder) viewHolder;

            Product product = products.get(position);

            holder.productBinding.tvProductDescription.setText(product.getDescription());
            holder.productBinding.tvProductType.setText(product.getType());
            holder.productBinding.tvProductPrice.setText(String.valueOf(product.getPrice()));


            String content = product.getTitle().toLowerCase();
            int startIndex = content.indexOf(searchTerm);
            int endIndex = startIndex + searchTerm.length();
            if (endIndex > content.length()) {
                endIndex = content.length() - 1;
            }

            SpannableString spannableString = new SpannableString(content);
            spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.productBinding.tvProductTitle.setText(spannableString);
        } else if (viewHolder instanceof LoadMoreViewHolder) {
            LoadMoreViewHolder holder = (LoadMoreViewHolder) viewHolder;

            holder.loadMoreBinding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return products.isEmpty() ? 0 : products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private ItemProductBinding productBinding;

        ProductViewHolder(ItemProductBinding productBinding) {
            super(productBinding.getRoot());
            this.productBinding = productBinding;
        }
    }

    static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        private ItemLoadMoreBinding loadMoreBinding;

        LoadMoreViewHolder(ItemLoadMoreBinding loadMoreBinding) {
            super(loadMoreBinding.getRoot());
            this.loadMoreBinding = loadMoreBinding;
        }
    }
}
