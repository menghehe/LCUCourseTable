package site.imcu.lcus.score;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.activity.ScoreActivity;
import site.imcu.lcus.utils.LoginUtils;

public class Score_Detail_Activity extends AppCompatActivity {

    private static final String TAG = "Score_Detail_Activity";
    private String session;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score__detail_);
        Intent intent = getIntent();
        session= intent.getStringExtra("session");
        url=intent.getStringExtra("url");
        getMark();
    }

    private void getMark() {


        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {

            @Override

            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

                e.onNext(LoginUtils.getData(session,url));

            }

        });

        Consumer<String> consumer= new Consumer<String>() {

            @Override

            public void accept(String html) throws Exception {
                Log.d(TAG, "accept: "+html);
                WebView webView = (WebView)findViewById(R.id.score_detail);
                webView.setWebViewClient(new WebViewClient());
                webView.loadData(html,"text/html; charset=UTF-8", null);
            }

        };

        observable.subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(consumer);

    }
}
