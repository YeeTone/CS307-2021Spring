package reference.util;

import cn.edu.sustech.cs307.config.Config;
import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.factory.ServiceFactory;
import cn.edu.sustech.cs307.service.StudentService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.*;

@ParametersAreNonnullByDefault
public class SearchCourseUtil extends Util{
    private SearchCourseUtil(){
        super();
    }

    /**
     * Search available courses (' sections) for the specified student in the semester with extra conditions.
     * The result should be first sorted by course ID, and then sorted by course full name (course.name[section.name]).
     *
     * @param studentId
     * @param semesterId
     * @param searchCid                  search course id. Rule: searchCid in course.id
     * @param searchName                 search course name. Rule: searchName in "course.name[section.name]"
     * @param searchInstructor           search instructor name.
     *                                   Rule: firstName + lastName begins with searchInstructor
     *                                   or firstName + ' ' + lastName begins with searchInstructor
     *                                   or firstName begins with searchInstructor
     *                                   or lastName begins with searchInstructor.
     * @param searchDayOfWeek            search day of week. Matches *any* class in the section in the search day of week.
     * @param searchClassTime            search class time. Matches *any* class in the section contains the search class time.
     * @param searchClassLocations       search class locations. Matches *any* class in the section contains *any* location from the search class locations.
     * @param searchCourseType           search course type. See {@link cn.edu.sustech.cs307.service.StudentService.CourseType}
     * @param ignoreFull                 whether or not to ignore full course sections.
     * @param ignoreConflict             whether or not to ignore course or time conflicting course sections.
     *                                   Note that a section is both course and time conflicting with itself.
     * @param ignorePassed               whether or not to ignore the student's passed courses.
     * @param ignoreMissingPrerequisites whether or not to ignore courses with missing prerequisites.
     * @param pageSize                   the page size, effectively `limit pageSize`.
     *                                   It is the number of {@link cn.edu.sustech.cs307.dto.CourseSearchEntry}
     * @param pageIndex                  the page index, effectively `offset pageIndex * pageSize`.
     *                                   If the page index is so large that there is no message,return an empty list
     * @return a list of search entries. See {@link cn.edu.sustech.cs307.dto.CourseSearchEntry}
     */

