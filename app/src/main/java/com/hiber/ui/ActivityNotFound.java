package com.hiber.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.hiber.cons.Cons;
import com.hiber.hiber.R;
import com.hiber.tools.Lgg;

public class ActivityNotFound extends Activity {

    /**
     * 全局异常捕获的标记KEY
     *
     * @apiNote 被CrashHelper.java引用
     */
    public static String ERROR_INTENT = "ERROR_INTENT";

    private TextView tvErrAction;// 描述信息
    private TextView tvOKAction;// OK面板

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_not_found);
        // 设置描述信息
        tvErrAction = findViewById(R.id.tv_des_action);
        String errInfo = getIntent().getStringExtra(ERROR_INTENT);
        tvErrAction.setText(errInfo);
        Lgg.t(Cons.TAG).ee(errInfo);
        // 设置点击事件
        tvOKAction = findViewById(R.id.tv_ok_action);
        tvOKAction.setOnClickListener(v -> {
            // 结束进程
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 结束进程
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
