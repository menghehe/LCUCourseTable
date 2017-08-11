package site.imcu.lcus.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import site.imcu.lcus.Course.ClassSchedule;
import site.imcu.lcus.R;


public class AddActivity extends AppCompatActivity {


    WheelView weekView;
    WheelView orderView;
    WheelView spanView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        setToolBar();
        setWheelView();
        Button button = (Button)findViewById(R.id.add_save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               saveCourse();
            }
        });

    }
    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void saveCourse(){
        ClassSchedule classSchedule = new ClassSchedule();
        EditText name =(EditText)findViewById(R.id.add_name);
        EditText location=(EditText)findViewById(R.id.add_location);
        classSchedule.setName(name.getText().toString());
        classSchedule.setLocation(location.getText().toString());
        classSchedule.setWeek(getWeek());
        classSchedule.setOrder(getNub(1));
        classSchedule.setSpan(getNub(2)-getNub(1)+1);
        classSchedule.setFlag((int) (Math.random() * 10));
        if (checkData(classSchedule)){
            classSchedule.save();
            Toast.makeText(AddActivity.this,"添加成功，请返回主界面后手动刷新",Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    private Boolean checkData(ClassSchedule classSchedule){
        int week = classSchedule.getWeek();
        int order= classSchedule.getOrder();
        List<ClassSchedule> classScheduleList =DataSupport.where("week = ? and order = ?",String.valueOf(week),String.valueOf(order)).find(ClassSchedule.class);
        if(classSchedule.getName().equals("")){
            Toast.makeText(AddActivity.this,"课程名必填",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(classScheduleList.size()!=0){
            Toast.makeText(AddActivity.this,"当前节已存在课程",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (classSchedule.getSpan()!=1&&classSchedule.getSpan()!=2){
            Toast.makeText(AddActivity.this,"课程长度只有一节或者两节",Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }

    }
    private void setWheelView(){
        List<String> week = new ArrayList<String>();
        String week_1 = "周一";week.add(week_1);
        String week_2 = "周二";week.add(week_2);
        String week_3 = "周三";week.add(week_3);
        String week_4 = "周四";week.add(week_4);
        String week_5 = "周五";week.add(week_5);
        weekView = (WheelView) findViewById(R.id.chose_week);
        weekView.setWheelAdapter(new ArrayWheelAdapter(this)); // 文本数据源
        weekView.setSkin(WheelView.Skin.None); // common皮肤
        weekView.setWheelData(week);  // 数据集合
        List<String> order = new ArrayList<String>();
        String one = "一";order.add(one);
        String two = "二";order.add(two);
        String thr = "三";order.add(thr);
        String fou = "四";order.add(fou);
        String fiv = "五";order.add(fiv);
        String six = "六";order.add(six);
        String sev = "七";order.add(sev);
        String eit = "八";order.add(eit);
        String nin = "九";order.add(nin);
        String ten = "十";order.add(ten);
        orderView = (WheelView) findViewById(R.id.chose_order);
        orderView.setWheelAdapter(new ArrayWheelAdapter(this)); // 文本数据源
        orderView.setSkin(WheelView.Skin.None); // common皮肤
        orderView.setWheelData(order);  // 数据集合
        spanView = (WheelView) findViewById(R.id.chose_span);
        spanView.setWheelAdapter(new ArrayWheelAdapter(this)); // 文本数据源
        spanView.setSkin(WheelView.Skin.None); // common皮肤
        spanView.setWheelData(order);  // 数据集合
    }

    private int getWeek(){
        int week=0;
        switch (weekView.getSelectionItem().toString()){
            case "周一" :
                week=1;
                break;
            case "周二" :
                week=2;
                break;
            case "周三" :
                week=3;
                break;
            case "周四" :
                week=4;
                break;
            case "周五" :
                week=5;
                break;
        }
        return week;
    }
    private int getNub(int i){
        int b=0;
        String s;
        if(i==1){
            s=orderView.getSelectionItem().toString();
        }
        else {
            s=spanView.getSelectionItem().toString();
        }
        switch (s){
            case "一":
                b=1;
            break;
            case "二":
                b=2;
            break;
            case "三":
                b=3;
            break;
            case "四":
               b= 4;
            break;
            case "五":
               b=5;
            break;
            case "六":
                b=6;
            break;
            case "七":
                b=7;
            break;
            case "八":
                b=8;
            break;
            case "九":
                b=9;
            break;
            case "十":
                b=10;
            break;
        }
        return b;

    }

}
