package com.example.tangyangkai.myview.citylist;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tangyangkai.myview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangyangkai on 16/7/29.
 */
public class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int FIRST_STICKY_VIEW = 1;
    public static final int HAS_STICKY_VIEW = 2;
    public static final int NONE_STICKY_VIEW = 3;
    private List<City> cityLists = new ArrayList<>();
    private OnItemClickListener listener;

    public CityAdapter(List<City> cityLists, OnItemClickListener listener) {
        this.cityLists = cityLists;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_item, parent, false);
        return new CityViewHolder(view);
    }

    /**
     * 实现RecyclerView 类别标题栏显示
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof CityViewHolder) {
            CityViewHolder viewHolder = (CityViewHolder) holder;
            City city = cityLists.get(position);
            viewHolder.tvCityName.setText(city.getCityName());

            viewHolder.flContentWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.itemClick(position);
                }
            });
            /**
             * 第一个item的头部布局是显示的，设置为View.VISIBLE，标记tag为FIRST_STICKY_VIEW；
             */
            if (position == 0) {
                viewHolder.tvStickyHeader.setVisibility(View.VISIBLE);
                viewHolder.tvStickyHeader.setText(city.getFirstPinYin());
                viewHolder.itemView.setTag(FIRST_STICKY_VIEW);
            } else {
                /**
                 * 每一个RecyclerView的item的布局里面都包含一个头部布局，
                 * 然后判断当前item和上一个item的头部布局里的索引字母是否相同，
                 * 来决定是否展示item的头部布局。
                 */
                if (!TextUtils.equals(city.getFirstPinYin(), cityLists.get(position - 1).getFirstPinYin())) {
                    viewHolder.tvStickyHeader.setVisibility(View.VISIBLE);
                    viewHolder.tvStickyHeader.setText(city.getFirstPinYin());
                    viewHolder.itemView.setTag(HAS_STICKY_VIEW);
                } else {
                    viewHolder.tvStickyHeader.setVisibility(View.GONE);
                    viewHolder.itemView.setTag(NONE_STICKY_VIEW);
                }
            }
            /**
             * 为每一个item设置一个ContentDescription ，用来记录并获取头部布局展示的信息。
             */
            viewHolder.itemView.setContentDescription(city.getFirstPinYin());
        }
    }

    @Override
    public int getItemCount() {
        return cityLists == null ? 0 : cityLists.size();
    }

    public class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvStickyHeader, tvCityName;
        FrameLayout flContentWrapper;
        public CityViewHolder(View itemView) {
            super(itemView);
            tvStickyHeader = (TextView) itemView.findViewById(R.id.tv_sticky_header_view);
            flContentWrapper = (FrameLayout) itemView.findViewById(R.id.fl_content_wrapper);
            tvCityName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
}
