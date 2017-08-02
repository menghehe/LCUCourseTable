package site.imcu.lcus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.AutoText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    public String s ;
    private static final int SUCCESS = 1;
    private static final int FALL = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getYzm();
        Toast.makeText(LoginActivity.this, "在教务系统获取课程成功后会清除当前课程", Toast.LENGTH_LONG).show();
        ImageView img = (ImageView)findViewById(R.id.yzm_view);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getYzm();
            }
        });
        Button login = (Button)findViewById(R.id.login_get);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSupport.deleteAll(ClassSchedule.class);
                getHtml();
            }
        });
    }

    /*处理html*/
    private Handler handlerhtml = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case SUCCESS:
                    Document document = Jsoup.parse(msg.obj.toString());
                    Element element = document.getElementById("user");
                    Elements trelement = element.select("tr");
                    List<ClassSchedule> course = new ArrayList<>();
                    int q=1;
                    for(int i=0;i<trelement.size();i++){
                        Elements tdelement = trelement.get(i).select("td");
                        if(i!=0 && i!=5 && i!=10) {
                            for(int j=0;j<7;j++){
                                String a = tdelement.get(tdelement.size()-1-j).text();
                                String []b= null;
                                ClassSchedule cs = new ClassSchedule();
                                if(a.length()>5){
                                    b=a.split("[_()]");
                                    cs.setName(b[0]);
                                    cs.setLocation(b[2]);
                                    cs.setWeek(7-j);
                                    cs.setOrder(q);
                                    cs.setSpan(2);
                                    cs.setFlag((int) (Math.random() * 10));
                                    course.add(cs);
                                }

                            }
                            i++;
                            q+=2;

                        }

                    }
                    DataSupport.saveAll(course);
                    Toast.makeText(LoginActivity.this, "获取成功,请返回主界面后下拉刷新", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case FALL:
                    Toast.makeText(LoginActivity.this, "哪里出错了", Toast.LENGTH_SHORT).show();
                    break;



            }
        }
    };
    private void getHtml(){
        final EditText zjh=(EditText) findViewById(R.id.tv_user_name);
        final EditText mm=(EditText)findViewById(R.id.tv_password);
        final EditText yzm=(EditText)findViewById(R.id.tv_yzm);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    RequestBody body = new FormBody.Builder()
                            .add("zjh",zjh.getText().toString())
                            .add("mm",mm.getText().toString())
                            .add("v_yzm",yzm.getText().toString())
                            .build();

                    Request request = new Request.Builder()
                            .url("http://jwcweb.lcu.edu.cn/loginAction.do")
                            .addHeader("cookie",s)
                            .post(body)
                            .build();
                    Response response  = client.newCall(request).execute();
                    Request request1 = new Request.Builder()
                            .url("http://jwcweb.lcu.edu.cn/xkAction.do?actionType=6")
                            .addHeader("cookie",s)
                            .build();
                    Response response1 = client.newCall(request1).execute();
                    String responseData = response1.body().string();
                    Document document = Jsoup.parse(responseData);
                    Element element = document.select("title").first();
                    Message message = handlerhtml.obtainMessage();
                    if (element.text().equals("学生选课结果")){
                        message.what=SUCCESS;
                        message.obj=(Object) responseData;
                    }
                    else {
                        message.what=FALL;
                    }
                    handlerhtml.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /*获取验证码*/
    private Handler handleryzm = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what) {
                case SUCCESS:
                    byte[] pic = (byte[]) msg.obj;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                    ImageView imageView =(ImageView)findViewById(R.id.yzm_view);
                    imageView.setImageBitmap(bitmap);
                    Toast.makeText(LoginActivity.this, "验证码获取成功", Toast.LENGTH_SHORT).show();
                    break;
                case FALL:
                    Toast.makeText(LoginActivity.this, "网络出现了问题", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private void getYzm(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://jwcweb.lcu.edu.cn/validateCodeAction.do?random=0.4340224302096043").build();
                    Response response = client.newCall(request).execute();
                    byte[] pic = response.body().bytes();
                    Message message = handleryzm.obtainMessage();
                    message.what=SUCCESS;
                    message.obj=pic;
                    handleryzm.sendMessage(message);
                    Headers headers = response.headers();
                    List<String> cookies = headers.values("Set-Cookie");
                    Log.d("info_cookies", "onResponse-size: " + cookies);
                    String session = cookies.get(0);
                    s = session.substring(0,session.indexOf(";"));
                    Log.i("info_s", "session is  :" + s);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
