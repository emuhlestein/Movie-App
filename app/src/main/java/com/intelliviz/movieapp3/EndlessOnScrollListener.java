package com.intelliviz.movieapp3;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.View;

/**
 * Created by edm on 4/11/2016.
 */
public abstract class EndlessOnScrollListener extends OnScrollListener {
    private static final String LOG_TAG = EndlessOnScrollListener.class.getSimpleName();
    private int mCurrentPage;
    private boolean mLoading = false;
    private GridLayoutManager mGridLayoutManager;

    public EndlessOnScrollListener(GridLayoutManager gridLayoutManager) {
        mGridLayoutManager = gridLayoutManager;
        mCurrentPage = 1;
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        super.onScrolled(view, dx, dy);

        int lastVisibleItemPositions = mGridLayoutManager.findLastVisibleItemPosition();
        int count = mGridLayoutManager.getItemCount();
        int spanCount = mGridLayoutManager.getSpanCount();
        int currentRow = lastVisibleItemPositions/spanCount;
        int numberOfRows = count/spanCount;
        int threshold = count-1;
        int first = mGridLayoutManager.findFirstCompletelyVisibleItemPosition();

        View v = view.getChildAt(0);
        //boolean atBeginning =

        Log.d(LOG_TAG, "Last visible item: " + lastVisibleItemPositions + "  Item Count: " + count + "  Page: " + mCurrentPage + "  Threshold: " + threshold + " First Item: " + first);

        if(lastVisibleItemPositions == threshold && !mLoading) {
            mLoading = true;
            mCurrentPage++;
            onLoadMore(mCurrentPage);
            mLoading = false;
        } else if(first == 0 && !mLoading) {
            if(mCurrentPage - 1 >= 1) {
                mLoading = true;
                mCurrentPage--;
                onLoadMore(mCurrentPage);
                mLoading = false;
            }
        }
    }

    public abstract void onLoadMore(int currentPage);
}
