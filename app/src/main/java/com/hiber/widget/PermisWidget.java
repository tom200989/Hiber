package com.hiber.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiber.bean.StringBean;
import com.hiber.cons.Cons;
import com.hiber.hiber.R;
import com.hiber.tools.Lgg;
import com.hiber.tools.layout.PercentRelativeLayout;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by qianli.ma on 2019/2/20 0020.
 */
public class PermisWidget extends PercentRelativeLayout {

    private View inflate;
    private Context context;
    private RelativeLayout rlPermissed;// 展示上一个frag的图层(不具有实际功能, 仅是为了实现半透明效果而已)
    private ImageView ivBg;// 灰色背景
    private RelativeLayout rlContentSelf;// 自定义区域
    private RelativeLayout rlContentDefault;// 默认区域
    private TextView tvTitle;// 标题
    private TextView tvContent;// 内容
    private StrongView strongView;// 显示权限列表的视图
    private TextView tvCancel;// 取消
    private TextView tvOk;// 确定

    public PermisWidget(Context context) {
        this(context, null, 0);
    }

    public PermisWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PermisWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.context = context;
        inflate = inflate(context, R.layout.widget_permission, this);
        initView();
        initEvent();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        rlPermissed = inflate.findViewById(R.id.rl_permissed);
        ivBg = inflate.findViewById(R.id.iv_bg);
        rlContentSelf = inflate.findViewById(R.id.rl_content_self);
        rlContentDefault = inflate.findViewById(R.id.rl_content_default);
        tvTitle = inflate.findViewById(R.id.tv_title_default);
        tvContent = inflate.findViewById(R.id.tv_content_default);
        strongView = inflate.findViewById(R.id.sv_content_perList);
        tvCancel = inflate.findViewById(R.id.tv_cancel);
        tvOk = inflate.findViewById(R.id.tv_ok);
    }

    /**
     * 初始化点击时间
     */
    private void initEvent() {
        ivBg.setOnClickListener(v -> Lgg.t(getClass().getSimpleName()).ii("Click PermisWidget Background"));
        tvCancel.setOnClickListener(v -> {
            // 隐藏窗体
            setVisibility(GONE);
            Lgg.t(Cons.TAG2).ii("Click widget cancel");
            // 接口回调
            clickCancelNext();
        });
        tvOk.setOnClickListener(v -> {
            // 隐藏窗体
            setVisibility(GONE);
            Lgg.t(Cons.TAG2).ii("Click widget cancel");
            // 接口回调
            clickOkNext();
        });
    }

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 展示上一个frag的图层(不具有实际功能, 仅是为了实现半透明效果而已)
     *
     * @param layoutId 上一个frag图层的布局layoutId
     */
    public void setLastFragView(@LayoutRes int layoutId) {
        View inflate = View.inflate(context, layoutId, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-1, -1);
        rlPermissed.addView(inflate, lp);
    }

    /**
     * 设置视图
     *
     * @param view       自定义视图(允许为null)
     * @param stringBean 默认视图数据(允许为null, 为Null则使用英文)
     */
    public void setPermissView(@Nullable View view, @Nullable StringBean stringBean, List<String> denyPermisson) {
        Lgg.t(Cons.TAG2).ii("PermissWidget: setPermissView() start");
        rlContentSelf.setVisibility(view == null ? GONE : VISIBLE);
        rlContentDefault.setVisibility(rlContentSelf.getVisibility() == GONE ? VISIBLE : GONE);
        tvContent.setVisibility(stringBean != null ? VISIBLE : GONE);
        strongView.setVisibility(tvContent.getVisibility() == VISIBLE ? GONE : VISIBLE);
        // 自定义视图
        if (view != null) {
            Lgg.t(Cons.TAG2).ii("PermissWidget: setPermissView() view ！= null");
            rlContentSelf.addView(view);
        } else {
            Lgg.t(Cons.TAG2).ii("PermissWidget: setPermissView() view == null");
            if (stringBean != null) {
                Lgg.t(Cons.TAG2).ii("PermissWidget: setPermissView() stringBean != null");
                // 设置内容
                tvTitle.setText(TextUtils.isEmpty(stringBean.getTitle()) ? context.getString(R.string.wd_title) : stringBean.getTitle());
                tvContent.setText(TextUtils.isEmpty(stringBean.getContent()) ? context.getString(R.string.wd_content) : stringBean.getContent());
                tvCancel.setText(TextUtils.isEmpty(stringBean.getCancel()) ? context.getString(R.string.wd_cancel) : stringBean.getCancel());
                tvOk.setText(TextUtils.isEmpty(stringBean.getOk()) ? context.getString(R.string.wd_ok) : stringBean.getOk());
                // 设置颜色
                if (stringBean.getColorTitle() != 0) {
                    tvTitle.setTextColor(stringBean.getColorTitle());
                }
                if (stringBean.getColorContent() != 0) {
                    tvContent.setTextColor(stringBean.getColorContent());
                }
                if (stringBean.getColorCancel() != 0) {
                    tvCancel.setTextColor(stringBean.getColorCancel());
                }
                if (stringBean.getColorOk() != 0) {
                    tvOk.setTextColor(stringBean.getColorOk());
                }

            } else {
                Lgg.t(Cons.TAG2).ii("PermissWidget: setPermissView() stringBean == null");
                tvTitle.setText(context.getString(R.string.wd_title));
                strongView.createDefault(getErrorLogoList(denyPermisson.size()), getPermissDesList(denyPermisson));
                tvCancel.setText(context.getString(R.string.wd_cancel));
                tvOk.setText(context.getString(R.string.wd_ok));
            }
        }
        Lgg.t(Cons.TAG2).ii("PermissWidget: setPermissView() end");
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 获取错误图标
     *
     * @param count 需要创建的数量
     */
    private List<Drawable> getErrorLogoList(int count) {
        List<Drawable> logos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            logos.add(getResources().getDrawable(R.drawable.errors));
        }
        return logos;
    }

    /**
     * 处理权限描述
     *
     * @param denyPermisson 权限原文
     * @return 处理后的集合
     */
    private List<String> getPermissDesList(List<String> denyPermisson) {
        List<String> permisseds = new ArrayList<>();
        for (String deny : denyPermisson) {
            String[] split = deny.split("\\.");
            permisseds.add(split[split.length - 1]);
        }
        return permisseds;
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    private OnClickCancelListener onClickCancelListener;

    // Inteerface--> 接口OnClickCancelListener
    public interface OnClickCancelListener {
        void clickCancel();
    }

    // 对外方式setOnClickCancelListener
    public void setOnClickCancelListener(OnClickCancelListener onClickCancelListener) {
        this.onClickCancelListener = onClickCancelListener;
    }

    // 封装方法clickCancelNext
    private void clickCancelNext() {
        if (onClickCancelListener != null) {
            onClickCancelListener.clickCancel();
        }
    }

    private OnClickOkListener onClickOkListener;

    // Inteerface--> 接口OnClickOkListener
    public interface OnClickOkListener {
        void clickOk();
    }

    // 对外方式setOnClickOkListener
    public void setOnClickOkListener(OnClickOkListener onClickOkListener) {
        this.onClickOkListener = onClickOkListener;
    }

    // 封装方法clickOkNext
    private void clickOkNext() {
        if (onClickOkListener != null) {
            onClickOkListener.clickOk();
        }
    }


}
