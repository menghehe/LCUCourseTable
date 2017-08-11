package site.imcu.lcus.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
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

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.imcu.lcus.R;
import site.imcu.lcus.Score.Score;
import site.imcu.lcus.Score.ScoreAdapter;

public class ScoreActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private SharedPreferences pref;
    String session = null;
    private static final int SUCCESS = 1;
    private static final int FALL = 2;
    private String gradeAll = "http://jwcweb.lcu.edu.cn/gradeLnAllAction.do?type=ln&oper=qb";
    private String gradeFail="http://jwcweb.lcu.edu.cn/gradeLnAllAction.do?type=ln&oper=bjg";
    private String gradereal=null;
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


    private Handler handlerGradeAll = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Document document = Jsoup.parse(msg.obj.toString());
                    scoreDaoAll(document);
                    break;
                case FALL:
                    Toast.makeText(ScoreActivity.this, "哪里出错了", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private void getGradeFail(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder().build();
                    Request request1 = new Request.Builder()
                            .url(gradeFail)
                            .addHeader("cookie", session)
                            .build();
                    Response response = client.newCall(request1).execute();
                    String responseData = response.body().string();
                    Log.d("ScoreActivity", "run: "+responseData);
                    Document document = Jsoup.parse(responseData);
                    Message message = handlerFail.obtainMessage();
                    message.what = SUCCESS;
                    message.obj = (Object) responseData;
                    handlerFail.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private Handler handlerFail = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Document document = Jsoup.parse(msg.obj.toString());
                    scoreDaoFail(document);
                    Log.d("ScoreActivity", "handleMessage: "+document.text());
                    break;
                case FALL:
                    Toast.makeText(ScoreActivity.this, "哪里出错了", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private void getGradereal(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder().build();
                    Request request1 = new Request.Builder()
                            .url(gradereal)
                            .addHeader("cookie", session)
                            .build();
                    Response response1 = client.newCall(request1).execute();
                    String responseData = response1.body().string();
                    Log.d("ScoreActivity", "run: "+responseData);
                    Document document = Jsoup.parse(responseData);
                    Message message = handlerGradeAll.obtainMessage();
                        message.what = SUCCESS;
                        message.obj = (Object) responseData;
                    handlerGradeAll.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Handler handlerEntrance = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Document document = Jsoup.parse(msg.obj.toString());
                    Elements links = document.select("a[href]");
                    gradereal = gradeAll+"info&lnxndm="+links.get(4).text();
                    Log.d("ScoreActivity", "handleMessage: "+gradereal);
                   getGradereal();
                    break;
                case FALL:
                    Toast.makeText(ScoreActivity.this, "哪里出错了", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void getEntrance() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder().build();
                    Request request1 = new Request.Builder()
                            .url(gradeAll)
                            .addHeader("cookie", session)
                            .build();
                    Response response1 = client.newCall(request1).execute();
                    String responseData = response1.body().string();
                    Document document = Jsoup.parse(responseData);
                    Element element = document.select("title").first();
                    Message message = handlerEntrance.obtainMessage();
                    if (element.text().equals("成绩查询")) {
                        message.what = SUCCESS;
                        message.obj = (Object) responseData;
                    } else {
                        message.what = FALL;
                    }
                    handlerEntrance.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void scoreDaoAll(Document document){
        Elements links = document.getElementsByTag("a");
        Elements tables = document.getElementsByClass("displayTag");
        for(int i=0;i<tables.size();i++){
            Elements trs = tables.get(i).select("tr");
            for (int q=trs.size()-1;q>0;q--){
                Elements tds=trs.get(q).select("td");
                Score score=new Score();
                score.setKch(tds.get(0).text());
                score.setKxh(tds.get(1).text());
                score.setKcm(tds.get(2).text());
                score.setYwkcm(tds.get(3).text());
                score.setXf(tds.get(4).text());
                score.setKcxs(tds.get(5).text());
                score.setCj(tds.get(6).text().replace(" ",""));
                score.setCjmx(tds.get(7).text());
                score.setSbjg(false);
                score.setCbjg(false);
                Log.d("ScoreActivity",score.getKcm().toString());
                scoreList.add(score);
            }
        }
        getGradeFail();
        Log.d("ScoreActivity", "scoreDao: "+scoreList.size());
    }
    private void scoreDaoFail(Document document){
        Elements tables = document.getElementsByClass("displayTag");
        for(int i=0;i<tables.size();i++){
            Elements trs = tables.get(i).select("tr");
            for (int q=trs.size()-1;q>0;q--){
                Elements tds=trs.get(q).select("td");
                Score score=new Score();
                score.setKch(tds.get(0).text());
                score.setKxh(tds.get(1).text());
                score.setKcm(tds.get(2).text());
                score.setYwkcm(tds.get(3).text());
                score.setXf(tds.get(4).text());
                score.setKcxs(tds.get(5).text());
                score.setCj(tds.get(6).text());
                score.setCjmx(tds.get(7).text());
                if (i==0){
                    score.setSbjg(true);
                    score.setCbjg(false);
                }
                if (i==1){
                    score.setCbjg(true);
                    score.setSbjg(false);
                }
                scoreList.add(score);
            }
        }
        setrecycle();
    }
    private void setrecycle(){
        Collections.reverse(scoreList);
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.score_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ScoreAdapter adapter = new ScoreAdapter(scoreList);
        recyclerView.setAdapter(adapter);
        progressDialog.dismiss();
    }
    private void login() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request requestyzm = new Request.Builder().url("http://jwcweb.lcu.edu.cn/validateCodeAction.do?random").build();
                    Response response = client.newCall(requestyzm).execute();
                    byte[] pic = response.body().bytes();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);

                    TessBaseAPI baseApi = new TessBaseAPI();

                    //初始化OCR的训练数据路径与语言
                    baseApi.init("/data/data/site.imcu.lcus/" , "urp");
                    baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
                    Pix pix = ReadFile.readBitmap(bitmap);
                    pix = Binarize.sauvolaBinarizeTiled(pix);

                    Bitmap after = WriteFile.writeBitmap(pix);

                    baseApi.setImage(after);

                    String yzm = baseApi.getUTF8Text().replace(" ", "");
                    Headers headers = response.headers();
                    List<String> cookies = headers.values("Set-Cookie");
                    Log.d("info_cookies", "onResponse-size: " + cookies);
                    session = cookies.get(0);
                    session = session.substring(0, session.indexOf(";"));
                    RequestBody body = new FormBody.Builder()
                            .add("zjh",pref.getString("account",""))
                            .add("mm", pref.getString("password",""))
                            .add("v_yzm",yzm )
                            .build();
                    Request request = new Request.Builder()
                            .url("http://jwcweb.lcu.edu.cn/loginAction.do")
                            .addHeader("cookie", session)
                            .post(body)
                            .build();
                    Response responselogin = client.newCall(request).execute();
                    String responseData = responselogin.body().string();
                    Document document = Jsoup.parse(responseData);
                    Element element = document.select("title").first();
                    Log.d("ScoreActivity", "run: "+element.text());
                    Message message = handlerData.obtainMessage();
                    if (element.text().equals("学分制综合教务")) {
                        message.what = SUCCESS;
                        message.obj=session;
                    }else {
                        message.what = FALL;
                        login();
                    }handlerData.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private Handler handlerData = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    getEntrance();
                    break;
                case FALL:
                    time++;
                    if(time==3){
                        Toast.makeText(ScoreActivity.this,"获取失败,请检查账号设置",Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };
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
                point=Cgrade(pointList.get(i));
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
    private double Cgrade(Score score){
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
