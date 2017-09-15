package site.imcu.lcus.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import site.imcu.lcus.R;

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

        TextView gitAddress = (TextView) findViewById(R.id.my_git);
        LinearLayout tess = (LinearLayout) findViewById(R.id.tess);
        LinearLayout urp= (LinearLayout) findViewById(R.id.urp);

        LinearLayout js = (LinearLayout) findViewById(R.id.jsoup);
        LinearLayout SQL = (LinearLayout) findViewById(R.id.litepal);
        LinearLayout md = (LinearLayout) findViewById(R.id.md);
        LinearLayout wheel = (LinearLayout) findViewById(R.id.whell);
        LinearLayout square= (LinearLayout) findViewById(R.id.square);
        LinearLayout courseTable = (LinearLayout) findViewById(R.id.coursetable);
        ScrollView scroll_about = (ScrollView) findViewById(R.id.scroll_about);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        scroll_about.startAnimation(animation);

        js.setOnClickListener(this);
        SQL.setOnClickListener(this);
        md.setOnClickListener(this);
        wheel.setOnClickListener(this);
        square.setOnClickListener(this);
        courseTable.setOnClickListener(this);
        urp.setOnClickListener(this);
        tess.setOnClickListener(this);
        gitAddress.setOnClickListener(this);

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
            case R.id.urp:
                intent.setData(Uri.parse("https://github.com/Risid/URP"));
                startActivity(intent);
                break;
            case R.id.tess:
                intent.setData(Uri.parse("https://github.com/rmtheis/tess-two"));
                startActivity(intent);
                break;
            case R.id.my_git:
                intent.setData(Uri.parse("https://github.com/SHIELD7/LcuCourseTable"));
                startActivity(intent);
                break;

        }
    }
}
