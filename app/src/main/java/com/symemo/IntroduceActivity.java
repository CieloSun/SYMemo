package com.symemo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import java.util.Timer;
import java.util.TimerTask;

public class IntroduceActivity extends Activity {
    //Button ib;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_introduce);
        final Timer timer = new Timer();
        TimerTask tt=new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(IntroduceActivity.this,MainActivity.class));
                finish();
                timer.cancel();
            }
        };
        timer.schedule(tt, 1000);//可更改Introduce界面显示时间，单位微秒
        //可改为按键触发。
        /*ib=(Button)findViewById(R.id.introduce_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });*/
    }
}
