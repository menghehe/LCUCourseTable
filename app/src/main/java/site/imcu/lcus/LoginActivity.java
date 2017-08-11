package site.imcu.lcus;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.AutoText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bilibili.magicasakura.utils.ThemeUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public String session;
    private static final int SUCCESS = 1;
    private static final int FALL = 2;
    private SharedPreferences pref;
    private  SharedPreferences.Editor editor;
    private CheckBox rememberPass;
    private EditText accountEdit;
    private EditText passwordEdit;
    private EditText account;
    private EditText password;
    private EditText idcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setToolBar();
        getIdcode();
        restorePass();
        ImageView img = (ImageView) findViewById(R.id.idcodeimg);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getIdcode();
            }
        });
        Button login = (Button) findViewById(R.id.login_btn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void restorePass(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit = (EditText)findViewById(R.id.username);
        passwordEdit=(EditText)findViewById(R.id.password);
        rememberPass=(CheckBox) findViewById(R.id.remember_pass);
        boolean isremember = pref.getBoolean("remember_password",false);
        if (isremember){
            String account = pref.getString("account","");
            String password = pref.getString("password","");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }
    }
    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private Handler handlerData = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    editor = pref.edit();
                    if(rememberPass.isChecked()){
                        editor.putBoolean("remember_password",true);
                        editor.putString("account",account.getText().toString());
                        editor.putString("password",password.getText().toString());
                    }
                    else {
                        editor.clear();
                    }
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("session",session);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case FALL:
                    Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();

            }
        }
    };

    private void login() {
        account = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        idcode = (EditText) findViewById(R.id.idcode);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    RequestBody body = new FormBody.Builder()
                            .add("zjh", account.getText().toString())
                            .add("mm", password.getText().toString())
                            .add("v_yzm", idcode.getText().toString())
                            .build();
                    Request request = new Request.Builder()
                            .url("http://jwcweb.lcu.edu.cn/loginAction.do")
                            .addHeader("cookie", session)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    Request request1 = new Request.Builder()
                            .url("http://jwcweb.lcu.edu.cn/xkAction.do?actionType=6")
                            .addHeader("cookie", session)
                            .build();
                    Response response1 = client.newCall(request1).execute();
                    String responseData = response1.body().string();
                    Document document = Jsoup.parse(responseData);
                    Element element = document.select("title").first();
                    Message message = handlerData.obtainMessage();
                    if (element.text().equals("学生选课结果")) {
                        message.what = SUCCESS;
                        message.obj=session;
                    }else {
                        message.what=FALL;
                    }
                    handlerData.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*获取验证码*/
    private Handler handlerIdcode = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    byte[] pic = (byte[]) msg.obj;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                    ImageView imageView = (ImageView) findViewById(R.id.idcodeimg);
                    imageView.setImageBitmap(bitmap);
                    break;
                case FALL:
                    Toast.makeText(LoginActivity.this, "网络出现了问题", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void getIdcode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://jwcweb.lcu.edu.cn/validateCodeAction.do?random").build();
                    Response response = client.newCall(request).execute();
                    byte[] pic = response.body().bytes();
                    Message message = handlerIdcode.obtainMessage();
                    message.what = SUCCESS;
                    message.obj = pic;
                    handlerIdcode.sendMessage(message);
                    Headers headers = response.headers();
                    List<String> cookies = headers.values("Set-Cookie");
                    Log.d("info_cookies", "onResponse-size: " + cookies);
                    session = cookies.get(0);
                    session = session.substring(0, session.indexOf(";"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}