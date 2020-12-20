package com.android.task.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.task.R;
import com.android.task.databinding.ActivityMainBinding;
import com.android.task.ui.products.ProductsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding activityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProductsFragment())
                .commit();
    }
}
