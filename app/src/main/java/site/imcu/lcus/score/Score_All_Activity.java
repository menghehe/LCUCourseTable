package site.imcu.lcus.score;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.view.View;
import android.widget.Toast;

import com.bilibili.magicasakura.widgets.TintProgressDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.utils.LoginUtils;

public class Score_All_Activity extends AppCompatActivity {

    private ScoreAdapter mScoreAdapter;
    private static final String TAG = "Score_This_Activity";
    private String fakeUrl="http://jwcweb.lcu.edu.cn/gradeLnAllAction.do?type=ln&oper=qb";
    private String markUrl;
    String session;
    ProgressDialog progressDialog;
    List<Score> scoreList = new ArrayList<>();
    List<Score> pointList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_m);
        setToolBar();
        Intent intent = getIntent();
        session= intent.getStringExtra("session");
        Log.d(TAG, "onCreate: "+session);
        getMarkSrc();
        show();
    }


    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getMarkSrc() {


        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {

            @Override

            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

                e.onNext(LoginUtils.getData(session,fakeUrl));

            }

        });

        Consumer<String> consumer= new Consumer<String>() {

            @Override

            public void accept(String html) throws Exception {
                String src = getRealUr(html);
                Log.d(TAG, "accept: "+src);
                markUrl = "http://jwcweb.lcu.edu.cn/"+src;
                Log.d(TAG, "accept: "+markUrl);
                getMark();
            }

        };

        observable.subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(consumer);

    }
    private void getMark() {


        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {

            @Override

            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

                e.onNext(LoginUtils.getData(session,markUrl));

            }

        });

        Consumer<String> consumer= new Consumer<String>() {

            @Override

            public void accept(String html) throws Exception {
             scoreDao(html);
            }

        };

        observable.subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(consumer);

    }

    private String getRealUr (String html){
        Document document = Jsoup.parse(html);
        Elements iFrames = document.getElementsByTag("iframe");
        Log.d(TAG, "getRealUr: "+iFrames.size());
        return iFrames.attr("src");
    }

    private void scoreDao(String html) {
        Document document = Jsoup.parse(html);
        Elements tables = document.getElementsByClass("displayTag");
        for(int i=0;i<tables.size();i++){
            Elements trs = tables.get(i).select("tr");
            for(int q=1;q<trs.size();q++){
               Elements tds = trs.get(q).select("td");
                Score score = new Score();
                score.setCourseName(tds.get(2).text());
                score.setCredit(tds.get(4).text());
                score.setCourseAttr(tds.get(5).text());
                score.setMark(tds.get(6).text().replace(" ",""));
                score.setDetail("http://jwcweb.lcu.edu.cn"+getSrc(tds.get(7)));
                Log.d(TAG, "scoreDao: "+score.getDetail());
                scoreList.add(score);
            }
        }
        setRecycle();
        progressDialog.dismiss();
    }


    private void setRecycle(){

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.score_recycle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        mScoreAdapter = new ScoreAdapter(scoreList);

        recyclerView.setAdapter(mScoreAdapter);

        setListener();
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
        double credit;
        double creditSum=0;
        double point;
        double pointSum=0;
        pointList = filter();

            for (int i=0;i<pointList.size();i++){
                point=gradeToPoint(pointList.get(i));
                credit=stringToDouble(pointList.get(i).getCredit());
                creditSum+=credit;
                pointSum+=point*credit;
            }
        Log.d(TAG, "showGradePoint: "+pointSum);
        AlertDialog.Builder dialog = new AlertDialog.Builder(Score_All_Activity.this);
        dialog.setTitle("总学分:"+creditSum);
        double tmp=pointSum/creditSum;
        dialog.setMessage("平均绩点"+tmp+"\n看看就好，反正不准");
        dialog.show();
    }
    private List<Score> filter(){
        for(int i=0;i<scoreList.size();i++){
            if(scoreList.get(i).getCourseAttr().equals("必修")){
                Score score;
                score=scoreList.get(i);
                pointList.add(score);
            }

        }
        return pointList;
    }
    private double gradeToPoint(Score score){
        double point=0;
        Log.d(TAG, "gradeToPoint: "+score.getMark());
        switch (score.getMark()){
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
                point=perSys(stringToDouble(score.getMark()));
        }
        Log.d(TAG, "gradeToPoint: "+point);
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
    private void show(){
        progressDialog = new ProgressDialog(Score_All_Activity.this);
        progressDialog.setTitle("全部成绩成绩");
        progressDialog.setMessage("加载中");
        progressDialog.setProgressStyle(TintProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setListener(){
        mScoreAdapter.setOnItemClickListener(new ScoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(Score_All_Activity.this, Score_Detail_Activity.class);
                intent.putExtra("session",session);
                intent.putExtra("url",scoreList.get(position).getDetail());
                startActivity(intent);
            }
        });
    }

    private String getSrc(Element element){
        Elements src = element.select("img");
        String onclick = src.attr("onclick");
        String []a=onclick.split("[']");
        return a[1];
    }
}
