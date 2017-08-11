package site.imcu.lcus.Activity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.imcu.lcus.R;

public class LoginActivity extends AppCompatActivity {

    public String session;
    private static final int SUCCESS = 1;
    private static final int FALL = 2;
    private SharedPreferences pref;
    private  SharedPreferences.Editor editor;
    private EditText accountEdit;
    private EditText passwordEdit;
    private EditText account;
    private EditText password;
    private EditText idcode;
    private Bitmap bitmap;
    private final static String URP = "urp";

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
        String account = pref.getString("account","");
        String password = pref.getString("password","");
        accountEdit.setText(account);
        passwordEdit.setText(password);
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
                    editor.putBoolean("isFirst",false);
                    editor.putString("account",account.getText().toString());
                    editor.putString("password",password.getText().toString());
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
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
                    bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                    ImageView imageView = (ImageView) findViewById(R.id.idcodeimg);
                    imageView.setImageBitmap(bitmap);

                    Log.d("LoginActivity", "handleMessage: "+getPackageName());
                    TessBaseAPI baseApi = new TessBaseAPI();

                    //初始化OCR的训练数据路径与语言
                    baseApi.init("/data/data/site.imcu.lcus/" , URP);
                    baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
                    Pix pix = ReadFile.readBitmap(bitmap);
                    pix = Binarize.sauvolaBinarizeTiled(pix);

                    Bitmap after = WriteFile.writeBitmap(pix);

                    baseApi.setImage(after);

                    String yzm = baseApi.getUTF8Text().replace(" ", "");
                    idcode = (EditText)findViewById(R.id.idcode);
                    idcode.setText(yzm);
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