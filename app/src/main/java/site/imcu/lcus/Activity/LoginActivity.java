package site.imcu.lcus.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.Utils.LoginUtils;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private EditText accountEdit;
    private EditText passwordEdit;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setToolBar();
        restorePass();
        setLoginListener();
    }

    private void setLoginListener(){
        Button login = (Button)findViewById(R.id.login_btn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                login();
            }
        });
    }
    private void login(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        rememberAccount();
        Observable <String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext(LoginUtils.login(pref.getString("account",""),pref.getString("password","")));
            }
        });
        Consumer<String> consumer= new Consumer<String>() {
            @Override
            public void accept(String session) throws Exception {
                if (!session.equals("null")){
                    Toast.makeText(LoginActivity.this,"登陆成功",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                }else {
                    Toast.makeText(LoginActivity.this,"登陆失败",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);

    }
    private void rememberAccount(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=pref.edit();
        accountEdit = (EditText)findViewById(R.id.username);
        passwordEdit=(EditText)findViewById(R.id.password);
        editor.putString("account",accountEdit.getText().toString());
        editor.putString("password",passwordEdit.getText().toString());
        editor.apply();
    }
    private void restorePass(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit = (EditText)findViewById(R.id.username);
        passwordEdit=(EditText)findViewById(R.id.password);
        String account = pref.getString("account","");
        String password = pref.getString("password","");
        accountEdit.setText(account);
        passwordEdit.setText(password);
    }
    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void showProgress(){
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("登陆中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}