package site.imcu.lcus.Course;

import org.litepal.crud.DataSupport;

/**
 * Created by mengh on 2017/7/29.
 */

public class ClassSchedule extends DataSupport{
    private int week;//周几
    private int order;//起始节
    private int span;//跨几节课
    private String name;//课程名称
    private String location;//课程地点
    private int flag;//课程颜色

    public int getWeek(){
        return week;
    }
    public int getOrder(){
        return order;
    }
    public int getSpan(){return span;}
    public String getLocation(){
        return location;
    }
    public String getName(){
        return name;
    }
    public int getFlag(){return flag;}

    public void setWeek(int week){
        this.week=week;
    }
    public void setOrder(int order){
        this.order=order;
    }
    public void setSpan(int span){this.span=span;}
    public void setLocation(String location){
        this.location=location;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setFlag(int flag){this.flag=flag;}
}
