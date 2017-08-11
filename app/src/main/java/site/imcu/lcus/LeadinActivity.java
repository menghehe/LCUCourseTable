package site.imcu.lcus;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LeadinActivity extends AppCompatActivity {

    String session = null;
    private static final int SUCCESS = 1;
    private static final int FALL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leadin);
        setToolBar();
        setStartLoginListener();
        setLeadinListener();
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setStartLoginListener() {

        final Button startLogin = (Button) findViewById(R.id.start_login);
        startLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeadinActivity.this, LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(LeadinActivity.this,"没有登陆",Toast.LENGTH_SHORT).show();
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
                                    cs.setName(b[0]);
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