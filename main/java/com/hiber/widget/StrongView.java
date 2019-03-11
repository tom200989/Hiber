package com.hiber.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiber.tools.ScreenSize;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by qianli.ma on 2019/3/9 0008.
 */
public class StrongView extends LinearLayout {

    private final String TAG = "NotPermissWidget";

    /**
     * 整体控件的最大高度
     */
    private float SV_MAX_HEIGHT_SELF = 0.8f;

    /**
     * 条目占总布局高百分比
     */
    private float ITEM_HEIGHT_PERCENT = 0.05f;

    /**
     * 图标占所在布局的高百分比
     */
    private float IV_HEIGHT_PERCENT = 0.5f;

    /**
     * 文本占所在布局的高百分比
     */
    private float TV_SIZE_PERCENT = 0.5f;

    /**
     * 图标左侧间距
     */
    private int IV_MARGIN_START_DP = 0;

    /**
     * 图标右侧间距
     */
    private int IV_MARGIN_END_DP = 0;

    /**
     * 文本距离图标的左侧间距
     */
    private int TV_MARGIN_START_TO_IV_DP = 15;

    /**
     * 文本距离图标的右侧间距
     */
    private int TV_MARGIN_END_TO_IV_DP = 15;

    /**
     * planA模式下条目布局ID
     */
    private final int LINEARLAYOUT_ID = 0x0011;

    /**
     * planA模式下图标ID
     */
    private final int IMAGEVIEW_ID = 0x0012;

    /**
     * planA模式下文本ID
     */
    private final int TEXTVIEW_ID = 0x0013;

    private List<Drawable> draws = new ArrayList<>();
    private List<String> contentList = new ArrayList<>();
    private WDAdapter wdAdapter;


    public StrongView(Context context) {
        this(context, null, 0);
    }

