package site.imcu.lcus.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.Score.Score_This_Activity;
import site.imcu.lcus.Utils.LoginUtils;

public class ScoreActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private SharedPreferences pref;
    String session = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        setToolBar();
        showProgress();
        login();
        setButton();
    }


    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void setButton(){
        Button thisTerm = (Button)findViewById(R.id.score_this_term);
        thisTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScoreActivity.this, Score_This_Activity.class);
                intent.putExtra("session",session);
                startActivity(intent);
            }
        });
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
                    progressDialog.dismiss();
                }else {
                    login();
                }
            }
        };
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }



    private void showProgress(){
        progressDialog = new ProgressDialog(ScoreActivity.this);
        progressDialog.setTitle("登陆中");
        progressDialog.setMessage("请稍等");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

}
