package site.imcu.lcus.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.List;

import site.imcu.lcus.course.ClassSchedule;
import site.imcu.lcus.R;

public class EditActivity extends AppCompatActivity {

    EditText edit_name;
    EditText edit_location;
    EditText edit_teacher;
    EditText edit_span;
    EditText edit_weekList;
    EditText edit_note;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setToolBar();
        restoreData();
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
                savaCourse();
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
        edit_weekList=(EditText)findViewById(R.id.edit_weekList);
        edit_note=(EditText)findViewById(R.id.edit_note);
        List<ClassSchedule> classSchedules = DataSupport.where("week = ? and order = ?",String.valueOf(a),String.valueOf(b)).find(ClassSchedule.class);
        edit_name.setText(classSchedules.get(0).getName());
        edit_location.setText(classSchedules.get(0).getLocation());
        edit_teacher.setText(classSchedules.get(0).getTeacher());
        edit_weekList.setText(classSchedules.get(0).getWeekList());
        edit_note.setText(classSchedules.get(0).getNote());
        int span = classSchedules.get(0).getSpan();
        edit_span.setText(String.valueOf(span));
    }

    private void savaCourse(){
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
        classSchedule.setWeekList(edit_weekList.getText().toString());
        classSchedule.setNote(edit_note.getText().toString());
        classSchedule.setSpan(Integer.parseInt(edit_span.getText().toString()));
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

}
