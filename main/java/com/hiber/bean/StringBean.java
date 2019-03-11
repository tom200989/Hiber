package com.hiber.bean;

/*
 * Created by qianli.ma on 2019/2/20 0020.
 * 权限对话框的默认显示实体
 */
public class StringBean {

    private String title;// 标题
    private String content;// 描述内容
    private String cancel;// 取消字符
    private String ok;// 确认字符
    private int colorTitle;// 标题颜色
    private int colorContent;// 描述颜色
    private int colorCancel;// 取消颜色
    private int colorOk;// 确认颜色

    public StringBean() {
    }

    public StringBean(String title, String content, String cancel, String ok) {
        this.title = title;
        this.content = content;
        this.cancel = cancel;
        this.ok = ok;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCancel() {
        return cancel;
    }

    public void setCancel(String cancel) {
        this.cancel = cancel;
    }

    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }

    public int getColorTitle() {
        return colorTitle;
    }

    public void setColorTitle(int colorTitle) {
        this.colorTitle = colorTitle;
    }

    public int getColorContent() {
        return colorContent;
    }

    public void setColorContent(int colorContent) {
        this.colorContent = colorContent;
    }

    public int getColorCancel() {
        return colorCancel;
    }

    public void setColorCancel(int colorCancel) {
        this.colorCancel = colorCancel;
    }

    public int getColorOk() {
        return colorOk;
    }

    public void setColorOk(int colorOk) {
        this.colorOk = colorOk;
    }
}
