package site.imcu.lcus.activity;

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
import site.imcu.lcus.course.ClassSchedule;
import site.imcu.lcus.R;
import site.imcu.lcus.utils.LoginUtils;

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

                switch (mSession){
                    case "passwordError":
                        Toast.makeText(LeadActivity.this,"密码或者账号错误",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        break;
                    case "null":
                        Toast.makeText(LeadActivity.this,"登陆失败，重新登陆试试吧",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        break;
                    default:
                        session=mSession;
                        getTable();
                        progressDialog.dismiss();
                        finish();
                        break;
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
                cs.setName(tdElement.get(2).text().replace(" ",""));
                cs.setTeacher(tdElement.get(7).text().replace("*","").replace(" ",""));
                cs.setWeekList(formatWeekList(tdElement.get(11).text()));
                cs.setWeek(strToInt(tdElement.get(12).text()));
                cs.setOrder(strToInt(tdElement.get(13).text()));
                cs.setSpan(strToInt(tdElement.get(14).text()));
                cs.setLocation(tdElement.get(17).text().replace(" ",""));
                cs.setFlag((int) (Math.random() * 10));
                course.add(cs);
            }

            if(tdElement.size()!=18&&!tdElement.get(0).text().equals(" ")){
                cs.setName(course.get(course.size()-1).getName().replace(" ",""));
                cs.setTeacher(course.get(course.size()-1).getTeacher().replace("*","").replace(" ",""));
                cs.setWeekList(formatWeekList(tdElement.get(0).text()));
                cs.setWeek(strToInt(tdElement.get(1).text()));
                cs.setOrder(strToInt(tdElement.get(2).text()));
                cs.setSpan(strToInt(tdElement.get(3).text()));
                cs.setLocation(tdElement.get(6).text().replace(" ",""));
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
           return Integer.valueOf(str);
        }catch (NumberFormatException e){
            return 0;
        }

    }
    private String formatWeekList(String string){

        String weekList="1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,";



        if (string.contains("周上")&&string.contains(",")){
            weekList = string.replace("周上",",");
        }

        if (string.contains("-")){

            weekList = "";
            string = string.replace("周上","");
            string = string.replace("周","");

            String a[] = string.split("[-]");

            for (int i=strToInt(a[0]);i<=strToInt(a[1]);i++){
                    weekList +=i+",";
            }


        }


        if (string.contains("3周上")){
            weekList ="3,";
        }
        weekList=weekList.replace(" ","");
        weekList=weekList.replace(" ","");
        weekList = ","+weekList;
        Log.d(TAG, "formatWeekList: "+weekList);
        return weekList;

    }
    private void showProgress(){
        progressDialog = new ProgressDialog(LeadActivity.this);
        progressDialog.setMessage("获取中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

}