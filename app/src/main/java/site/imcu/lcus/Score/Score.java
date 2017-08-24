package site.imcu.lcus.score;

/**
 * Created by SHIELD_7 on 2017/8/8.
 *
 */

class Score {
    private String courseName;
    private String credit;
    private String courseAttr;
    private String mark;
    private String position;

    String getCourseName() {
        return courseName;
    }

    void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    String getCredit() {
        return credit;
    }

    void setCredit(String credit) {
        this.credit = credit;
    }

    String getCourseAttr() {
        return courseAttr;
    }

    void setCourseAttr(String courseAttr) {
        this.courseAttr = courseAttr;
    }

    String getMark() {
        return mark;
    }

    void setMark(String mark) {
        this.mark = mark;
    }

    String getPosition() {
        return position;
    }

    void setPosition(String position) {
        this.position = position;
    }
}