    public StrongView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrongView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 允许的最大高度
        SV_MAX_HEIGHT_SELF = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res-auto", "SV_MAX_HEIGHT_SELF", 0.8f);
        // 条目占总布局高百分比
        ITEM_HEIGHT_PERCENT = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res-auto", "sv_item_height_percent", 0.05f);
        // 图标占所在布局的高百分比
        IV_HEIGHT_PERCENT = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res-auto", "sv_iv_height_percent", 0.5f);
        // 文本占所在布局的高百分比
        TV_SIZE_PERCENT = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res-auto", "sv_tv_size_percent", 0.5f);
        // 图标左侧间距
        IV_MARGIN_START_DP = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "sv_iv_margin_start_dp", 0);
        // 图标右侧间距
        IV_MARGIN_END_DP = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "sv_iv_margin_end_dp", 0);
        // 文本距离图标的左侧间距
        TV_MARGIN_START_TO_IV_DP = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "sv_tv_margin_start_to_iv_dp", 15);
        // 文本距离图标的右侧间距
        TV_MARGIN_END_TO_IV_DP = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "sv_tv_margin_end_to_iv_dp", 15);
        // 校对参数合理性
        checkRuleAttrs();
    }

    /**
     * 校对参数合理性
     */
    private void checkRuleAttrs() {
        // 允许的最大高度
        if (SV_MAX_HEIGHT_SELF > 1 | SV_MAX_HEIGHT_SELF < 0) {
            SV_MAX_HEIGHT_SELF = 1;
        }
        // 条目占总布局高百分比
        if (ITEM_HEIGHT_PERCENT > 1 | ITEM_HEIGHT_PERCENT < 0) {
            ITEM_HEIGHT_PERCENT = 1;
        }
        // 图标占所在布局的高百分比
        if (IV_HEIGHT_PERCENT > 1 | IV_HEIGHT_PERCENT < 0) {
            IV_HEIGHT_PERCENT = 1;
        }
        // 文本占所在布局的高百分比
        if (TV_SIZE_PERCENT > 1 | TV_SIZE_PERCENT < 0) {
            TV_SIZE_PERCENT = 1;
        }
        // 图标左侧间距
        if (IV_MARGIN_START_DP < 0) {
            IV_MARGIN_START_DP = 0;
        }
        // 图标右侧间距
        if (IV_MARGIN_END_DP < 0) {
            IV_MARGIN_END_DP = 0;
        }
        // 文本距离图标的左侧间距
        if (TV_MARGIN_START_TO_IV_DP < 0) {
            TV_MARGIN_START_TO_IV_DP = 0;
        }
        // 文本距离图标的右侧间距
        if (TV_MARGIN_END_TO_IV_DP < 0) {
            TV_MARGIN_END_TO_IV_DP = 0;
        }
    }

    /* 重写该方法以限制最大值 */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            //最大高度显示为屏幕内容高度的一半
            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            DisplayMetrics d = new DisplayMetrics();
            display.getMetrics(d);
            //此处是关键，设置控件高度不能超过屏幕高度一半（d.heightPixels / 2）（在此替换成自己需要的高度）
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (d.heightPixels * SV_MAX_HEIGHT_SELF), MeasureSpec.AT_MOST);

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 默认模式
     *
     * @param contentList 内容列表p
     */
    public void createDefault(List<Drawable> draws, List<String> contentList) {

        // 0.进行数量匹对
        boolean isEqualSize = checkSizeEqual(draws, contentList);
        if (!isEqualSize) {
            Toast.makeText(getContext(), "图标与文本数量不等", Toast.LENGTH_LONG * 5000).show();
            return;
        }

        // 1.获取自定义widget区域最佳的容纳数量
        int bestCount = getContainCount();
        // 2.判断是否超限?
        if (contentList.size() > bestCount) {
            // 3.采用列表布局方案
            PlanA();
        } else {
            // 3.采用可变布局方案
            PlanB(draws, contentList);
        }
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 进行数量匹对
     *
     * @param draws       LOGO集合
     * @param contentList 文本集合
     */
    private boolean checkSizeEqual(List<Drawable> draws, List<String> contentList) {
        // 0.传递全局变量
        this.draws = draws;
        this.contentList = contentList;

        // 1.防止空值
        if (this.draws == null) {
            this.draws = new ArrayList<>();
        }

        if (this.contentList == null) {
            this.contentList = new ArrayList<>();
        }

        // 2.判断数量
        if (this.draws.size() != this.contentList.size()) {

            if (this.draws.size() > this.contentList.size()) {// 2.1.文本漏写
                // 2.2.补充需要的文本数量(空文本)
                int count = this.draws.size() - this.contentList.size();
                for (int i = 0; i < count; i++) {
                    this.contentList.add("");
                }

            } else {// 2.1.图标漏写
                // 2.2.补充需要的图标数量(透明视图)
                int count = this.contentList.size() - this.draws.size();
                for (int i = 0; i < count; i++) {
                    this.draws.add(new ColorDrawable(Color.TRANSPARENT));
                }

            }
        }

        return this.draws.size() == this.contentList.size();
    }

    /**
     * 获取自定义widget区域最佳的容纳数量
     *
     * @return 自定义widget区域最佳的容纳数量
     */
    private int getContainCount() {
        // 1.获取所在布局的marginTop & marginottom
        int[] marginTopBottom = getMarginTopAndBottom();
        // 2.获取可见布局总大小
        ScreenSize.SizeBean size = ScreenSize.getSize(getContext());
        int totalHeight = size.height;
        // 3.获取剩余布局大小
        int remainHeight = totalHeight - marginTopBottom[0] - marginTopBottom[1];
        // 4.获取每个item大小
        int itemHeight = (int) (totalHeight * ITEM_HEIGHT_PERCENT);
        // 5.计算剩余布局可容纳个数
        int count = remainHeight / itemHeight;
        // 6.得出最理想的个数 = 剩余布局可容纳个数 - 1
        int bestCount = count - 1;
        return bestCount;
    }

    private void PlanA() {
        // 1.创建列表控件
        RecyclerView rcv = new RecyclerView(getContext());
        rcv.setOverScrollMode(OVER_SCROLL_NEVER);
        rcv.setVerticalScrollBarEnabled(false);
        // 2.配置布局
        LayoutParams rvp = new LayoutParams(-1, -1);
        rcv.setLayoutParams(rvp);
        // 3.设置布局管理器
        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // 4.绑定布局管理器
        rcv.setLayoutManager(lm);
        // 6.添加到布局
        addView(rcv);
        // 5.创建适配器
        wdAdapter = new WDAdapter();
        rcv.setAdapter(wdAdapter);

    }

    /**
     * 方案2
     *
     * @param draws       图标集合
     * @param contentList 内容集合
     */
    private void PlanB(List<Drawable> draws, List<String> contentList) {
        // 1.新增包裹布局--> 注: 直接addview在跟布局是不能的
        LinearLayout llContent = new LinearLayout(getContext());
        llContent.setOrientation(LinearLayout.VERTICAL);
        LayoutParams lp = new LayoutParams(-2, -2);
        llContent.setLayoutParams(lp);
        // 2.设置logo和内容
        for (int i = 0; i < contentList.size(); i++) {
            LinearLayout ll_item = createItemLayoutInPlanB(draws.get(i), contentList.get(i));
            llContent.addView(ll_item);
        }
        // 3.添加到布局
        addView(llContent);
    }

    /**
     * 获取所在布局的上下间距
     *
     * @return 所在布局的上下间距
     * @apiNote param1: marginTop
     * @apiNote param2: marginBottom
     */
    private int[] getMarginTopAndBottom() {
        // 1.初始化margin
        int marginTop = 0;
        int marginBottom = 0;

        // 2.获取所在布局的属性 (按照类型强转后获取)
        ViewGroup.LayoutParams pp = getLayoutParams();
        if (pp instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams pr = (RelativeLayout.LayoutParams) pp;
            marginTop = pr.topMargin;
            marginBottom = pr.bottomMargin;

        } else if (pp instanceof LayoutParams) {
            LayoutParams pl = (LayoutParams) pp;
            marginTop = pl.topMargin;
            marginBottom = pl.bottomMargin;

        } else if (pp instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams pf = (FrameLayout.LayoutParams) pp;
            marginTop = pf.topMargin;
            marginBottom = pf.bottomMargin;

        }
        return new int[]{marginTop, marginBottom};
    }

    /**
     * PLAN A 创建item布局
     *
     * @return Item条目
     */
    private LinearLayout createItemLayoutInPlanA() {

        // 0.获取屏幕尺寸
        ScreenSize.SizeBean size = ScreenSize.getSize(getContext());
        // 1.0.定义线性布局高度 -- 默认: 5%sh
        int itemHeight = (int) (size.height * ITEM_HEIGHT_PERCENT);
        // 1.1.创建一个线性布局
        LinearLayout ll_item = new LinearLayout(getContext());
        // 1.2.配置布局属性
        LayoutParams lp = new LayoutParams(-1, itemHeight);
        // 1.3.绑定布局与属性
        ll_item.setLayoutParams(lp);
        // 1.4.设置水平排列以及垂直居中
        ll_item.setOrientation(HORIZONTAL);
        ll_item.setVerticalGravity(Gravity.CENTER_VERTICAL);
        // 1.5.设置id
        ll_item.setId(LINEARLAYOUT_ID);

        // 2.0.定义iv大小 -- 默认: 50%h
        int ivh = (int) (itemHeight * IV_HEIGHT_PERCENT);
        // 2.1.创建iv
        ImageView iv = new ImageView(getContext());
        // 2.2.设置填充
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        // 2.2.配置iv属性
        LayoutParams ip = new LayoutParams(ivh, ivh);
        ip.setMarginStart(IV_MARGIN_START_DP);
        ip.setMarginEnd(IV_MARGIN_END_DP);
        // 2.3.绑定布局与属性
        iv.setLayoutParams(ip);
        // 2.4.设置ID
        iv.setId(IMAGEVIEW_ID);

        // 3.0.定义字体大小 -- 默认: 50%h
        int tvs = (int) (itemHeight * TV_SIZE_PERCENT);
        // 3.1.创建tv, 并设置最大行数为1
        TextView tv = new TextView(getContext());
        tv.setMaxLines(1);
        // 3.2.设置大小以及垂直居中
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvs);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        // 3.3.设置颜色
        tv.setTextColor(Color.parseColor("#000000"));
        // 3.4.设置间距 -- 默认: 15dp
        LayoutParams tp = new LayoutParams(-1, -1);
        tp.setMarginStart(TV_MARGIN_START_TO_IV_DP);
        tp.setMarginEnd(TV_MARGIN_END_TO_IV_DP);
        // 3.5.绑定属性
        tv.setLayoutParams(tp);
        // 3.6.设置ID
        tv.setId(TEXTVIEW_ID);

        // 4.添加iv和tv到ll中
        ll_item.addView(iv);
        ll_item.addView(tv);

        return ll_item;
    }

    /**
     * PLAN B 创建item布局
     *
     * @param drawable 图标
     * @param content  内容
     * @return Item条目
     */
    private LinearLayout createItemLayoutInPlanB(Drawable drawable, String content) {
        // 0.获取屏幕尺寸
        ScreenSize.SizeBean size = ScreenSize.getSize(getContext());

        // 1.0.定义线性布局高度 -- 默认: 5%sh
        int itemHeight = (int) (size.height * ITEM_HEIGHT_PERCENT);
        // 1.1.创建一个线性布局
        LinearLayout ll_item = new LinearLayout(getContext());
        // 1.2.配置布局属性
        LayoutParams lp = new LayoutParams(-1, itemHeight);
        // 1.3.绑定布局与属性
        ll_item.setLayoutParams(lp);
        // 1.4.设置水平排列以及垂直居中
        ll_item.setOrientation(HORIZONTAL);
        ll_item.setVerticalGravity(Gravity.CENTER_VERTICAL);

        // 2.0.定义iv大小 -- 默认: 50%h
        int ivh = (int) (itemHeight * IV_HEIGHT_PERCENT);
        // 2.1.创建iv
        ImageView iv = new ImageView(getContext());
        // 2.2.设置填充
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        // 2.2.配置iv属性
        LayoutParams ip = new LayoutParams(ivh, ivh);
        ip.setMarginStart(IV_MARGIN_START_DP);
        ip.setMarginEnd(IV_MARGIN_END_DP);
        // 2.3.绑定布局与属性
        iv.setLayoutParams(ip);
        // 2.4.设置图片
        iv.setImageDrawable(drawable);

        // 3.0.定义字体大小 -- 默认: 50%h
        int tvs = (int) (itemHeight * TV_SIZE_PERCENT);
        // 3.1.创建tv, 并设置最大行数为1
        TextView tv = new TextView(getContext());
        tv.setMaxLines(1);
        // 3.2.设置大小以及垂直居中
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvs);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        // 3.3.设置颜色
        tv.setTextColor(Color.parseColor("#000000"));
        // 3.4.设置间距 -- 默认: 15dp
        LayoutParams tp = new LayoutParams(-1, -1);
        tp.setMarginStart(TV_MARGIN_START_TO_IV_DP);
        tp.setMarginEnd(TV_MARGIN_END_TO_IV_DP);
        // 3.5.绑定属性
        tv.setLayoutParams(tp);
        // 3.6.设置文本内容
        tv.setText(content);

        // 4.添加iv和tv到ll中
        ll_item.addView(iv);
        ll_item.addView(tv);

        return ll_item;
    }

    /* -------------------------------------------- class: adapter -------------------------------------------- */

    /**
     * 控件适配器
     */
    public class WDAdapter extends RecyclerView.Adapter<WDHodler> {
        @NonNull
        @Override
        public WDHodler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // TOGO: 以下两行的写法是错误的
            // TOGO: ViewHolder views must not be attached when created. Ensure that you are not passing 'true' to the attachToRoot
            // TOGO: LinearLayout itemLayoutInPlanA = createItemLayoutInPlanA();
            // TOGO: viewGroup.addView(itemLayoutInPlanA);

            // 这种写法是正确的--> 应直接返回创建的Layout即可
            return new WDHodler(createItemLayoutInPlanA());
        }

        @Override
        public void onBindViewHolder(@NonNull WDHodler wdHodler, int i) {
            wdHodler.ivLogo.setImageDrawable(draws.get(i));
            wdHodler.tvContent.setText(contentList.get(i));
        }

        @Override
        public int getItemCount() {
            return draws.size();
        }
    }

    /**
     * 控件盒子
     */
    public class WDHodler extends RecyclerView.ViewHolder {

        public LinearLayout llItem;// Item布局
        public ImageView ivLogo;// 图标
        public TextView tvContent;// 内容

        public WDHodler(@NonNull View itemView) {
            super(itemView);
            llItem = itemView.findViewById(LINEARLAYOUT_ID);
            ivLogo = itemView.findViewById(IMAGEVIEW_ID);
            tvContent = itemView.findViewById(TEXTVIEW_ID);
        }
    }
}
