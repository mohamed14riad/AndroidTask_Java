package com.android.task.ui.products;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.task.R;
import com.android.task.data.model.SearchResponse;
import com.android.task.data.repository.ProductsRepository;
import com.android.task.databinding.FragmentProductsBinding;
import com.android.task.helpers.VerticalRecyclerViewMargin;
import com.google.gson.Gson;
import com.jakewharton.rxbinding4.widget.RxSearchView;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductsFragment extends Fragment {

    private static final String TAG = "ProductsFragment";

    private FragmentProductsBinding fragmentBinding;

    private ProductsRepository productsRepository;

    private CompositeDisposable compositeDisposable;

    private ProductsAdapter productsAdapter;

    private SearchResponse lastSearchResponse;

    private int offset = 0;
    private final int limit = 10;
    private boolean isLoading;

    public ProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentBinding = FragmentProductsBinding.inflate(inflater, container, false);

        productsRepository = new ProductsRepository(getActivity());

        compositeDisposable = new CompositeDisposable();

        productsAdapter = new ProductsAdapter(getActivity());

        VerticalRecyclerViewMargin decoration = new VerticalRecyclerViewMargin(24, 1);

        fragmentBinding.rvProducts.addItemDecoration(decoration);
        fragmentBinding.rvProducts.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        fragmentBinding.rvProducts.setAdapter(productsAdapter);

        fragmentBinding.fabScrollTop.setOnClickListener(v -> {
            fragmentBinding.rvProducts.scrollToPosition(0);
        });

        return fragmentBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.item_search);

        SearchView searchView = (SearchView) searchItem.getActionView();

        Disposable queryDisposable = RxSearchView.queryTextChanges(searchView)
                .skip(1)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Throwable {
                        offset = 0;
                        return charSequence != null ? charSequence.toString().trim().toLowerCase() : "";
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> {

                    fragmentBinding.loadingIndicator.setVisibility(View.VISIBLE);

                    Single<SearchResponse> responseObservable = productsRepository.searchProducts(query, offset, limit);
                    Disposable disposable = responseObservable.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(searchResponse -> {
                                Log.d(TAG, "onCreateOptionsMenu: searchResponse = " + new Gson().toJson(searchResponse));

                                fragmentBinding.loadingIndicator.setVisibility(View.GONE);

                                lastSearchResponse = searchResponse;

                                productsAdapter.removeAll();

                                if (searchResponse.getSearchQuery().isEmpty()) {
                                    fragmentBinding.tvMessageStartTyping.setVisibility(View.VISIBLE);
                                    fragmentBinding.tvErrorMessage.setVisibility(View.GONE);
                                    fragmentBinding.rvProducts.setVisibility(View.GONE);
                                    fragmentBinding.fabScrollTop.setVisibility(View.GONE);
                                } else {
                                    fragmentBinding.tvMessageStartTyping.setVisibility(View.GONE);
                                    fragmentBinding.tvErrorMessage.setVisibility(View.GONE);
                                    fragmentBinding.rvProducts.setVisibility(View.VISIBLE);
                                    fragmentBinding.rvProducts.scrollToPosition(0);

                                    if (searchResponse.getProducts().isEmpty()) {
                                        fragmentBinding.rvProducts.setVisibility(View.GONE);
                                        fragmentBinding.fabScrollTop.setVisibility(View.GONE);
                                        fragmentBinding.tvErrorMessage.setVisibility(View.VISIBLE);
                                        fragmentBinding.tvErrorMessage.setText(R.string.no_products_found);
                                    } else {
                                        productsAdapter.setSearchTerm(searchResponse.getSearchQuery());
                                        productsAdapter.addItems(searchResponse.getProducts());

                                        loadMore();
                                    }
                                }
                            }, e -> {
                                Log.e(TAG, "onCreateOptionsMenu: ", e);

                                fragmentBinding.loadingIndicator.setVisibility(View.GONE);
                                fragmentBinding.rvProducts.setVisibility(View.GONE);
                                fragmentBinding.tvMessageStartTyping.setVisibility(View.GONE);
                                fragmentBinding.tvErrorMessage.setVisibility(View.VISIBLE);
                                fragmentBinding.tvErrorMessage.setText(R.string.error_occurred);
                            });
                    compositeDisposable.add(disposable);

                }, e -> {
                    Log.e(TAG, "onCreateOptionsMenu: ", e);

                    fragmentBinding.loadingIndicator.setVisibility(View.GONE);
                    fragmentBinding.rvProducts.setVisibility(View.GONE);
                    fragmentBinding.tvMessageStartTyping.setVisibility(View.GONE);
                    fragmentBinding.tvErrorMessage.setVisibility(View.VISIBLE);
                    fragmentBinding.tvErrorMessage.setText(R.string.error_occurred);
                });
        compositeDisposable.add(queryDisposable);
    }

    private void loadMore() {
        fragmentBinding.rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null && layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    fragmentBinding.fabScrollTop.setVisibility(View.GONE);
                } else {
                    fragmentBinding.fabScrollTop.setVisibility(View.VISIBLE);
                }


                if (productsAdapter.getItemCount() >= 20 || offset >= 10 || lastSearchResponse.getProducts().size() < limit) {
                    return;
                }

                if (!isLoading) {
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == productsAdapter.getItemCount() - 1) {
                        isLoading = true;
                        productsAdapter.insertItem(null);

                        offset = offset + limit;

                        getMoreProducts(lastSearchResponse.getSearchQuery());
                    }
                }
            }
        });
    }

    private void getMoreProducts(String query) {

        fragmentBinding.loadingIndicator.setVisibility(View.VISIBLE);

        Single<SearchResponse> responseObservable = productsRepository.searchProducts(query, offset, limit);
        Disposable disposable = responseObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchResponse -> {
                    Log.d(TAG, "getMoreProducts: searchResponse = " + new Gson().toJson(searchResponse));

                    fragmentBinding.loadingIndicator.setVisibility(View.GONE);

                    lastSearchResponse = searchResponse;

                    if (searchResponse.getProducts().isEmpty()) {
                        if (productsAdapter.getItemCount() > 0 && productsAdapter.getItem(productsAdapter.getItemCount() - 1) == null) {
                            productsAdapter.removeItem(productsAdapter.getItemCount() - 1);
                            isLoading = false;
                        }
                    } else {
                        if (productsAdapter.getItemCount() > 0 && productsAdapter.getItem(productsAdapter.getItemCount() - 1) == null) {
                            productsAdapter.removeItem(productsAdapter.getItemCount() - 1);
                            isLoading = false;
                        }

                        productsAdapter.addItems(searchResponse.getProducts());
                    }
                }, e -> {
                    Log.e(TAG, "getMoreProducts: ", e);

                    fragmentBinding.loadingIndicator.setVisibility(View.GONE);
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }
}
