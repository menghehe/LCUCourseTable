package site.imcu.lcus.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.bilibili.magicasakura.widgets.TintProgressDialog;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.utils.LoginUtils;

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
        setLoginListener();
        restorePass();
    }
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            enableLoginBtn();
        }
    };
    private void enableLoginBtn() {
        Button login = (Button)findViewById(R.id.login_btn);
        login.setEnabled(accountEdit.getText().length() == 10 && passwordEdit.getText().length() != 0);
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

                switch (session){
                    case "passwordError":
                        Toast.makeText(LoginActivity.this,"密码或者账号错误",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        break;
                    case "null":
                        Toast.makeText(LoginActivity.this,"登陆失败，重新登陆试试吧",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        break;
                    default:
                        Toast.makeText(LoginActivity.this,"登陆成功",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();
                        break;
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
        accountEdit.addTextChangedListener(textWatcher);
        passwordEdit.addTextChangedListener(textWatcher);
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