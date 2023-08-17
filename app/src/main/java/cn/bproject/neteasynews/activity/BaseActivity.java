package cn.bproject.neteasynews.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 *如果用户点击了返回按钮，调用 finish() 方法来关闭当前活动（Activity），使其返回上一个活动
 */

public class BaseActivity extends AppCompatActivity{

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
