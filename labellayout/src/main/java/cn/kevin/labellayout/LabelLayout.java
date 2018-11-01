package cn.kevin.labellayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2017/11/14.
 *
 * @author yangjinhai
 */

public class LabelLayout extends ViewGroup implements View.OnClickListener {
    private static final int LEFT = 0;
    private static final int CENTER = 1;
    private static final int RIGHT = 2;

    private List<View> mLabelViews;
    private Map<View, LabelRegion> labelRegionMap;
    private int mLabelGravity;
    LabelAdapter mAdapter;
    int mHeight;
    private OnItemClickListener onItemClickListener;
    private boolean isAttached;

    public LabelLayout(Context context) {
        this(context, null);
    }


    public LabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLabelViews = new ArrayList<>();
        labelRegionMap = new HashMap<>();
        initAttr(attrs);
    }

    private void initAttr(AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.LabelLayout);
        mLabelGravity = t.getInt(R.styleable.LabelLayout_label_gravity, 0);
        t.recycle();
    }

    private void setupEvent() {
        if (onItemClickListener != null){
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setOnClickListener(this);
            }
        }
    }

    public void setAdapter(LabelAdapter adapter) {
        mAdapter = adapter;
        removeAllViews();
        mLabelViews.clear();
        labelRegionMap.clear();
        mHeight = 0;
        initView();
    }

    private void initView() {
        if(mAdapter == null){
            return;
        }
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View child = mAdapter.getLabelView(this);
            ViewGroup.MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            if (params == null) {
                params = (MarginLayoutParams) generateDefaultLayoutParams();
            }
            child.setLayoutParams(params);
            mAdapter.onDataSet(child, mAdapter.getItem(i));
            addView(child);
        }

        setupEvent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mHeight == 0){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                //限制Label宽度在父控件宽度的允许范围内
                int width = getPaddingLeft() + layoutParams.leftMargin
                        + child.getMeasuredWidth() + layoutParams.rightMargin + getPaddingRight();
                if (width > getMeasuredWidth()) {
                    width = getMeasuredWidth() - layoutParams.leftMargin
                            - layoutParams.rightMargin - getPaddingLeft() - getPaddingRight();
                    layoutParams.width = width;
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                }
                if (mLabelViews.size() != childCount){
                    mLabelViews.add(child);
                }
            }
            measureLabelRegion();
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measuredWidth = widthMode == MeasureSpec.EXACTLY ?
                getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec)
                : getPaddingLeft() + getPaddingRight();
        int measuredHeight = heightMode == MeasureSpec.EXACTLY ?
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec):
                mHeight;
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    /**
     * 测量出每一个Label的位置
     */
    private void measureLabelRegion() {
        int singleLineHeight = 0;
        int totalLineHeight = 0;
        boolean isLineFirstLabel = true;
        int left = 0 , top, right, bottom;

        int totalWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int usedWidth = 0;
        int lineStartIndex = 0;

        for (int i = 0; i < mLabelViews.size(); i++) {
            View view = mLabelViews.get(i);
            MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
            int childWidth = layoutParams.leftMargin + view.getMeasuredWidth() +
                    layoutParams.rightMargin;
            //如果占用的行宽超过了父控件的宽度就需要换行了
            if (usedWidth + childWidth > totalWidth) {
                //每次换行时都要对完成的行进行对齐处理
                int extraMargin = 0;
                if(mLabelGravity == CENTER ){
                    extraMargin = (totalWidth - usedWidth) / 2;
                }else if(mLabelGravity == RIGHT){
                    extraMargin = totalWidth - usedWidth;
                }
                if(extraMargin > 0){
                    for(int j = lineStartIndex; j < i; j++){
                        View v = mLabelViews.get(j);
                        LabelRegion labelRegion = labelRegionMap.get(v);
                        labelRegion.left += extraMargin;
                        labelRegion.right += extraMargin;
                    }
                }
                lineStartIndex = i;
                isLineFirstLabel = true;
                totalLineHeight += singleLineHeight;
                singleLineHeight = 0;
            }

            if (isLineFirstLabel) {
                left = getPaddingLeft() + layoutParams.leftMargin;
                isLineFirstLabel = false;
                usedWidth = childWidth;
            } else {
                View label = mLabelViews.get(i - 1);
                MarginLayoutParams mlp = (MarginLayoutParams) label.getLayoutParams();
                left += label.getMeasuredWidth() + mlp.leftMargin + layoutParams.rightMargin;
                usedWidth += childWidth;
            }

            top = getPaddingTop() + totalLineHeight + layoutParams.topMargin;
            right = left + view.getMeasuredWidth();
            bottom = top + view.getMeasuredHeight();
            labelRegionMap.put(view, new LabelRegion(left, top, right, bottom));

            //寻找最高行
            int height = layoutParams.topMargin + view.getMeasuredHeight()
                    + layoutParams.bottomMargin;
            if (height > singleLineHeight){
                singleLineHeight = height;
            }
        }
        //处理最后一行
        int extraMargin = 0;
        if(mLabelGravity == CENTER ){
            extraMargin = (totalWidth - usedWidth) / 2;
        }else if(mLabelGravity == RIGHT){
            extraMargin = totalWidth - usedWidth;
        }
        if(extraMargin > 0){
            for(int i = lineStartIndex; i < mLabelViews.size(); i++){
                View v = mLabelViews.get(i);
                LabelRegion labelRegion = labelRegionMap.get(v);
                labelRegion.left += extraMargin;
                labelRegion.right += extraMargin;
            }
        }

        totalLineHeight += singleLineHeight;
        mHeight = getPaddingTop() + getPaddingBottom() + totalLineHeight;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for(View child : mLabelViews){
            LabelRegion msg = labelRegionMap.get(child);
            child.layout(msg.left, msg.top, msg.right, msg.bottom);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }


    public int getAllLabelHeight(){
        return mHeight;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        onItemClickListener = l;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(mLabelViews.indexOf(v));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int p);
    }

    private static final class LabelRegion {
        LabelRegion(int l, int t, int r, int b) {
            left = l;
            top = t;
            right = r;
            bottom = b;
        }
        int left;
        int right;
        int top;
        int bottom;
    }
}
