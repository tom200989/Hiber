package com.hiber.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hiber.hiber.R;
import com.lintcheck.lintcheck.helper.LintHelper;
import com.lintcheck.lintcheck.helper.LintWidget;

import java.util.List;

/*
 * Created by qianli.ma on 2019/4/18 0018.
 */
public class LintActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lint);
        Intent intent = getIntent();
        List<Integer> lintcodes = intent.getIntegerArrayListExtra(LintHelper.class.getSimpleName());
        LintWidget wdLint = findViewById(R.id.wd_lint);
        wdLint.setLintTip(lintcodes);
    }
}
