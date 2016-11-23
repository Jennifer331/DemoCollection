package cn.leixiaoyue.lhlearningdemo.actionbaranimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import cn.leixiaoyue.lhlearningdemo.R;


/**
 * Created by 80119424 on 2016/11/22.
 */

public class ActionBarAnimActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("+添加标题");
        initTitleOnClickeListener();
    }

    private void initTitleOnClickeListener() {
        final View  titleView = getActionBarView();
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomView((int)titleView.getX());
            }
        });
    }

    private View getActionBarView() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        int resId = getResources().getIdentifier("action_bar_title", "id", "android");
        return decorView.findViewById(resId);
    }

    private void showCustomView(int animationStartPos) {
        ActionBar actionBar = getActionBar();
        CharSequence title = actionBar.getTitle();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.input_view_withstub);
        actionBar.setDisplayShowCustomEnabled(true);

        EditText inputEdt = (EditText) findViewById(R.id.input);
        inputEdt.setHint(title);

        Button cancelBtn = (Button) findViewById(R.id.cancel);
        initCancelListener(cancelBtn);

        startAnimation(animationStartPos);
    }

    private void initCancelListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionBar actionBar = getActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayShowCustomEnabled(false);
            }
        });
    }

    private void startAnimation(int startX) {
        ValueAnimator valueAnimator = ValueAnimator
                .ofInt(startX, 0)
                .setDuration(500);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                View stub = findViewById(R.id.stub);
                int currentValue = (int)valueAnimator.getAnimatedValue();
                stub.setLayoutParams(new LinearLayout.LayoutParams((int)currentValue, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                EditText editText = (EditText) findViewById(R.id.input);
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
    }
}
