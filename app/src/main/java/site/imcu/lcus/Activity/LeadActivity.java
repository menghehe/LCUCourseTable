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
                Log.d(TAG, "onClick: ");
                login();
            }
        });
    }
    private void login(){
        showLoginProgress();
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
                    progressDialog.dismiss();
                    getTable();
                }else {
                    login();
                    progressDialog.dismiss();
                }
            }
        };
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);

    }
    private void getTable(){
        showGetProgress();
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
                    progressDialog.dismiss();
                    Toast.makeText(LeadActivity.this, "获取成功,请返回主界面后下拉刷新", Toast.LENGTH_SHORT).show();
                    finish();
            }
    private void showLoginProgress(){
        progressDialog = new ProgressDialog(LeadActivity.this);
        progressDialog.setMessage("登陆中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void showGetProgress(){
        progressDialog = new ProgressDialog(LeadActivity.this);
        progressDialog.setMessage("获取中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}