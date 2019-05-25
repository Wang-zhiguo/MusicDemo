package cn.wang.glidedemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/6 0006.
 * huangjialin
 * 普通类型的适配器
 */

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {
    private Context mContext;
    private int mLayoutId;
    private List<T> mData;
    //第一步 定义接口
    public interface OnItemClickListener {
        void onClick(int position);
    }

    private OnItemClickListener listener;

    //第二步， 写一个公共的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BaseRecyclerAdapter(Context mContext, int mLayoutId, List<T> mData) {
        this.mContext = mContext;
        this.mLayoutId = mLayoutId;
        this.mData = mData;
        if(mData == null){
            this.mData = new ArrayList<>();
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = BaseViewHolder.getRecyclerHolder(mContext, parent, mLayoutId);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        convert(holder, mData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * 对外提供的方法
     */
    public abstract void convert(BaseViewHolder holder, T t);


    /**
     * 针对多种类型的itemView
     *
     * @param <T>
     */
    public interface ConmonItemType<T> {
        int getLayoutId(int itemType); //不同的Item的布局

        int getItemViewType(int position, T t); //type
    }

    public List<T> getmData() {
        return mData;
    }

    public void setmData(List<T> mData) {
        this.mData = mData;
    }
    public void add2mData(List<T> mData) {
        this.mData.addAll(mData);
    }

}
