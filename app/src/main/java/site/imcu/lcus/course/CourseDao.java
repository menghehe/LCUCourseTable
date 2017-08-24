package site.imcu.lcus.course;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SHIELD_7 on 17/8/1.
 *
 */
public class CourseDao {

    public static List<ClassSchedule>[] getCourseData() {

        @SuppressWarnings("unchecked")
        List<ClassSchedule> courseModels[] = new ArrayList[7];
        for (int i = 0; i < courseModels.length; i++) {
            courseModels[i] = new ArrayList<>();
        }
        List<ClassSchedule> models_1;
        models_1 = DataSupport.where("week = ?","1").find(ClassSchedule.class);
        courseModels[0].addAll(models_1);

        List<ClassSchedule> models_2;
        models_2 = DataSupport.where("week = ?","2").find(ClassSchedule.class);
        courseModels[1].addAll(models_2);

        List<ClassSchedule> models_3;
        models_3 = DataSupport.where("week = ?","3").find(ClassSchedule.class);
        courseModels[2].addAll(models_3);

        List<ClassSchedule> models_4;
        models_4 = DataSupport.where("week = ?","4").find(ClassSchedule.class);
        courseModels[3].addAll(models_4);

        List<ClassSchedule> models_5;
        models_5 = DataSupport.where("week = ?","5").find(ClassSchedule.class);
        courseModels[4].addAll(models_5);


        return courseModels;

    }
}
