package site.imcu.lcus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.weekNames)
    LinearLayout weekNames;

    @BindView(R.id.sections)
    LinearLayout sections;

    @BindView(R.id.mFreshLayout)
    MaterialRefreshLayout mFreshLayout;

    @BindViews({R.id.weekPanel_1, R.id.weekPanel_2, R.id.weekPanel_3, R.id.weekPanel_4, R.id.weekPanel_5})
    List<LinearLayout> mWeekViews;
    private int itemHeight;
    private int maxSection = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        itemHeight = getResources().getDimensionPixelSize(R.dimen.sectionHeight);
        initWeekNameView();
        initSectionView();
        initWeekCourseView();
        setRefreshListener();
        setAddListenner();

    }

    private void setAddListenner(){
        FloatingActionButton fab= (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddActivity.class);
                startActivity(intent);
            }
        });
    }
    /**
     * 顶部周一到周五的布局
     **/
    private void initWeekNameView() {
        for (int i = 0; i < mWeekViews.size()+1 ; i++) {
            TextView tvWeekName = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER | Gravity.CENTER_HORIZONTAL;
            if (i != 0) {
                lp.weight = 1.48f;
                tvWeekName.setText("周" + intToZH(i));
                if (i == getWeekDay()) {
                    tvWeekName.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    tvWeekName.setTextColor(Color.parseColor("#4A4A4A"));
                }
            } else {
                lp.weight = 0.4f;
                tvWeekName.setText(getMonth()+"月");
            }
            tvWeekName.setGravity(Gravity.CENTER_HORIZONTAL);
            tvWeekName.setLayoutParams(lp);
            weekNames.addView(tvWeekName);
        }
    }
    /**
     * 左边节次布局，设定每天最多10节课
     */
    private void initSectionView() {
        for (int i = 1; i <= maxSection; i++) {
            TextView tvSection = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getResources().getDimensionPixelSize(R.dimen.sectionHeight));
            lp.gravity = Gravity.CENTER;
            tvSection.setGravity(Gravity.CENTER);
            tvSection.setText(String.valueOf(i));
            tvSection.setLayoutParams(lp);
            sections.addView(tvSection);
        }
    }

    /**
     * 当前星期
     */
    public int getWeekDay() {
        int w = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        if (w <= 0) {
            w = 7;
        }
        return w;
    }

    /**
     * 初始化课程表
     */
    private void initWeekCourseView() {
        for (int i = 0; i < mWeekViews.size(); i++) {
            initWeekPanel(mWeekViews.get(i), CourseDao.getCourseData()[i]);
        }
    }
    /**
     * 下拉刷新
     */
    private void setRefreshListener() {
        mFreshLayout.setLoadMore(false);
        mFreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                clearChildView();
                initWeekCourseView();
                mFreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshLayout.finishRefreshing();
                        Toast.makeText(MainActivity.this,"我在这里",Toast.LENGTH_SHORT).show();
                    }
                }, 500);
            }

        });
    }
    private void clearChildView() {
        for (int i = 0; i < mWeekViews.size(); i++) {
            if (mWeekViews.get(i) != null)
                if (mWeekViews.get(i).getChildCount() > 0)
                    mWeekViews.get(i).removeAllViews();
        }
    }
    /**
     * 当前月份
     */
    public int getMonth() {
        int w = Calendar.getInstance().get(Calendar.MONTH) + 1;
        return w;
    }
    /**
     * 当前日期
     */
    public int getDay() {
        int w = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        return w;
    }
    /**
     * 数字转换中文
     */
    public static String intToZH(int i) {
        String[] zh = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String[] unit = {"", "十", "百", "千", "万", "十", "百", "千", "亿", "十"};

        String str = "";
        StringBuffer sb = new StringBuffer(String.valueOf(i));
        sb = sb.reverse();
        int r = 0;
        int l = 0;
        for (int j = 0; j < sb.length(); j++) {
            r = Integer.valueOf(sb.substring(j, j + 1));
            if (j != 0)
                l = Integer.valueOf(sb.substring(j - 1, j));
            if (j == 0) {
                if (r != 0 || sb.length() == 1)
                    str = zh[r];
                continue;
            }
            if (j == 1 || j == 2 || j == 3 || j == 5 || j == 6 || j == 7 || j == 9) {
                if (r != 0)
                    str = zh[r] + unit[j] + str;
                else if (l != 0)
                    str = zh[r] + str;
                continue;
            }
            if (j == 4 || j == 8) {
                str = unit[j] + str;
                if ((l != 0 && r == 0) || r != 0)
                    str = zh[r] + str;
                continue;
            }
        }
        if (str.equals("七"))
            str = "日";
        return str;
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
            case R.id.jwxt:
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.about:
                break;
        }
        return true;
    }
    public void initWeekPanel(LinearLayout ll, List<ClassSchedule> data) {

        if (ll == null || data == null || data.size() < 1)
            return;
        ClassSchedule firstCourse = data.get(0);
        for (int i = 0; i < data.size(); i++) {
            final ClassSchedule courseModel = data.get(i);

            if (courseModel.getOrder() == 0 || courseModel.getSpan() == 0)
                return;
            FrameLayout frameLayout = new FrameLayout(this);

            CornerTextView tv = new CornerTextView(this,
                    ColorUtils.getCourseBgColor(courseModel.getFlag()),
                    dip2px(this, 3));
            LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    itemHeight * courseModel.getSpan());
            LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            if (i == 0) {
                frameLp.setMargins(0, (courseModel.getOrder() - 1) * itemHeight, 0, 0);
            } else {
                frameLp.setMargins(0, (courseModel.getOrder() - (firstCourse.getOrder() + firstCourse.getSpan())) * itemHeight, 0, 0);
            }
            tv.setLayoutParams(tvLp);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(12);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setText(courseModel.getName() + "\n @" + courseModel.getLocation());

            frameLayout.setLayoutParams(frameLp);
            frameLayout.addView(tv);
            frameLayout.setPadding(2, 2, 2, 2);
            ll.addView(frameLayout);
            firstCourse = courseModel;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,EditActivity.class);
                    intent.putExtra("int_week",courseModel.getWeek());
                    intent.putExtra("int_order",courseModel.getOrder());
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}

