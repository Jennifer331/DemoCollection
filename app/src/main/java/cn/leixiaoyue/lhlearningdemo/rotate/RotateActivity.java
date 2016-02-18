package cn.leixiaoyue.lhlearningdemo.rotate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.leixiaoyue.lhlearningdemo.R;

/**
 *
 * Created by 80119424 on 2016/1/14.
 */
public class RotateActivity extends Activity{
    private RotateView mView = null;
    private Button mBitmapBtn,mMatrixBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotate);
        init();
    }

    private void init(){
        mView = (RotateView)findViewById(R.id.myview);
        mBitmapBtn = (Button)findViewById(R.id.bitmap);
        mMatrixBtn = (Button)findViewById(R.id.matrix);
        mView.setListener(new RotateView.MyViewListener() {
            @Override
            public void animationStarted() {
                mBitmapBtn.setClickable(false);
                mMatrixBtn.setClickable(false);
            }

            @Override
            public void animationEnded() {
                mBitmapBtn.setClickable(true);
                mMatrixBtn.setClickable(true);
            }
        });
    }

    public void rotateBitmap(View view){
        mView.nextState(RotateView.ROTATE_BITMAP);
    }

    public void rotateMatrix(View view){
        mView.nextState(RotateView.ROTATE_MATRIX);
    }
}
