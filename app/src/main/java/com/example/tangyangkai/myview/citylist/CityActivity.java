package com.example.tangyangkai.myview.citylist;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tangyangkai.myview.CircleTextView;
import com.example.tangyangkai.myview.R;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 一 利用RecyclerView生成:
 *      1 支持类别标题栏显示
 *      2 支持顶部标题栏炫富显示
 *
 * 二 自定义快速导航栏显示选中,并跳转选中类别标题处
 *      1 onDraw绘制矩形和字符
 *      2 onTouch自定义
 *
 * 三 自动中文拼音排序
 *
 */

public class CityActivity extends Activity implements MySlideView.onTouchListener, OnItemClickListener {


    private List<City> cityList = new ArrayList<>();
    private Set<String> firstCityPinYinSet = new LinkedHashSet<>();
    public static List<String> firstCityPinYinList = new ArrayList<>();
    private PinyinComparator pinyinComparator;

    private MySlideView characterNevList;
    private CircleTextView circleTxt;

    private RecyclerView recyclerView;
    private TextView tvStickyHeaderView;
    private CityAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        characterNevList = (MySlideView) findViewById(R.id.my_slide_view);
        circleTxt = (CircleTextView) findViewById(R.id.my_circle_view);
        tvStickyHeaderView = (TextView) findViewById(R.id.tv_sticky_header_view);
        recyclerView = (RecyclerView) findViewById(R.id.rv_sticky_example);
    }

    private void initData() {
        cityList.clear();
        firstCityPinYinSet.clear();
        firstCityPinYinList.clear();

        /**
         * 数据准备,构建城市对象列表,包括中文名/拼音
         */
        for (int i = 0; i < City.stringCitys.length; i++) {
            City city = new City();
            city.setCityName(City.stringCitys[i]);
            //汉子-->拼音
            city.setCityPinyin(transformPinYin(City.stringCitys[i]));
            cityList.add(city);
        }
        /**
         * 构建Comparator子类实现按照拼音字母排序
         * 获取排好序的cityList, 用做recyclerView的数据源
         */
        pinyinComparator = new PinyinComparator();
        Collections.sort(cityList, pinyinComparator);

        /**
         * firstCityPinYinSet是Set,通过它来去处重复的首字母
         * 然后无重复的首字母保存到firstCityPinYinList这个List中
         * 获取无重复firstCityPinYinList,用做characterNevList数据源
         */
        for (City city : cityList) {
            firstCityPinYinSet.add(city.getCityPinyin().substring(0, 1));
        }
        for (String string : firstCityPinYinSet) {
            firstCityPinYinList.add(string);
        }

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CityAdapter(cityList, this);
        recyclerView.setAdapter(adapter);
    }

    private void initListener(){
        characterNevList.setListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                /**
                 * 实现顶部悬浮
                 */

                /**
                 * 第一次调用RecyclerView的findChildViewUnder()方法，返回指定位置的childView，
                 * 这里也就是item的头部布局，
                 * 因为我们的tvStickyHeaderView展示的肯定是最上面item的头部布局里的索引字母信息。
                 */
                View stickyInfoView = recyclerView.findChildViewUnder(
                        tvStickyHeaderView.getMeasuredWidth() / 2, 5);

                if (stickyInfoView != null && stickyInfoView.getContentDescription() != null) {
                    tvStickyHeaderView.setText(String.valueOf(stickyInfoView.getContentDescription()));
                }

                /**
                 * 第二次调用RecyclerView的findChildViewUnder()方法，
                 * 这里返回的是固定在屏幕上方那个tvStickyHeaderView下面一个像素位置的RecyclerView的item，
                 * 根据这个item来更新tvStickyHeaderView要translate多少距离。
                 */

                View transInfoView = recyclerView.findChildViewUnder(
                        tvStickyHeaderView.getMeasuredWidth() / 2, tvStickyHeaderView.getMeasuredHeight() + 1);

                if (transInfoView != null && transInfoView.getTag() != null) {
                    int transViewStatus = (int) transInfoView.getTag();
                    int dealtY = transInfoView.getTop() - tvStickyHeaderView.getMeasuredHeight();
                    if (transViewStatus == CityAdapter.HAS_STICKY_VIEW) {
                        if (transInfoView.getTop() > 0) {
                            tvStickyHeaderView.setTranslationY(dealtY);
                        } else {
                            tvStickyHeaderView.setTranslationY(0);
                        }
                    } else if (transViewStatus == CityAdapter.NONE_STICKY_VIEW) {
                        tvStickyHeaderView.setTranslationY(0);
                    }
                }
            }
        });
    }

    @Override
    public void itemClick(int position) {
        Toast.makeText(getApplicationContext(), "你选择了:" + cityList.get(position).getCityName(), Toast.LENGTH_SHORT).show();
    }

    /**
     *这里我用的是TinyPinyin，一个适用于Java和Android的快速、低内存占用的汉字转拼音库。
     * TinyPinyin的特点有：
     *      生成的拼音不包含声调，也不处理多音字，默认一个汉字对应一个拼音；
     *      拼音均为大写；
     *      无需初始化，执行效率很高(Pinyin4J的4倍)；很低的内存占用（小于30KB）。
     * @param character
     * @return
     */

    public String transformPinYin(String character) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < character.length(); i++) {
            buffer.append(Pinyin.toPinyin(character.charAt(i)));
        }
        return buffer.toString();
    }

    @Override
    public void showTextView(String textView, boolean dismiss) {

        if (dismiss) {
            circleTxt.setVisibility(View.GONE);
        } else {
            circleTxt.setVisibility(View.VISIBLE);
            circleTxt.setText(textView);
        }

        int selectPosition = 0;
        for (int i = 0; i < cityList.size(); i++) {
            if (cityList.get(i).getFirstPinYin().equals(textView)) {
                selectPosition = i;
                break;
            }
        }
        recyclerView.scrollToPosition(selectPosition);

    }

}
