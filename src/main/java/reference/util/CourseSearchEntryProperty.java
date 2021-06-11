package reference.util;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.CourseSearchEntry;
import cn.edu.sustech.cs307.service.StudentService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CourseSearchEntryProperty{
    public int studentId;
    public boolean isFull=false;
    public boolean isHasConflict=false;
    public boolean isHasPassed=false;
    public boolean isPrerequisitesMissing=false;
    public boolean isAcceptable4CourseType=false;
    public CourseSearchEntry entry;

    public CourseSearchEntryProperty(CourseSearchEntry entry,int sid){
        this.entry=entry;
        this.studentId=sid;
    }

    public void checkIsFull(){
        try(Connection conn= SQLDataSource.getInstance().getSQLConnection()){
            int sectionId=entry.section.id;
            String sql="select c.totalcapacity,count(*) from studentcourseselection as scs " +
                    "inner join coursesection c on c.sectionid = scs.sectionid " +
                    "where c.sectionid=? group by c.totalcapacity,scs.studentid";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,sectionId);

            ResultSet rs=p.executeQuery();
            if(rs.next()){
                int total=rs.getInt(1);
                int current=rs.getInt(2);
                this.isFull=total<=current;
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void checkIsHasConflict(){
        this.entry.conflictCourseNames=new ArrayList<>();

        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){

            int sectionId=entry.section.id;

            String sql="select sameTimeCourse.sectionid," +
                    "sameTimeCourse.coursename||'['||sameTimeCourse.sectionname||']' as allName from( " +
                    "select otherClasses.*,cs.sectionname,c2.coursename from " +
                    "(select c.*,cs.semesterid " +
                    "from coursesection as cs " +
                    "inner join coursesectionclass c on cs.sectionid = c.sectionid " +
                    "where cs.sectionid=?) as currentCourseSection " +
                    "inner join coursesectionclass as otherClasses " +
                    "    on ((otherClasses.classbegin<=currentCourseSection.classbegin " +
                    "and otherClasses.classend>=currentCourseSection.classend)or " +
                    "(otherClasses.classbegin>=currentCourseSection.classbegin " +
                    "and otherClasses.classend<=currentCourseSection.classend) or " +
                    "(otherClasses.classbegin>=currentCourseSection.classbegin and" +
                    "                   otherClasses.classbegin<=currentCourseSection.classend)" +
                    "                or(otherClasses.classend>=currentCourseSection.classbegin and" +
                    "                   otherClasses.classend<=currentCourseSection.classend)) " +
                    "and otherClasses.dayofweek=currentCourseSection.dayofweek " +
                    "and otherClasses.week=currentCourseSection.week " +
                    "inner join coursesection as cs " +
                    "   on cs.sectionid=otherClasses.sectionid" +
                    "   and cs.semesterid=currentCourseSection.semesterid " +
                    "inner join course c2 on cs.courseid = c2.courseid" +
                    ") as sameTimeCourse " +
                    "inner join (select cs1.studentid, sectionid from studentcourseselection as cs1 " +
                    "union all (select cs2.studentid, sectionid from student100course as cs2)" +
                    "union all (select cs3.studentid, sectionid from studentpfcourse as cs3)) as selection " +
                    "on selection.sectionid=sameTimeCourse.sectionid " +
                    "where selection.studentid=?";
            PreparedStatement p=conn.prepareStatement(sql);


            p.setInt(1,sectionId);
            p.setInt(2,studentId);

            ResultSet rs1 =p.executeQuery();

            Set<String> conflictSet=new HashSet<>();
            while (rs1.next()){
                conflictSet.add(rs1.getString(2));
            }


            String sql2="with t as (select c2.courseid,c.semesterid,c2.coursename,c.sectionname " +
                    "from coursesectionclass as cs3 " +
                    "    inner join coursesection c on c.sectionid = cs3.sectionid " +
                    "    inner join course c2 on c2.courseid = c.courseid " +
                    "where c.sectionid=?) " +
                    "select t.coursename||'['||t.sectionname||']','a' from t " +
                    "    inner join coursesection as cs on cs.courseid=t.courseid " +
                    "    inner join studentcourseselection s on cs.sectionid = s.sectionid " +
                    "where s.studentid=? and t.semesterid=cs.semesterid " +
                    "union all " +
                    "select t.coursename||'['||t.sectionname||']','b' from t " +
                    "    inner join coursesection as cs on cs.courseid=t.courseid " +
                    "    inner join student100course s on cs.sectionid = s.sectionid " +
                    "where s.studentid=? and t.semesterid=cs.semesterid " +
                    "union all " +
                    "select t.coursename||'['||t.sectionname||']','c' from t " +
                    "    inner join coursesection as cs on cs.courseid=t.courseid " +
                    "    inner join studentpfcourse s on cs.sectionid = s.sectionid " +
                    "where s.studentid=? and t.semesterid=cs.semesterid;";
            PreparedStatement p2=conn.prepareStatement(sql2);
            p2.setInt(1,sectionId);
            p2.setInt(2,studentId);
            p2.setInt(3,studentId);
            p2.setInt(4,studentId);

            ResultSet rs2=p2.executeQuery();

            while (rs2.next()){
                conflictSet.add(rs2.getString(1));
            }
            this.entry.conflictCourseNames.addAll(conflictSet);
            Collections.sort(entry.conflictCourseNames);

            /*if(studentId==11711705 && entry.conflictCourseNames.size()==2){
                System.out.println(studentId);
            }*/

            this.isHasConflict=!entry.conflictCourseNames.isEmpty();


        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void checkIsHasPassed(){
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            int sectionId=entry.section.id;

            String sql="select * from " +
                    "((select c.courseid from student100course " +
                    "inner join coursesection c on c.sectionid = student100course.sectionid" +
                    " where studentid=?and grade>=60)" +
                    "union all " +
                    "(select c2.courseid from studentpfcourse " +
                    "inner join coursesection c2 on c2.sectionid = studentpfcourse.sectionid" +
                    " where studentid=? and grade='P')) as allPassed " +
                    "inner join coursesection cs on cs.courseid=allPassed.courseid and cs.sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,studentId);
            p.setInt(3,sectionId);

            ResultSet rs=p.executeQuery();

            this.isHasPassed=rs.next();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void checkIsPrerequisitesMissing(){
        this.isPrerequisitesMissing=!Util.SERVICE_FACTORY.createService(StudentService.class)
                .passedPrerequisitesForCourse(studentId,entry.course.id);
    }

    public void checkIsAcceptable4CourseType(StudentService.CourseType type){
        if(type== StudentService.CourseType.ALL){
            this.isAcceptable4CourseType=true;
            return;
        }

        try (Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select isCompul_isMajor.iscompulsory,isCompul_isMajor.isMajor from(" +
                    "(select mc.courseid,mc.iscompulsory,true as isMajor from majorcourse as mc " +
                    "inner join students s on mc.majorid = s.majorid" +
                    " where mc.courseid=? and s.userid=?)" +
                    "union all " +
                    "(select mc.courseid,mc.iscompulsory,false as isMajor from majorcourse as mc " +
                    "inner join students s2 on mc.majorid <> s2.majorid " +
                    "where mc.courseid=? and s2.userid=?)) as isCompul_isMajor";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setString(1,entry.course.id);
            p.setInt(2,studentId);
            p.setString(3,entry.course.id);
            p.setInt(4,studentId);

            ResultSet rs=p.executeQuery();

            if(rs.next()){
                boolean isCompulsory=rs.getBoolean(1);
                boolean isMajor=rs.getBoolean(2);
                if(isMajor){
                    if(isCompulsory){
                        this.isAcceptable4CourseType=(type== StudentService.CourseType.MAJOR_COMPULSORY);
                    }else {
                        this.isAcceptable4CourseType=(type== StudentService.CourseType.MAJOR_ELECTIVE);
                    }
                }else {
                    this.isAcceptable4CourseType=(type== StudentService.CourseType.CROSS_MAJOR);
                }
            }else {
                this.isAcceptable4CourseType=(type== StudentService.CourseType.PUBLIC);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void checkAll(StudentService.CourseType type){

        checkIsFull();
        checkIsHasConflict();
        checkIsHasPassed();
        checkIsPrerequisitesMissing();
        checkIsAcceptable4CourseType(type);
    }

    @Override
    public String toString() {
        return "CourseSearchEntryProperty{" +
                "studentId=" + studentId +
                ", isFull=" + isFull +
                ", isHasConflict=" + isHasConflict +
                ", isHasPassed=" + isHasPassed +
                ", isPrerequisitesMissing=" + isPrerequisitesMissing +
                '}';
    }
}