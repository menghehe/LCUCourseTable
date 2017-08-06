package site.imcu.lcus;

import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bilibili.magicasakura.utils.ThemeUtils;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
        setToolBar();
    }
    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    public void initView() {
        LinearLayout jsoup = (LinearLayout) findViewById(R.id.jsoup);
        LinearLayout litepal = (LinearLayout) findViewById(R.id.litepal);
        LinearLayout md = (LinearLayout) findViewById(R.id.md);
        LinearLayout whell = (LinearLayout) findViewById(R.id.whell);
        LinearLayout square= (LinearLayout) findViewById(R.id.square);
        LinearLayout coursetable = (LinearLayout) findViewById(R.id.coursetable);
        ScrollView scroll_about = (ScrollView) findViewById(R.id.scroll_about);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        scroll_about.startAnimation(animation);

        jsoup.setOnClickListener(this);
        litepal.setOnClickListener(this);
        md.setOnClickListener(this);
        whell.setOnClickListener(this);
        square.setOnClickListener(this);
        coursetable.setOnClickListener(this);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setStartOffset(600);
    }
    @Override
    public void onClick(View view) {

        Intent intent = new Intent(Intent.ACTION_VIEW);

        switch (view.getId()) {
            case R.id.jsoup:
                intent.setData(Uri.parse("https://github.com/jhy/jsoup"));
                startActivity(intent);
                break;
            case R.id.litepal:
                intent.setData(Uri.parse("https://github.com/LitePalFramework/LitePal"));
                startActivity(intent);
                break;
            case R.id.md:
                intent.setData(Uri.parse("https://github.com/Eajy/MaterialDesignDemo"));
                startActivity(intent);
                break;
            case R.id.whell:
                intent.setData(Uri.parse("https://github.com/venshine/WheelView"));
                startActivity(intent);
                break;
            case R.id.square:
                intent.setData(Uri.parse("https://github.com/square/okhttp"));
                startActivity(intent);
                break;
            case R.id.coursetable:
                intent.setData(Uri.parse("https://github.com/square/okhttp"));
                startActivity(intent);
                break;
        }
    }
}
