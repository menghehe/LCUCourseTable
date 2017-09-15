package site.imcu.lcus.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindViews;
import site.imcu.lcus.course.ClassSchedule;
import site.imcu.lcus.R;

public class EditActivity extends AppCompatActivity {
    private static final String TAG = "EditActivity";

    EditText edit_name;
    EditText edit_location;
    EditText edit_teacher;
    EditText edit_span;
    EditText edit_note;
    private CheckBox choseWeek01,choseWeek02,choseWeek03,choseWeek04,choseWeek05,choseWeek06,choseWeek07,choseWeek08,choseWeek09,choseWeek10,
            choseWeek11,choseWeek12,choseWeek13,choseWeek14,choseWeek15,choseWeek16,choseWeek17,choseWeek18,choseWeek19,choseWeek20,choseWeek21;
    List<CheckBox> checkBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setToolBar();
        restoreData();
        checkBoxChange();

    }
    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.editbar,menu);
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item){
        Intent intent = getIntent();
        int a = intent.getIntExtra("int_week",1);
        int b =intent.getIntExtra("int_order",1);
        switch (item.getItemId()){
            case R.id.edit_save:
                saveCourse();
                break;
            case R.id.edit_delete:
                DataSupport.deleteAll(ClassSchedule.class,"week = ? and order = ?",String.valueOf(a),String.valueOf(b));
                Toast.makeText(EditActivity.this,"删除成功请下拉刷新",Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        return true;
    }
    private void restoreData(){
        Intent intent = getIntent();
        int a = intent.getIntExtra("int_week",1);
        int b =intent.getIntExtra("int_order",1);
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_location=(EditText)findViewById(R.id.edit_location);
        edit_span=(EditText) findViewById(R.id.edit_span);
        edit_teacher=(EditText)findViewById(R.id.edit_teacher);
        edit_note=(EditText)findViewById(R.id.edit_note);
        List<ClassSchedule> classSchedules = DataSupport.where("week = ? and order = ?",String.valueOf(a),String.valueOf(b)).find(ClassSchedule.class);
        edit_name.setText(classSchedules.get(0).getName());
        edit_location.setText(classSchedules.get(0).getLocation());
        edit_teacher.setText(classSchedules.get(0).getTeacher());
        edit_note.setText(classSchedules.get(0).getNote());
        int span = classSchedules.get(0).getSpan();
        edit_span.setText(String.valueOf(span));


        choseWeek01  = (CheckBox)findViewById(R.id.checkbox01);
        choseWeek02  = (CheckBox)findViewById(R.id.checkbox02);
        choseWeek03  = (CheckBox)findViewById(R.id.checkbox03);
        choseWeek04  = (CheckBox)findViewById(R.id.checkbox04);
        choseWeek05  = (CheckBox)findViewById(R.id.checkbox05);
        choseWeek06  = (CheckBox)findViewById(R.id.checkbox06);
        choseWeek07  = (CheckBox)findViewById(R.id.checkbox07);
        choseWeek08  = (CheckBox)findViewById(R.id.checkbox08);
        choseWeek09  = (CheckBox)findViewById(R.id.checkbox09);
        choseWeek10  = (CheckBox)findViewById(R.id.checkbox10);
        choseWeek11  = (CheckBox)findViewById(R.id.checkbox11);
        choseWeek12  = (CheckBox)findViewById(R.id.checkbox12);
        choseWeek13  = (CheckBox)findViewById(R.id.checkbox13);
        choseWeek14  = (CheckBox)findViewById(R.id.checkbox14);
        choseWeek15  = (CheckBox)findViewById(R.id.checkbox15);
        choseWeek16  = (CheckBox)findViewById(R.id.checkbox16);
        choseWeek17  = (CheckBox)findViewById(R.id.checkbox17);
        choseWeek18  = (CheckBox)findViewById(R.id.checkbox18);
        choseWeek19  = (CheckBox)findViewById(R.id.checkbox19);
        choseWeek20  = (CheckBox)findViewById(R.id.checkbox20);
        choseWeek21  = (CheckBox)findViewById(R.id.checkbox21);

        checkBoxes.add(choseWeek01);
        checkBoxes.add(choseWeek02);
        checkBoxes.add(choseWeek03);
        checkBoxes.add(choseWeek04);
        checkBoxes.add(choseWeek05);
        checkBoxes.add(choseWeek06);
        checkBoxes.add(choseWeek07);
        checkBoxes.add(choseWeek08);
        checkBoxes.add(choseWeek09);
        checkBoxes.add(choseWeek10);
        checkBoxes.add(choseWeek11);
        checkBoxes.add(choseWeek12);
        checkBoxes.add(choseWeek13);
        checkBoxes.add(choseWeek14);
        checkBoxes.add(choseWeek15);
        checkBoxes.add(choseWeek16);
        checkBoxes.add(choseWeek17);
        checkBoxes.add(choseWeek18);
        checkBoxes.add(choseWeek19);
        checkBoxes.add(choseWeek20);
        checkBoxes.add(choseWeek21);

        for (int i=1;i<=checkBoxes.size();i++){
            String week;
            week = ","+i+",";

            if(classSchedules.get(0).getWeekList().contains(week)){
                checkBoxes.get(i-1).setChecked(true);
                Log.d(TAG, "restoreData: "+week+classSchedules.get(0).getWeekList());
            }
            else {
                checkBoxes.get(i-1).setChecked(false);
            }
        }

       // setCheckBoxes(classSchedules.get(0).getWeekList());

    }

    private void saveCourse(){
        Intent intent = getIntent();
        int a = intent.getIntExtra("int_week",1);
        int b =intent.getIntExtra("int_order",1);
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_location=(EditText)findViewById(R.id.edit_location);
        edit_span=(EditText) findViewById(R.id.edit_span);
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setName(edit_name.getText().toString());
        classSchedule.setLocation(edit_location.getText().toString());
        classSchedule.setTeacher(edit_teacher.getText().toString());
        classSchedule.setNote(edit_note.getText().toString());
        classSchedule.setSpan(Integer.parseInt(edit_span.getText().toString()));


        String weekList="";
        for (int i=1;i<=checkBoxes.size();i++){
            if (checkBoxes.get(i-1).isChecked()){
                String week;
                if(i>=10){
                    week = i+",";
                }else {
                    week = i+",";
                }

                weekList+=week;
            }
        }
        classSchedule.setWeekList(","+weekList);

        if(checkData(classSchedule)){
            classSchedule.updateAll("week = ? and order = ?",String.valueOf(a),String.valueOf(b));
            Toast.makeText(EditActivity.this,"编辑成功，请手动刷新课表",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private Boolean checkData(ClassSchedule classSchedule){
        if(classSchedule.getName().equals("")){
            Toast.makeText(EditActivity.this,"课程名必填",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (classSchedule.getSpan()!=1&&classSchedule.getSpan()!=2){
            Toast.makeText(EditActivity.this,"课程长度只有一节或者两节",Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }

    }
    private void checkBoxChange(){
        final CheckBox choseAll = (CheckBox)findViewById(R.id.chose_all);
        choseAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (choseAll.isChecked()){
                    for(int i=0;i<checkBoxes.size();i++){
                        checkBoxes.get(i).setChecked(true);
                    }
                }else {
                    for(int i=0;i<checkBoxes.size();i++){
                        checkBoxes.get(i).setChecked(false);
                    }
                }
            }
        });

        final CheckBox choseDouble = (CheckBox)findViewById(R.id.chose_double);
        choseDouble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(int i=0;i<checkBoxes.size();i++){
                    checkBoxes.get(i).setChecked(false);
                }
                if(choseDouble.isChecked()){
                    for (int i=0;i<checkBoxes.size();i++){
                        if((i+1)%2==0){
                            checkBoxes.get(i).setChecked(true);
                        }
                    }
                }else {
                    for (int i=0;i<checkBoxes.size();i++){
                        if((i+1)%2==0){
                            checkBoxes.get(i).setChecked(false);
                        }
                    }
                }
            }
        });

        final CheckBox choseSingle = (CheckBox)findViewById(R.id.chose_single);
        choseSingle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(int i=0;i<checkBoxes.size();i++){
                    checkBoxes.get(i).setChecked(false);
                }
                if(choseSingle.isChecked()){
                    for (int i=0;i<checkBoxes.size();i++){
                        if((i+1)%2!=0){
                            checkBoxes.get(i).setChecked(true);
                        }
                    }
                }else {
                    for (int i=0;i<checkBoxes.size();i++){
                        if((i+1)%2==0){
                            checkBoxes.get(i).setChecked(false);
                        }
                    }
                }
            }
        });
    }

}
