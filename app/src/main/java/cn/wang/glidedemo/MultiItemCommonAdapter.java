package cn.wang.glidedemo;

import android.content.Context;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 2017/11/6 0006.
 * huangjialin
 * 如果列表的布局item有多种类型，则需要继承该适配器
 */

public abstract class MultiItemCommonAdapter<T> extends BaseRecyclerAdapter<T> {
    private ConmonItemType<T> mConmonItemType;
    private List<T> mDatas;
    private Context mContext;


    public MultiItemCommonAdapter(Context mContext, List<T> mData, ConmonItemType<T> conmonItemType) {
        super(mContext, -1, mData);
        this.mConmonItemType = conmonItemType;
        mDatas = mData;
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        return mConmonItemType.getItemViewType(position, mDatas.get(position));
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = mConmonItemType.getLayoutId(viewType);
        BaseViewHolder holder = BaseViewHolder.getRecyclerHolder(mContext, parent, layoutId);
        return holder;
    }
}
