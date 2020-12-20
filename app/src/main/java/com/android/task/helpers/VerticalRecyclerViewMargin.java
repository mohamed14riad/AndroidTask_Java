package com.android.task.helpers;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class VerticalRecyclerViewMargin extends RecyclerView.ItemDecoration {

    private int margin;
    private final int columns;

    /**
     * constructor
     *
     * @param margin  desirable margin size in px between the views in the recyclerView
     * @param columns number of columns of the RecyclerView
     */
    public VerticalRecyclerViewMargin(@IntRange(from = 0) int margin, @IntRange(from = 0) int columns) {
        this.margin = margin;
        this.columns = columns;
    }

    /**
     * Set different margins for the items inside the recyclerView: no top margin for the first row
     * and no left margin for the first column.
     */
    @Override
    public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {

        int position = parent.getChildLayoutPosition(view);

        // set bottom margin to all
        outRect.bottom = margin;

        // we only add top margin to the first row
        if (position < columns) {
            outRect.top = margin;
        }

        // set right margin to all
        outRect.right = margin;

        // add left margin only to the first column
        if (position % columns == 0) {
            outRect.left = margin;
        }
    }
}
