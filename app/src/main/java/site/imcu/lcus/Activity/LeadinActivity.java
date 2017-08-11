package site.imcu.lcus.Activity;

import android.content.Intent;
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
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.imcu.lcus.Course.ClassSchedule;
import site.imcu.lcus.R;

public class LeadinActivity extends AppCompatActivity {

    String session = null;
    private SharedPreferences pref;
    private static final int SUCCESS = 1;
    private static final int FALL = 2;
    private int time=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leadin);
        setToolBar();
        login();
        setLeadinListener();
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                    break;
                case FALL:
                    time++;
                    if(time==3){
                        Toast.makeText(LeadinActivity.this,"获取失败,请检查账号设置",Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };

    private void setLeadinListener(){
        final Button leadin = (Button)findViewById(R.id.leadin);
        leadin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session!=null){
                    DataSupport.deleteAll(ClassSchedule.class);
                    getData();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK){
                    session = data.getStringExtra("session");
                }
                break;
            default:
                break;
        }

        }

    private Handler handlerdata = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Document document = Jsoup.parse(msg.obj.toString());
                    Element element = document.getElementById("user");
                    Elements trelement = element.select("tr");
                    List<ClassSchedule> course = new ArrayList<>();
                    int q = 1;
                    for (int i = 0; i < trelement.size(); i++) {
                        Elements tdelement = trelement.get(i).select("td");
                        if (i != 0 && i != 5 && i != 10) {
                            for (int j = 0; j < 7; j++) {
                                String a = tdelement.get(tdelement.size() - 1 - j).text();
                                String[] b = null;
                                ClassSchedule cs = new ClassSchedule();
                                if (a.length() > 5) {
                                    b = a.split("[_()]");
                                    cs.setName(b[0].replace("  ",""));
                                    cs.setLocation(b[2]);
                                    cs.setWeek(7 - j);
                                    cs.setOrder(q);
                                    cs.setSpan(2);
                                    cs.setFlag((int) (Math.random() * 10));
                                    course.add(cs);
                                }
                            }
                            i++;
                            q += 2;

                        }
                    }
                    DataSupport.saveAll(course);
                    Toast.makeText(LeadinActivity.this, "获取成功,请返回主界面后下拉刷新", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case FALL:
                    Toast.makeText(LeadinActivity.this, "哪里出错了", Toast.LENGTH_SHORT).show();
                    break;


            }
        }
    };

    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder().build();
                    Request request1 = new Request.Builder()
                            .url("http://jwcweb.lcu.edu.cn/xkAction.do?actionType=6")
                            .addHeader("cookie", session)
                            .build();
                    Response response1 = client.newCall(request1).execute();
                    String responseData = response1.body().string();
                    Document document = Jsoup.parse(responseData);
                    Element element = document.select("title").first();
                    Message message = handlerdata.obtainMessage();
                    if (element.text().equals("学生选课结果")) {
                        message.what = SUCCESS;
                        message.obj = (Object) responseData;
                    } else {
                        message.what = FALL;
                    }
                    handlerdata.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}