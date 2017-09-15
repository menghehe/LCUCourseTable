package site.imcu.lcus.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.score.Score_All_Activity;
import site.imcu.lcus.score.Score_Fail_Activity;
import site.imcu.lcus.score.Score_This_Activity;
import site.imcu.lcus.utils.LoginUtils;

public class ScoreActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private SharedPreferences pref;
    String session = "null";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        setToolBar();
        login();
        login_btn();

    }


    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void setButton(){

        Button fail = (Button)findViewById(R.id.score_fail);
        fail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScoreActivity.this, Score_Fail_Activity.class);
                intent.putExtra("session",session);
                startActivity(intent);
            }
        });

        Button thisTerm = (Button)findViewById(R.id.score_this_term);
        thisTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScoreActivity.this, Score_This_Activity.class);
                intent.putExtra("session",session);
                startActivity(intent);
            }
        });

        Button all = (Button)findViewById(R.id.score_all);
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScoreActivity.this, Score_All_Activity.class);
                intent.putExtra("session",session);
                startActivity(intent);
            }
        });

    }

    private void login(){
        showProgress();
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
                switch (mSession){
                    case "passwordError":
                        Toast.makeText(ScoreActivity.this,"密码或者账号错误",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        break;
                    case "null":
                        Toast.makeText(ScoreActivity.this,"登陆失败，重新登陆试试吧",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        break;
                    default:
                        Toast.makeText(ScoreActivity.this,"登陆成功",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        session=mSession;
                        setButton();
                        break;
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
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void login_btn(){
        final Button login = (Button)findViewById(R.id.score_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

}