    public static List<CourseSearchEntry> searchCourse
            (int studentId, int semesterId, @Nullable String searchCid, @Nullable String searchName,
             @Nullable String searchInstructor, @Nullable DayOfWeek searchDayOfWeek,
             @Nullable Short searchClassTime, @Nullable List<String> searchClassLocations,
             StudentService.CourseType searchCourseType, boolean ignoreFull,boolean ignoreConflict,
             boolean ignorePassed, boolean ignoreMissingPrerequisites,
             int pageSize,int pageIndex){
        List<CourseSearchEntry> result=new ArrayList<>();
        Map<CourseSectionNode, Set<CourseSectionClass>>classMap=new HashMap<>();

        /*if(studentId==11712716){
            System.out.println(5);
        }*/

        try(Connection conn= SQLDataSource.getInstance().getSQLConnection()){
            String sql;
            PreparedStatement p;
            if(searchClassLocations ==null){
                sql="select distinct ak.courseid, ak.coursename, ak.credit, ak.classhour, " +
                        "ak.ispf, ak.sectionid, ak.sectionname, " +
                        "ak.totalcapacity, ak.cnt, ak.classid, ak.userid,ak.fullName, ak.dayofweek, array_agg(csc.week)," +
                        "ak.classbegin, ak.classend, ak.location from(" +
                        "select c.courseid, c.coursename, c.credit, c.classhour, c.ispf," +
                        "cs.sectionid,cs.sectionname,cs.totalcapacity,count(scs.studentid) as cnt," +
                        "c2.classid,i.userid,getfullname(i.firstname,i.lastname) as fullName,c2.dayofweek," +
                        "array_agg(c2.week),c2.classbegin,c2.classend,c2.location from course as c " +
                        "inner join coursesection cs on c.courseid = cs.courseid " +
                        "inner join coursesectionclass c2 on cs.sectionid = c2.sectionid " +
                        "inner join instructor i on i.userid = c2.instructorid " +
                        "left outer join studentcourseselection as scs on scs.sectionid=cs.sectionid " +
                        "where (c.courseid like('%'||?||'%') or ? is null) and (c.coursename||'['||cs.sectionname||']' like '%'||?||'%' or ? is null)" +
                        "and (? is null or i.firstname||i.lastname like('%'||?||'%') or(i.firstname||' '||i.lastname like('%'||?||'%'))" +
                        "or i.firstname like('%'||?||'%') or i.lastname like('%'||?||'%'))" +
                        "and (? is null or c2.dayofweek=?) " +
                        "and (? is null or ? between c2.classbegin and c2.classend)" +
                        "and (cs.semesterid=?) " +
                        //"and (? is null or c2.location like('%'||?||'%')) "+
                        "group by c.courseid, c.coursename, c.credit, c.classhour, c.ispf, cs.sectionid, cs.sectionname, " +
                        "cs.totalcapacity, c2.classid, i.userid, getfullname(i.firstname,i.lastname), " +
                        "c2.dayofweek, c2.classbegin, c2.classend, c2.location) as ak " +
                        "inner join coursesectionclass as csc on csc.sectionid=ak.sectionid " +
                        "group by csc.sectionid,csc.dayofweek,ak.courseid, coursename, credit, classhour, ispf, " +
                        "ak.sectionid, ak.sectionname, ak.totalcapacity, ak.cnt, ak.classid, ak.userid, " +
                        "ak.dayofweek, ak.classbegin, ak.classend, ak.location, ak.fullName";

            }else {
                sql="select distinct(ak.courseid, ak.coursename, ak.credit, ak.classhour, " +
                        "ak.ispf, ak.sectionid, ak.sectionname, " +
                        "ak.totalcapacity, ak.cnt, ak.classid, ak.userid,ak.fullName, ak.dayofweek, array_agg(csc.week)," +
                        "ak.classbegin, ak.classend, ak.location) from(" +
                        "select c.courseid, c.coursename, c.credit, c.classhour, c.ispf," +
                        "cs.sectionid,cs.sectionname,cs.totalcapacity,count(scs.studentid) as cnt," +
                        "c2.classid,i.userid,getfullname(i.firstname,i.lastname) as fullName,c2.dayofweek," +
                        "array_agg(c2.week),c2.classbegin,c2.classend,c2.location from course as c " +
                        "inner join coursesection cs on c.courseid = cs.courseid " +
                        "inner join coursesectionclass c2 on cs.sectionid = c2.sectionid " +
                        "inner join instructor i on i.userid = c2.instructorid " +
                        "left outer join studentcourseselection as scs on scs.sectionid=cs.sectionid " +
                        "where (c.courseid like('%'||?||'%') or ? is null) and (c.coursename||'['||cs.sectionname||']' like '%'||?||'%' or ? is null)" +
                        "and (? is null or i.firstname||i.lastname like('%'||?||'%') or(i.firstname||' '||i.lastname like('%'||?||'%'))" +
                        "or i.firstname like('%'||?||'%') or i.lastname like('%'||?||'%'))" +
                        "and (? is null or c2.dayofweek=?) " +
                        "and (? is null or ? between c2.classbegin and c2.classend)" +
                        "and (cs.semesterid=?) " +
                        "and (? is null or c2.location like('%'||?||'%')) "+
                        "group by c.courseid, c.coursename, c.credit, c.classhour, c.ispf, cs.sectionid, cs.sectionname, " +
                        "cs.totalcapacity, c2.classid, i.userid, getfullname(i.firstname,i.lastname), " +
                        "c2.dayofweek, c2.classbegin, c2.classend, c2.location) as ak " +
                        "inner join coursesectionclass as csc on csc.sectionid=ak.sectionid " +
                        "group by csc.sectionid,csc.dayofweek,ak.courseid, coursename, credit, classhour, ispf, " +
                        "ak.sectionid, ak.sectionname, ak.totalcapacity, ak.cnt, ak.classid, ak.userid, " +
                        "ak.dayofweek, ak.classbegin, ak.classend, ak.location, ak.fullName";
            }
            p=conn.prepareStatement(sql);
            p.setString(1,searchCid);
            p.setString(2,searchCid);
            p.setString(3,searchName);
            p.setString(4,searchName);
            p.setString(5,searchInstructor);
            p.setString(6,searchInstructor);
            p.setString(7,searchInstructor);
            p.setString(8,searchInstructor);
            p.setString(9,searchInstructor);
            if(searchDayOfWeek==null){
                p.setNull(10,Types.INTEGER);
                p.setNull(11,Types.INTEGER);
            }else {
                p.setInt(10,searchDayOfWeek.getValue());
                p.setInt(11,searchDayOfWeek.getValue());
            }
            if(searchClassTime==null){
                p.setNull(12,Types.INTEGER);
                p.setNull(13,Types.INTEGER);
            }else {
                p.setInt(12,searchClassTime);
                p.setInt(13,searchClassTime);
            }

            p.setInt(14,semesterId);


            if(searchClassLocations!=null){
                for (String loc:searchClassLocations) {
                    p.setString(15, loc);
                    p.setString(16, loc);

                    //System.out.println(p);
                    ResultSet rs = p.executeQuery();

                    while (rs.next()) {
                        Course c = new Course();
                        c.id = rs.getString(1);
                        c.name = rs.getString(2);
                        c.credit = rs.getInt(3);
                        c.classHour = rs.getInt(4);
                        c.grading = rs.getBoolean(5) ? Course.CourseGrading.PASS_OR_FAIL
                                : Course.CourseGrading.HUNDRED_MARK_SCORE;

                        CourseSection cs = new CourseSection();
                        cs.id = rs.getInt(6);
                        cs.name = rs.getString(7);
                        cs.totalCapacity = rs.getInt(8);
                        cs.leftCapacity = cs.totalCapacity - rs.getInt(9);

                        CourseSectionNode csn = new CourseSectionNode();
                        csn.c = c;
                        csn.cs = cs;


                        CourseSectionClass csc = new CourseSectionClass();
                        csc.id = rs.getInt(10);

                        Instructor instructor = new Instructor();
                        instructor.id = rs.getInt(11);
                        instructor.fullName = rs.getString(12);
                        csc.instructor = instructor;

                        csc.dayOfWeek = DayOfWeek.values()[rs.getInt(13) - 1];

                        Array weekList = rs.getArray(14);
                        Set<Short> weeks = new HashSet<>();
                        for (Object o : (Object[]) weekList.getArray()) {
                            if (o instanceof Number) {
                                try {
                                    int x = (int) o;
                                    short s = (short) x;
                                    weeks.add(s);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }

                        csc.weekList=weeks;
                        csc.classBegin = rs.getShort(15);
                        csc.classEnd = rs.getShort(16);
                        csc.location = rs.getString(17);

                        if (!classMap.containsKey(csn)) {
                            classMap.put(csn, new HashSet<>());
                        }
                        classMap.get(csn).add(csc);
                    }

                }
            }else {
                /*if(studentId==11710673 && pageSize==25 && pageIndex==0){
                    System.out.println(p);
                    System.exit(0);
                }*/
                //System.err.println(p);
                ResultSet rs=p.executeQuery();


                while (rs.next()) {
                    Course c = new Course();
                    c.id = rs.getString(1);
                    c.name = rs.getString(2);
                    c.credit = rs.getInt(3);
                    c.classHour = rs.getInt(4);
                    c.grading = rs.getBoolean(5) ? Course.CourseGrading.PASS_OR_FAIL
                            : Course.CourseGrading.HUNDRED_MARK_SCORE;

                    CourseSection cs = new CourseSection();
                    cs.id = rs.getInt(6);
                    cs.name = rs.getString(7);
                    cs.totalCapacity = rs.getInt(8);
                    cs.leftCapacity = cs.totalCapacity - rs.getInt(9);

                    CourseSectionNode csn = new CourseSectionNode();
                    csn.c = c;
                    csn.cs = cs;

                    CourseSectionClass csc = new CourseSectionClass();
                    csc.id = rs.getInt(10);

                    Instructor instructor = new Instructor();
                    instructor.id = rs.getInt(11);
                    instructor.fullName = rs.getString(12);
                    csc.instructor = instructor;

                    csc.dayOfWeek = DayOfWeek.values()[rs.getInt(13) - 1];

                    Array weekList = rs.getArray(14);
                    Set<Short> weeks = new HashSet<>();
                    for (Object o : (Object[]) weekList.getArray()) {
                        if (o instanceof Number) {
                            try {
                                int x = (int) o;
                                short s = (short) x;
                                weeks.add(s);
                            } catch (Exception e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                    csc.weekList = weeks;
                    csc.classBegin = rs.getShort(15);
                    csc.classEnd = rs.getShort(16);
                    csc.location=rs.getString(17);

                    if(!classMap.containsKey(csn)){
                        classMap.put(csn,new HashSet<>());
                    }
                    classMap.get(csn).add(csc);
                }
            }


            for (Map.Entry<CourseSectionNode, Set<CourseSectionClass>> entry:classMap.entrySet()){
                CourseSearchEntry searchEntry=new CourseSearchEntry();
                searchEntry.course=entry.getKey().c;


                searchEntry.section=entry.getKey().cs;
                searchEntry.sectionClasses=entry.getValue();
                searchEntry.conflictCourseNames=new ArrayList<>();

                result.add(searchEntry);
            }



            Map<CourseSearchEntry,CourseSearchEntryProperty>propertyMap=getEntryProperty(result,studentId);
            result=new ArrayList<>();
            for (CourseSearchEntryProperty csep:propertyMap.values()){
                //System.out.println(csep);



                if((ignoreConflict && csep.isHasConflict) ||(ignoreFull && csep.isFull)
                        ||(ignorePassed && csep.isHasPassed)||
                        (ignoreMissingPrerequisites && csep.isPrerequisitesMissing)){
                    continue;
                }
                result.add(csep.entry);
            }

            result.sort((r1,r2)->{
                if(r1.course.id.compareTo(r2.course.id)!=0){
                    return r1.course.id.compareToIgnoreCase(r2.course.id);
                }else {
                    return (r1.course.name+"["+r1.section.name+"]").compareToIgnoreCase(r2.course.name+"["+r2.section.name+"]");
                }
            });



            List<CourseSearchEntry> finalResult=new ArrayList<>();
            for (int i = pageIndex*pageSize; i < (pageIndex+1)*pageSize && i< result.size(); i++) {
                finalResult.add(result.get(i));
            }

            return finalResult;
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    private static Map<CourseSearchEntry,CourseSearchEntryProperty> getEntryProperty(List<CourseSearchEntry> result,int studentId){
        Map<CourseSearchEntry,CourseSearchEntryProperty> entryPropertyMap=new HashMap<>();
        for (CourseSearchEntry entry:result){
            CourseSearchEntryProperty property=new CourseSearchEntryProperty(entry,studentId);
            property.checkAll();
            entryPropertyMap.put(entry,property);
        }
        return entryPropertyMap;
    }

    private static class CourseSectionNode{
        public Course c;
        public CourseSection cs;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseSectionNode that = (CourseSectionNode) o;
            return Objects.equals(c, that.c) && Objects.equals(cs, that.cs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c, cs.id,cs.name);
        }
    }

    private static class CourseSearchEntryProperty{
        int studentId;
        boolean isFull=false;
        boolean isHasConflict=false;
        boolean isHasPassed=false;
        boolean isPrerequisitesMissing=false;
        CourseSearchEntry entry;

        public CourseSearchEntryProperty(CourseSearchEntry entry,int sid){
            this.entry=entry;
            this.studentId=sid;
        }

        public void checkIsFull(){
            try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
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
            try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
                int sectionId=entry.section.id;

                String sql="select sameTimeCourse.sectionid from( " +
                        "select otherClasses.*from " +
                        "(select c.*,cs.semesterid " +
                        "from coursesection as cs " +
                        "inner join coursesectionclass c on cs.sectionid = c.sectionid " +
                        "where cs.sectionid=?) as currentCourseSection " +
                        "inner join coursesectionclass as otherClasses " +
                        "    on otherClasses.classbegin<=currentCourseSection.classbegin " +
                        "and otherClasses.classend>=currentCourseSection.classend " +
                        "and otherClasses.dayofweek=currentCourseSection.dayofweek " +
                        "inner join coursesection as cs " +
                        "   on cs.sectionid=otherClasses.sectionid" +
                        "   and cs.semesterid=currentCourseSection.semesterid" +
                        ") as sameTimeCourse " +
                        "inner join (select cs1.studentid, sectionid from studentcourseselection as cs1 " +
                        "union all (select cs2.studentid, sectionid from student100course as cs2)" +
                        "union all (select cs3.studentid, sectionid from studentpfcourse as cs3)) as selection " +
                        "on selection.sectionid=sameTimeCourse.sectionid " +
                        "where selection.studentid=?";
                PreparedStatement p=conn.prepareStatement(sql);

                p.setInt(1,sectionId);
                p.setInt(2,studentId);

                ResultSet rs=p.executeQuery();

                this.isHasConflict=rs.next();
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

        public void checkAll(){
            checkIsFull();
            checkIsHasConflict();
            checkIsHasPassed();
            checkIsPrerequisitesMissing();
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
}
