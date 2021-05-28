package reference.service;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Course;
import cn.edu.sustech.cs307.dto.CourseSection;
import cn.edu.sustech.cs307.dto.CourseSectionClass;
import cn.edu.sustech.cs307.dto.Student;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;
import reference.util.PrerequisiteUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ReferenceCourseService implements CourseService {

    @Override
    public void addCourse(String courseId, String courseName,
                          int credit, int classHour, Course.CourseGrading grading,
                          @Nullable Prerequisite prerequisite) {
        try(Connection con=SQLDataSource.getInstance().getSQLConnection()){
            Prerequisite simplified= PrerequisiteUtil.transformPrerequisite(prerequisite);
            int groupCount=PrerequisiteUtil.getGroupCount(simplified);

            String sql1="insert into course" +
                    "(courseId, courseName, credit, classHour, isPF, maxprerequisitegroupcount)" +
                    " values(?,?,?,?,?,?);";
            PreparedStatement preparedStatement= con.prepareStatement(sql1);

            preparedStatement.setString(1,courseId);
            preparedStatement.setString(2,courseName);
            preparedStatement.setInt(3,credit);
            preparedStatement.setInt(4,classHour);
            preparedStatement.setBoolean(5, grading == Course.CourseGrading.PASS_OR_FAIL);
            preparedStatement.setInt(6,groupCount);
            preparedStatement.executeUpdate();

            insertPrerequisite(courseId,simplified);


            String sql2="select count(distinct group_id) from prerequisite group by course_id";
            PreparedStatement p2=con.prepareStatement(sql2);
            ResultSet rs=p2.executeQuery();
            if(rs.next()){
                int maxGroupCount=rs.getInt(1);

                if(maxGroupCount!=groupCount){
                    String sql3 ="update course set maxprerequisitegroupcount=? where courseid=?";
                    PreparedStatement p3 =con.prepareStatement(sql3);

                    p3.setInt(1,maxGroupCount);
                    p3.setString(2,courseId);
                    p3.executeUpdate();
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    private void insertPrerequisite(String courseId, @Nullable Prerequisite simplified){
        System.out.println(simplified);
        if(simplified==null){
            return;
        }

        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="insert into prerequisite" +
                    "(course_id, prerequisitecourseid, group_id)" +
                    " values(?,?,?); ";
            PreparedStatement preparedStatement=conn.prepareStatement(sql);
            //conn.setAutoCommit(false);

            int groupId=1;
            if(simplified instanceof CoursePrerequisite){
                CoursePrerequisite course=(CoursePrerequisite)simplified;

                preparedStatement.setString(1,courseId);
                preparedStatement.setString(2,course.courseID);
                preparedStatement.setInt(3,groupId);

                preparedStatement.executeUpdate();

            }else if(simplified instanceof AndPrerequisite){
                AndPrerequisite and=(AndPrerequisite) simplified;

                for (Prerequisite child: and.terms){
                    if(child instanceof CoursePrerequisite){
                        CoursePrerequisite course=(CoursePrerequisite) child;
                        preparedStatement.setString(1,courseId);
                        preparedStatement.setString(2,course.courseID);
                        preparedStatement.setInt(3,groupId);
                        preparedStatement.executeUpdate();
                    }
                }

                preparedStatement.executeUpdate();
            }else if(simplified instanceof OrPrerequisite){
                OrPrerequisite or=(OrPrerequisite) simplified;

                for (Prerequisite andChild:or.terms){
                    if(andChild instanceof AndPrerequisite){
                        AndPrerequisite and=(AndPrerequisite) andChild;
                        for (Prerequisite courseChild:and.terms){
                            if(courseChild instanceof CoursePrerequisite){
                                try{
                                    CoursePrerequisite course=(CoursePrerequisite) courseChild;

                                    preparedStatement.setString(1,courseId);
                                    preparedStatement.setString(2,course.courseID);
                                    preparedStatement.setInt(3,groupId);
                                    preparedStatement.executeUpdate();
                                }catch (SQLException e){
                                    e.printStackTrace();
                                    groupId--;
                                    break;
                                }
                            }
                        }
                        groupId++;
                    }
                }
            }
            //conn.commit();
        }catch (SQLException e){

            e.printStackTrace();
            throw new IntegrityViolationException();
        }




    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {

        try(Connection con= SQLDataSource.getInstance().getSQLConnection()){
            String sql
                    ="insert into coursesection" +
                    "(courseId, semesterId, sectionName, totalCapacity)" +
                    " values(?,?,?,?);";

            PreparedStatement preparedStatement=con.prepareStatement(sql);

            preparedStatement.setString(1,courseId);
            preparedStatement.setInt(2,semesterId);
            preparedStatement.setString(3,sectionName);
            preparedStatement.setInt(4,totalCapacity);

            preparedStatement.executeUpdate(sql,PreparedStatement.RETURN_GENERATED_KEYS);

            ResultSet rs= preparedStatement.getGeneratedKeys();

            if(rs.next()){
                return rs.getInt(3);
            }else {
                throw new IntegrityViolationException();
            }
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek,
                                     List<Short> weekList, short classStart,
                                     short classEnd, String location) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="insert into coursesectionclass" +
                    "(sectionid, instructorid, dayofweek, week, classbegin, classend, location) " +
                    "values (?,?,?,?,?,?,?);";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,sectionId);
            p.setInt(2,instructorId);
            p.setInt(3,dayOfWeek.getValue());
            p.setInt(5,classStart);
            p.setInt(6,classEnd);
            p.setString(7,location);

            int classId =-1;
            for (short week:weekList){
                p.setInt(4,week);
                p.executeUpdate();
                if(classId ==-1){
                    ResultSet rs=p.getGeneratedKeys();
                    classId =rs.getInt(8);
                }
            }

            return classId;
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void removeCourse(String courseId) {
        try (Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="delete from course where courseid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setString(1,courseId);

            p.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void removeCourseSection(int sectionId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="delete from coursesection where sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,sectionId);

            p.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void removeCourseSectionClass(int classId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="delete from coursesectionclass where classid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,classId);

            p.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<Course> getAllCourses() {
        List<Course> result=new ArrayList<>();
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select * from course";
            PreparedStatement p=conn.prepareStatement(sql);

            ResultSet rs=p.executeQuery();

            while (rs.next()){
                String courseId=rs.getString(1);
                String courseName=rs.getString(2);
                int credit=rs.getInt(3);
                int classHour=rs.getInt(4);
                boolean isPF=rs.getBoolean(5);

                Course.CourseGrading grading=isPF? Course.CourseGrading.PASS_OR_FAIL:
                        Course.CourseGrading.HUNDRED_MARK_SCORE;

                Course c=new Course();
                c.id=courseId;
                c.name=courseName;
                c.credit=credit;
                c.classHour=classHour;
                c.grading=grading;

                result.add(c);
            }
            return result;
        }catch (SQLException e){
            e.printStackTrace();
            return result;
        }
    }

    @Override
    public List<CourseSection> getCourseSectionsInSemester(String courseId, int semesterId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            List<CourseSection> result=new ArrayList<>();

            String sql="select cs2.sectionid, cs2.sectionname, cs2.totalcapacity, count(s.studentid) from coursesection as cs2 " +
                    "inner join studentcourseselection as s on cs2.sectionid = s.sectionid " +
                    "where courseid=? and semesterid=? group by s.studentid,cs2.sectionid;";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setString(1,courseId);
            p.setInt(2,semesterId);

            ResultSet rs=p.executeQuery();

            while (rs.next()){
                CourseSection cs=new CourseSection();

                cs.id=rs.getInt(1);
                cs.name=rs.getString(2);
                cs.totalCapacity=rs.getInt(3);
                cs.leftCapacity=cs.totalCapacity-rs.getInt(4);
                result.add(cs);
            }

            return result;
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }

    }

    @Override
    public Course getCourseBySection(int sectionId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            Course c=new Course();
            String sql="select co.courseid,co.coursename,co.credit,co.classhour,co.ispf from course as co " +
                    "inner join coursesection c on co.courseid = c.courseid " +
                    "where c.sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);
            p.setInt(1,sectionId);

            ResultSet rs=p.executeQuery();

            if(rs.next()){
                c.id=rs.getString(1);
                c.name=rs.getString(2);
                c.credit=rs.getInt(3);
                c.classHour=rs.getInt(4);
                c.grading=
                        rs.getBoolean(5)? Course.CourseGrading.PASS_OR_FAIL
                                : Course.CourseGrading.HUNDRED_MARK_SCORE;
                return c;
            }else{
                return null;
            }

        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<CourseSectionClass> getCourseSectionClasses(int sectionId) {
        return null;
    }

    @Override
    public CourseSection getCourseSectionByClass(int classId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select csc.sectionid, c.sectionname,c.totalcapacity,count(s.studentid) " +
                    "from coursesectionclass as csc " +
                    "inner join coursesection c on c.sectionid = csc.sectionid " +
                    "inner join studentcourseselection s on c.sectionid = s.sectionid where csc.classid=? " +
                    "group by csc.sectionid,c.sectionname,c.totalcapacity;";
            PreparedStatement p=conn.prepareStatement(sql);
            p.setInt(1,classId);

            ResultSet rs=p.executeQuery();

            if(rs.next()){
                CourseSection cs=new CourseSection();
                cs.id=rs.getInt(1);
                cs.name=rs.getString(2);
                cs.totalCapacity=rs.getInt(3);
                cs.leftCapacity=cs.totalCapacity-rs.getInt(4);

                return cs;
            }else {
                throw new SQLException();
            }
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId) {
        return null;
    }
}
