package site.imcu.lcus.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.Course.ClassSchedule;
import site.imcu.lcus.R;
import site.imcu.lcus.Utils.LoginUtils;

public class LeadActivity extends AppCompatActivity {
    private static final String TAG = "LeadActivity";
    private ProgressDialog progressDialog;
    String session = null;
    String tableUrl = "http://jwcweb.lcu.edu.cn/xkAction.do?actionType=6";
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leadin);
        setToolBar();
        setListener();
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void setListener(){
        Button button = (Button)findViewById(R.id.leadin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                Log.d(TAG, "onClick: ");
                login();
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
                    getTable();
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(LeadActivity.this, "登陆失败,请重试", Toast.LENGTH_SHORT).show();
                }
            }
        };
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);

    }
    private void getTable(){
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext(LoginUtils.getData(session,tableUrl));
            }
        });
        Consumer<String> consumer= new Consumer<String>() {
            @Override
            public void accept(String table) throws Exception {
                Document document = Jsoup.parse(table);
                Element element = document.select("title").first();
                if (element.text().equals("学生选课结果")) {
                        initData(table);
                } else {
                    login();
                }
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);

    }
    private void initData(String html){
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByClass("displayTag");
        Elements tBody = elements.get(1).select("tbody");
        Elements trElement = tBody.get(0).select("tr");
        Log.d(TAG, "initData1: ");
        List<ClassSchedule> course = new ArrayList<>();
        for (int i=0;i<trElement.size();i++){
            Elements tdElement = trElement.get(i).select("td");
            ClassSchedule cs = new ClassSchedule();

            if(tdElement.size()==18&&!tdElement.get(13).text().equals(" ")){
                cs.setName(tdElement.get(2).text());
                cs.setTeacher(tdElement.get(7).text());
                cs.setWeekList(tdElement.get(11).text());
                cs.setWeek(strToInt(tdElement.get(12).text()));
                cs.setOrder(strToInt(tdElement.get(13).text()));
                cs.setSpan(strToInt(tdElement.get(14).text()));
                cs.setLocation(tdElement.get(17).text());
                cs.setFlag((int) (Math.random() * 10));
                Log.d(TAG, "initData:"+cs.getWeekList());
                course.add(cs);
            }

            if(tdElement.size()!=18&&!tdElement.get(0).text().equals(" ")){
                cs.setName(course.get(course.size()-1).getName());
                cs.setTeacher(course.get(course.size()-1).getTeacher());
                cs.setWeekList(tdElement.get(0).text());
                cs.setWeek(strToInt(tdElement.get(1).text()));
                cs.setOrder(strToInt(tdElement.get(2).text()));
                cs.setSpan(strToInt(tdElement.get(3).text()));
                cs.setLocation(tdElement.get(6).text());
                cs.setFlag((int) (Math.random() * 10));
                course.add(cs);
                Log.d(TAG, "initData:"+cs.getWeekList());
            }
        }
        DataSupport.deleteAll(ClassSchedule.class);
        DataSupport.saveAll(course);
        progressDialog.dismiss();
        Toast.makeText(LeadActivity.this, "获取成功,请返回主界面后下拉刷新", Toast.LENGTH_SHORT).show();
        finish();

    }
    private int strToInt(String str){
        str=str.replace("  ","");
        str=str.replace(" ","");
        try{
            int number  = Integer.valueOf(str);
            return number;
        }catch (NumberFormatException e){
            return 0;
        }

    }
    private void showProgress(){
        progressDialog = new ProgressDialog(LeadActivity.this);
        progressDialog.setMessage("获取中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

   /*private String generateWeekList(String str){

        String  st1="  1,3,5,7,9,11,13,15,17周上";
        String  st2="  1-18周上";
        String  st3="  2,4,6,8,10,12,14,16周上";
        String  st4="  1-16周上";
        String  st5="  1-20周";
        String  st6="  3-16周上";

        if (str.equals(st1)){
            return "1,3,5,7,9,11,13,15,17,";
        }
        if (str.equals(st2)){
            return "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,";
        }
        if (str.equals(st3)){
            return "2,4,6,8,10,12,14,16,";
        }
        if (str.equals(st4)){
            return "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,";
        }
        if (str.equals(st5)){
            return "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,";
        }
        if (str.equals(st6)){
            return "3,4,5,6,7,8,9,10,11,12,13,14,15,16,";
        }
        return "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,";
    }*/
}