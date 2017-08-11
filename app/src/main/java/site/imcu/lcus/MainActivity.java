package site.imcu.lcus;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import site.imcu.lcus.Activity.AboutActivity;
import site.imcu.lcus.Activity.AddActivity;
import site.imcu.lcus.Activity.EditActivity;
import site.imcu.lcus.Activity.LeadinActivity;
import site.imcu.lcus.Activity.LoginActivity;
import site.imcu.lcus.Activity.ScoreActivity;
import site.imcu.lcus.Course.ClassSchedule;
import site.imcu.lcus.Course.ColorUtils;
import site.imcu.lcus.Course.CornerTextView;
import site.imcu.lcus.Course.CourseDao;
import site.imcu.lcus.Theme.CardPickerDialog;
import site.imcu.lcus.Theme.SnackAnimationUtil;
import site.imcu.lcus.Theme.ThemeHelper;


public class MainActivity extends AppCompatActivity implements CardPickerDialog.ClickListener{
    @BindView(R.id.weekNames)
    LinearLayout weekNames;

    @BindView(R.id.sections)
    LinearLayout sections;

    @BindView(R.id.mFreshLayout)
    MaterialRefreshLayout mFreshLayout;

    @BindViews({R.id.weekPanel_1, R.id.weekPanel_2, R.id.weekPanel_3, R.id.weekPanel_4, R.id.weekPanel_5})
    List<LinearLayout> mWeekViews;
    private int itemHeight;
    int maxSection = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        whenFirst();
        setToolBar();
        ButterKnife.bind(this);
        initWeekNameView();
        initSectionView();
        initWeekCourseView();
        setRefreshListener();
        setNavListener();

    }
    /**
     * 第一次启动处理
     */
    private void whenFirst(){
        copyFileOrDir("tessdata");
        SharedPreferences sharedPreferences=this.getSharedPreferences("share",MODE_PRIVATE);
        boolean isFirstRun=sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        if(isFirstRun){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            editor.putBoolean("isFirstRun", false);
            editor.apply();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url("http://iguxue.cn/Blog/?p=100").build();
                        Response response = client.newCall(request).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = "/data/data/" + this.getPackageName() + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    if (!dir.mkdir()){
                        return;
                    }
                for (String asset : assets) {
                    copyFileOrDir(path + "/" + asset);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + this.getPackageName() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }
    /**
     * 抽屉listener
     */
    private  void setNavListener(){
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = new Intent();
                switch (item.getItemId()) {
                    case R.id.nav_account:
                        intent.setClass(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_jwxt:
                        intent.setClass(MainActivity.this, LeadinActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_score:
                        intent.setClass(MainActivity.this, ScoreActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_add:
                        intent.setClass(MainActivity.this, AddActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_about:
                        intent.setClass(MainActivity.this, AboutActivity.class);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
    }
    /**
     * toolbar
     */
    private  void setToolBar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("聊大课表");
        setSupportActionBar(toolbar);
        initDrawerToggle();
    }
    /**
     * toolbars 左边导航
     */
    private void initDrawerToggle() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final DrawerLayout mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,0,0);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
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
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        return month;
    }
    /**
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

    public void initWeekPanel(LinearLayout ll, List<ClassSchedule> data) {

        itemHeight = getResources().getDimensionPixelSize(R.dimen.sectionHeight);

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

    /**
     * 哔哩哔哩主题设置
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.change_theme:
                CardPickerDialog dialog = new CardPickerDialog();
                dialog.setClickListener(this);
                dialog.show(getSupportFragmentManager(), CardPickerDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onConfirm(int currentTheme) {
        if (ThemeHelper.getTheme(MainActivity.this) != currentTheme) {
            ThemeHelper.setTheme(MainActivity.this, currentTheme);
            ThemeUtils.refreshUI(MainActivity.this, new ThemeUtils.ExtraRefreshable() {
                        @Override
                        public void refreshGlobal(Activity activity) {
                            //for global setting, just do once
                            if (Build.VERSION.SDK_INT >= 21) {
                                final MainActivity context = MainActivity.this;
                                ActivityManager.TaskDescription taskDescription =
                                        new ActivityManager.TaskDescription(null, null,
                                                ThemeUtils.getThemeAttrColor(context, android.R.attr.colorPrimary));
                                setTaskDescription(taskDescription);
                                getWindow().setStatusBarColor(
                                        ThemeUtils.getColorById(context, R.color.theme_color_primary_dark));
                            }
                        }

                        @Override
                        public void refreshSpecificView(View view) {
                        }
                    }
            );
            View view = findViewById(R.id.snack_layout);
            if (view != null) {
                TextView textView = (TextView) view.findViewById(R.id.content);
                textView.setText(getSnackContent(currentTheme));
                SnackAnimationUtil.with(this, R.anim.snack_in, R.anim.snack_out)
                        .setDismissDelayTime(1000)
                        .setTarget(view)
                        .play();
            }
        }
    }
    private String getSnackContent(int current) {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        return getResources().getString(getResources().getIdentifier(
                "magicasrkura_prompt_" + random.nextInt(3), "string", getPackageName())) + ThemeHelper.getName(current);
    }


}

