package site.imcu.lcus.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.Score.Score;
import site.imcu.lcus.Score.ScoreAdapter;
import site.imcu.lcus.Utils.LoginUtils;

public class ScoreActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private SharedPreferences pref;
    String session = null;
    private String gradeAllUrl = "http://jwcweb.lcu.edu.cn/gradeLnAllAction.do?type=ln&oper=qb";
    private String gradeFailUrl="http://jwcweb.lcu.edu.cn/gradeLnAllAction.do?type=ln&oper=bjg";
    private String gradeRealUrl=null;
    private List<Score> scoreList = new ArrayList<>();
    private int time=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        setToolBar();
        showProgress();
        login();

    }

    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void login(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext(LoginUtils.login(pref.getString("account",""),pref.getString("password","")));
            }
        });
        Consumer<String> consumer= new Consumer<String>() {
            @Override
            public void accept(String mSession) throws Exception {
                if (!mSession.equals("null")){
                    session=mSession;
                    getGradeRealUrl();
                }else {
                    login();
                }
            }
        };
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    private void getGradeRealUrl(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext(LoginUtils.getData(session,gradeAllUrl));
            }
        });
        Consumer<String> consumer= new Consumer<String>() {
            @Override
            public void accept(String html) throws Exception {
                if (html.equals("null")){
                    Toast.makeText(ScoreActivity.this,"哪里出错了",Toast.LENGTH_SHORT).show();
                }else {

                    Document document = Jsoup.parse(html);

                    Elements links = document.select("a[href]");

                    gradeRealUrl = gradeAllUrl+"info&lnxndm="+links.get(4).text();

                    getGradeAll();
                }
            }
        };
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    private void getGradeAll(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext(LoginUtils.getData(session,gradeRealUrl));//获取全部成绩
                e.onNext(LoginUtils.getData(session,gradeFailUrl));//获取不及格成绩
                e.onNext("over");
            }
        });
        Consumer<String> consumer= new Consumer<String>() {
            @Override
            public void accept(String html) throws Exception {
                if(html.equals("over")){
                    setRecycle();
                    progressDialog.dismiss();
                }
                scoreDaoAll(html);
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }


    private void scoreDaoAll(String html){
        try {
        time++;
        Document document = Jsoup.parse(html);
        Elements tables = document.getElementsByClass("displayTag");
        for(int i=0;i<tables.size();i++) {
            Elements trs = tables.get(i).select("tr");
            for (int q = trs.size() - 1; q > 0; q--) {
                Elements tds = trs.get(q).select("td");
                Score score = new Score();
                score.setKch(tds.get(0).text());
                score.setKxh(tds.get(1).text());
                score.setKcm(tds.get(2).text());
                score.setYwkcm(tds.get(3).text());
                score.setXf(tds.get(4).text());
                score.setKcxs(tds.get(5).text());
                score.setCj(tds.get(6).text().replace(" ", ""));
                score.setCjmx(tds.get(7).toString());
                if(time==2){
                    if(i==0){
                        score.setSbjg(true);
                        score.setCbjg(false);
                    }
                    else {
                        score.setSbjg(false);
                        score.setCbjg(true);
                    }
                }else {
                    score.setSbjg(false);
                    score.setCbjg(false);
                }
                scoreList.add(score);
            }
        }
        }catch (Exception e){
            Toast.makeText(ScoreActivity.this,"哪里出错了",Toast.LENGTH_SHORT).show();
        }
    }
    private void setRecycle(){
        Collections.reverse(scoreList);
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.score_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ScoreAdapter adapter = new ScoreAdapter(scoreList);
        recyclerView.setAdapter(adapter);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.score_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.grade_point:
                showGradePoint();
                break;
            case R.id.regular:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showGradePoint(){
        List<Score> pointList = new ArrayList<>();
        double xf;
        double xfSum=0;
        double point=0;
        double pointSum=0;
        if (scoreList.size()==0){
            Toast.makeText(ScoreActivity.this,"还没刷新出成绩",Toast.LENGTH_SHORT).show();
        }else {
            pointList=filter();
            for (int i=0;i<pointList.size();i++){
                point=getPoint(pointList.get(i));
                xf=stringToDouble(pointList.get(i).getXf());
                xfSum+=xf;
                pointSum+=point*xf;
                Log.d("ScoreActivity", "showGradePoint: "+point);
            }

        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(ScoreActivity.this);
        dialog.setTitle("总学分:"+xfSum);
        double tmp=pointSum/xfSum;
        dialog.setMessage("平均绩点"+tmp+"\n看看就好，反正不准");
        dialog.show();
    }
    private List<Score> filter(){
         List<Score> pointList = new ArrayList<>();
        for(int i=0;i<scoreList.size();i++){
            if(!scoreList.get(i).getSbjg()&&!scoreList.get(i).getCbjg()){
                if(scoreList.get(i).getKcxs().equals("必修")){
                    Score score;
                    score=scoreList.get(i);
                    pointList.add(score);
                }
            }
        }
        return pointList;
    }
    private double getPoint(Score score){
        double point=0;
        switch (score.getCj()){
            case "优秀":
                point=4.5;
                break;
            case "良好":
                point=3.5;
                break;
            case "中等":
                point=2.5;
                break;
            case "及格":
                point=1.5;
                break;
            default:
                point=perSys(stringToDouble(score.getCj()));
        }
        return point;
    }
    private double perSys(Double cj){
        double point=1;
        if (cj>=95){
            return 5.0;
        }else {
            for (int i = 60; i < cj.intValue(); i++) {
                point += 0.1;
            }
            return point;
        }
    }
    private double stringToDouble(String num){
        if(TextUtils.isEmpty(num)){
            return 0;
        }
        try{
            return Double.parseDouble(num);
        }catch (Exception e){
            return 0;
        }
    }
    private void showProgress(){
        progressDialog = new ProgressDialog(ScoreActivity.this);
        progressDialog.setTitle("成绩查询");
        progressDialog.setMessage("加载中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
}
