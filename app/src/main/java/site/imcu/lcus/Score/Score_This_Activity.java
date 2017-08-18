package site.imcu.lcus.Score;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

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
import site.imcu.lcus.Utils.LoginUtils;

public class Score_This_Activity extends AppCompatActivity {

    private static final String TAG = "Score_This_Activity";
    private String markUrl= "http://jwcweb.lcu.edu.cn/bxqcjcxAction.do";
    String session;
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
    }


    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getMark() {


        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {

            @Override

            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

                e.onNext(LoginUtils.getData(session,markUrl));//获取本学期成绩

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
        Element table = document.getElementById("user");
        Elements trs = table.select("tr");
        for(int i=1;i<trs.size();i++){
            Elements tds = trs.get(i).select("td");
            Score score = new Score();
            score.setCourseName(tds.get(2).text());
            score.setCredit(tds.get(4).text());
            score.setCourseAttr(tds.get(5).text());
            score.setMark(tds.get(9).text());
            score.setPosition(tds.get(10).text());
            scoreList.add(score);
        }
        setRecycle();
    }


    private void setRecycle(){

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.score_recycle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        ScoreAdapter adapter = new ScoreAdapter(scoreList);

        recyclerView.setAdapter(adapter);

    }
}
