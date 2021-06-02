package reference.service;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;
import reference.util.PrerequisiteUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        //System.out.println(simplified);
        if(simplified==null){
            return;
        }

        Connection conn=null;

        try{
            conn=SQLDataSource.getInstance().getSQLConnection();
            String sql="insert into prerequisite" +
                    "(course_id, prerequisitecourseid, group_id)" +
                    " values(?,?,?); ";
            PreparedStatement preparedStatement=conn.prepareStatement(sql);
            conn.setAutoCommit(false);


            int groupId=1;
            if(simplified instanceof CoursePrerequisite){
                CoursePrerequisite course=(CoursePrerequisite)simplified;

                preparedStatement.setString(1,courseId);
                preparedStatement.setString(2,course.courseID);
                preparedStatement.setInt(3,groupId);

                preparedStatement.addBatch();

            }else if(simplified instanceof AndPrerequisite){
                AndPrerequisite and=(AndPrerequisite) simplified;

                for (Prerequisite child: and.terms){
                    if(child instanceof CoursePrerequisite){
                        CoursePrerequisite course=(CoursePrerequisite) child;
                        preparedStatement.setString(1,courseId);
                        preparedStatement.setString(2,course.courseID);
                        preparedStatement.setInt(3,groupId);
                        preparedStatement.addBatch();
                    }
                }

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
                                    preparedStatement.addBatch();
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
            preparedStatement.executeBatch();


            conn.commit();
            conn.close();
        }catch (SQLException e){
            try{
                if(conn!=null){
                    conn.rollback();
                    conn.close();
                }
            }catch (SQLException ex){
                ex.printStackTrace();
            }

            e.printStackTrace();
            throw new IntegrityViolationException();
        }

    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {

        Connection conn = null;
        try{
            conn =SQLDataSource.getInstance().getSQLConnection();
            conn.setAutoCommit(false);

            String sql
                    ="insert into coursesection" +
                    "(courseId, semesterId, sectionName, totalCapacity)" +
                    "values(?, ?, ?, ?);";

            PreparedStatement preparedStatement= conn.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1,courseId);
            preparedStatement.setInt(2,semesterId);
            preparedStatement.setString(3,sectionName);
            preparedStatement.setInt(4,totalCapacity);

            preparedStatement.executeUpdate();

            ResultSet rs= preparedStatement.getGeneratedKeys();

            if(rs.next()){

                int result=rs.getInt(3);
                conn.commit();
                conn.close();

                return result;
            }else {
                conn.commit();
                conn.close();

                throw new IntegrityViolationException();
            }
        }catch (SQLException e){
            try{
                if(conn !=null){
                    conn.rollback();
                    conn.close();
                }
            }catch (SQLException ex){
                ex.printStackTrace();
            }

            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }



    @Override
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek,
                                     Set<Short> weekList, short classStart,
                                     short classEnd, String location) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            adjustSequenceVal(conn);

            String sql="insert into coursesectionclass" +
                    "(sectionid, instructorid, dayofweek, week, classbegin, classend, location,classid) " +
                    "values (?,?,?,?,?,?,?,currval('coursesectionclass_classid_seq'));";
            PreparedStatement p=conn.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);

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
                    if(rs.next()){
                        classId =rs.getInt(8);
                    }else {
                        throw new EntityNotFoundException();
                    }

                }
            }

            return classId;
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    private void adjustSequenceVal(Connection conn){
        try{
            String getNextSQL="select nextval('coursesectionclass_classid_seq');";
            PreparedStatement p= conn.prepareStatement(getNextSQL);
            p.executeQuery();
        }catch (SQLException e){
            e.printStackTrace();
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
            if(result.isEmpty()){
                throw new EntityNotFoundException();
            }else {
                return result;
            }
        }catch (SQLException e){
            e.printStackTrace();
            return result;
        }
    }

    @Override
    public List<CourseSection> getCourseSectionsInSemester(String courseId, int semesterId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            List<CourseSection> result=new ArrayList<>();

            String sql="select cs2.sectionid, cs2.sectionname, cs2.totalcapacity, count(s.studentid) " +
                    "from coursesection as cs2 " +
                    "left outer join studentcourseselection as s on cs2.sectionid = s.sectionid " +
                    "where courseid=? and semesterid=? group by s.studentid,cs2.sectionid;";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setString(1,courseId);
            p.setInt(2,semesterId);

            System.out.println(p);

            ResultSet rs=p.executeQuery();

            while (rs.next()){
                CourseSection cs=new CourseSection();

                cs.id=rs.getInt(1);
                cs.name=rs.getString(2);
                cs.totalCapacity=rs.getInt(3);
                cs.leftCapacity=cs.totalCapacity-rs.getInt(4);
                result.add(cs);
            }

            if(result.isEmpty()){
                throw new EntityNotFoundException();
            }else {
                return result;
            }


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
                throw new EntityNotFoundException();
            }

        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<CourseSectionClass> getCourseSectionClasses(int sectionId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            List<CourseSectionClass> result=new ArrayList<>();

            String sql="select classid, i.userid as instructorId, i.firstname||' '||i.lastname as fullName" +
                    ",dayofweek,classbegin,classend,location,array_agg(week) as weekList " +
                    "from coursesectionclass " +
                    "inner join instructor i on i.userid = coursesectionclass.instructorid where sectionid=? " +
                    "group by classid,instructorId,fullName,i.userid,dayofweek,classbegin,classend,location";
            PreparedStatement p= conn.prepareStatement(sql);

            p.setInt(1,sectionId);

            ResultSet rs=p.executeQuery();

            while (rs.next()){
                CourseSectionClass csClass=new CourseSectionClass();
                Instructor instructor=new Instructor();

                csClass.id=rs.getInt(1);

                instructor.id=rs.getInt(2);
                instructor.fullName=rs.getString(3);

                csClass.instructor=instructor;

                csClass.dayOfWeek= DayOfWeek.of(rs.getInt(4));

                csClass.classBegin=(short) rs.getInt(5);
                csClass.classEnd=(short) rs.getInt(6);

                csClass.location=rs.getString(7);

                Array weekList=rs.getArray(8);

                Set<Short>weeks=new HashSet<>();

                for (Object o:(Object[]) weekList.getArray()){
                    if(o instanceof Number){
                        try{
                            int x=(int)o;
                            short s=(short)x;
                            weeks.add(s);
                        }catch (Exception e){
                            e.printStackTrace();
                            break;
                        }
                    }
                }

                csClass.weekList=weeks;

                result.add(csClass);
            }

            if(result.isEmpty()){
                throw new EntityNotFoundException();
            }else {
                return result;
            }


        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
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
                throw new EntityNotFoundException();
            }
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId) {

        Connection conn=null;
        try{
            conn=SQLDataSource.getInstance().getSQLConnection();
            conn.setAutoCommit(false);

            List<Student> result=new ArrayList<>();



            String sql="select stu.userid as id, getfullname(stu.firstname,stu.lastname) as fullname, stu.enrolleddate, " +
                    "m.majorid as majorid, m.name as majorName, " +
                    "d.departmentid as departmentId, d.name as departmentName " +
                    " from coursesection " +
                    "inner join studentcourseselection s on coursesection.sectionid = s.sectionid " +
                    "inner join students as stu on stu.userid=s.studentid " +
                    "inner join major m on m.majorid = stu.majorid " +
                    "inner join department d on d.departmentid = m.departmentid "+
                    "where courseid=? and semesterid=? ;";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setString(1,courseId);
            p.setInt(2,semesterId);

            ResultSet rs=p.executeQuery();
            conn.commit();

            while (rs.next()){
                Student s=new Student();

                s.id=rs.getInt(1);
                s.fullName=rs.getString(2);
                s.enrolledDate=rs.getDate(3);

                Major m=new Major();
                m.id=rs.getInt(4);
                m.name=rs.getString(5);
                s.major=m;

                Department d=new Department();
                d.id=rs.getInt(6);
                d.name=rs.getString(7);
                m.department=d;

                result.add(s);
            }

            conn.close();

            if(result.isEmpty()){
                throw new EntityNotFoundException();
            }else {
                return result;
            }
        }catch (SQLException e){
            try{
                if(conn!=null){
                    conn.rollback();
                    conn.close();
                }
            }catch (SQLException ex){
                ex.printStackTrace();
            }

            throw new IntegrityViolationException();
        }
    }
}
