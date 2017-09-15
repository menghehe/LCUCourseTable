package site.imcu.lcus.score;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

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

public class Score_Fail_Activity extends AppCompatActivity {

    private ScoreAdapter mScoreAdapter;
    private static final String TAG = "Score_This_Activity";
    private String markUrl= "http://jwcweb.lcu.edu.cn/gradeLnAllAction.do?type=ln&oper=bjg";
    String session;
    ProgressDialog progressDialog ;
    List<Score> scoreList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_m);
        setToolBar();
        Intent intent = getIntent();
        session= intent.getStringExtra("session");
        Log.d(TAG, "onCreate: "+session);
        getMark();
        show();
    }


    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getMark() {


        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {

            @Override

            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

                e.onNext(LoginUtils.getData(session,markUrl));//获取不及格成绩

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

    private void scoreDao(String html) {
        Document document = Jsoup.parse(html);
        Elements tables = document.getElementsByClass("displayTag");
        for(int i=0;i<tables.size();i++){
            Elements trs = tables.get(i).select("tr");
            for(int q=1;q<trs.size();q++){
              //  Log.d(TAG, "scoreDao: "+trs.get(q).text());
                Elements tds = trs.get(q).select("td");
                Log.d(TAG, "scoreDao: "+tds.text()+q);
                Score score = new Score();
                score.setCourseName(tds.get(2).text());
                score.setCredit(tds.get(4).text());
                score.setCourseAttr(tds.get(5).text());
                score.setMark(tds.get(6).text());
                score.setDetail("http://jwcweb.lcu.edu.cn/"+getSrc(tds.get(9)));
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

        mScoreAdapter= new ScoreAdapter(scoreList);

        recyclerView.setAdapter(mScoreAdapter);

        setListener();

    }

    private void show(){
        progressDialog = new ProgressDialog(Score_Fail_Activity.this);
        progressDialog.setTitle("不及格成绩");
        progressDialog.setMessage("加载中");
        progressDialog.setProgressStyle(TintProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setListener(){
        mScoreAdapter.setOnItemClickListener(new ScoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(Score_Fail_Activity.this, Score_Detail_Activity.class);
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
