package cn.kevin.labellayout;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 创建日期：2017/11/14.
 *
 * @author yangjinhai
 */

public abstract class LabelAdapter<T> {
    private List<T> mModels;

    public LabelAdapter(List<T> models) {
        mModels = models;
    }

    public List<T> getDatas() {
        return mModels;
    }

    public int getCount() {
        return mModels.size();
    }

    public T getItem(int position) {
        return mModels.get(position);
    }

    public abstract View getLabelView(ViewGroup parent);

    public abstract void onDataSet(View labelView, T item);
}
