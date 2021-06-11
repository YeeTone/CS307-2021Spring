package reference.util;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
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

        try(Connection conn= SQLDataSource.getInstance().getSQLConnection()){
            String sql;
            PreparedStatement p;
            if(searchClassLocations ==null){
                sql="with allSec as (select cs.sectionid from course as c " +
                        "left outer join coursesection cs on c.courseid = cs.courseid " +
                        "left outer join coursesectionclass c2 on cs.sectionid = c2.sectionid " +
                        "inner join instructor i on i.userid = c2.instructorid " +
                        "left outer join studentcourseselection as scs on scs.sectionid=cs.sectionid " +
                        "where (c.courseid like('%'||?||'%') or ? is null) " +
                        "and (c.coursename||'['||cs.sectionname||']' like '%'||?||'%' or ? is null)" +
                        "and (? is null or i.firstname||i.lastname like('%'||?||'%') " +
                        "or(i.firstname||' '||i.lastname like('%'||?||'%'))" +
                        "or i.firstname like('%'||?||'%') or i.lastname like('%'||?||'%'))" +
                        "and (? is null or c2.dayofweek=?) " +
                        "and (? is null or ? between c2.classbegin and c2.classend)" +
                        "and (cs.semesterid=?) " +
                        //"and (? is null or c2.location like('%'||?||'%')) "+
                        "group by c.courseid, c.coursename, c.credit, c.classhour, c.ispf, cs.sectionid, cs.sectionname, " +
                        "cs.totalcapacity, c2.classid, i.userid, " +
                        "c2.dayofweek, c2.classbegin, c2.classend, c2.location) " +
                        "select c3.courseid,c3.coursename,c3.credit,c3.classhour,c3.ispf," +
                        "cs1.sectionid,cs1.sectionname,cs1.totalcapacity,count(scs.studentid) as cnt," +
                        "c4.classid,i2.userid,getfullname(i2.firstname,i2.lastname) as fullName," +
                        "c4.dayofweek,array_agg(c4.week)as arr,c4.classbegin,c4.classend,c4.location" +
                        " from allSec " +
                        "inner join coursesection as cs1 on cs1.sectionid=allSec.sectionid " +
                        "inner join course c3 on c3.courseid = cs1.courseid " +
                        "left outer join coursesectionclass c4 on cs1.sectionid = c4.sectionid " +
                        "left outer join studentcourseselection scs on scs.sectionid=allSec.sectionid " +
                        "left outer join instructor i2 on c4.instructorid = i2.userid " +
                        "where cs1.sectionid=allSec.sectionid " +
                        "group by c3.courseid, c3.coursename, c3.credit, c3.classhour, c3.ispf, cs1.sectionid, cs1.sectionname, " +
                        "cs1.totalcapacity, c4.classid, i2.userid, getfullname(i2.firstname,i2.lastname)," +
                        " c4.dayofweek, c4.classbegin, c4.classend, c4.location";

            }else {
                sql="with allSec as (select cs.sectionid from course as c " +
                        "left outer join coursesection cs on c.courseid = cs.courseid " +
                        "left outer join coursesectionclass c2 on cs.sectionid = c2.sectionid " +
                        "inner join instructor i on i.userid = c2.instructorid " +
                        "left outer join studentcourseselection as scs on scs.sectionid=cs.sectionid " +
                        "where (c.courseid like('%'||?||'%') or ? is null) " +
                        "and (c.coursename||'['||cs.sectionname||']' like '%'||?||'%' or ? is null)" +
                        "and (? is null or i.firstname||i.lastname like('%'||?||'%') " +
                        "or(i.firstname||' '||i.lastname like('%'||?||'%'))" +
                        "or i.firstname like('%'||?||'%') or i.lastname like('%'||?||'%'))" +
                        "and (? is null or c2.dayofweek=?) " +
                        "and (? is null or ? between c2.classbegin and c2.classend)" +
                        "and (cs.semesterid=?) " +
                        "and (? is null or c2.location like('%'||?||'%')) "+
                        "group by c.courseid, c.coursename, c.credit, c.classhour, c.ispf, cs.sectionid, cs.sectionname, " +
                        "cs.totalcapacity, c2.classid, i.userid, " +
                        "c2.dayofweek, c2.classbegin, c2.classend, c2.location) " +
                        "select c3.courseid,c3.coursename,c3.credit,c3.classhour,c3.ispf," +
                        "cs1.sectionid,cs1.sectionname,cs1.totalcapacity,count(scs.studentid) as cnt," +
                        "c4.classid,i2.userid,getfullname(i2.firstname,i2.lastname) as fullName," +
                        "c4.dayofweek,array_agg(c4.week)as arr,c4.classbegin,c4.classend,c4.location" +
                        " from allSec " +
                        "inner join coursesection as cs1 on cs1.sectionid=allSec.sectionid " +
                        "inner join course c3 on c3.courseid = cs1.courseid " +
                        "left outer join coursesectionclass c4 on cs1.sectionid = c4.sectionid " +
                        "left outer join studentcourseselection scs on scs.sectionid=allSec.sectionid " +
                        "left outer join instructor i2 on c4.instructorid = i2.userid " +
                        "where cs1.sectionid=allSec.sectionid " +
                        "group by c3.courseid, c3.coursename, c3.credit, c3.classhour, c3.ispf, cs1.sectionid, cs1.sectionname, " +
                        "cs1.totalcapacity, c4.classid, i2.userid, getfullname(i2.firstname,i2.lastname)," +
                        " c4.dayofweek, c4.classbegin, c4.classend, c4.location";

                /*sql="select c.courseid, c.coursename, c.credit, c.classhour, c.ispf," +
                        "cs.sectionid,cs.sectionname,cs.totalcapacity,count(scs.studentid) as cnt," +
                        "c2.classid,i.userid,getfullname(i.firstname,i.lastname) as fullName,c2.dayofweek," +
                        "array_agg(c2.week) as arr,c2.classbegin,c2.classend,c2.location from course as c " +
                        "left outer join coursesection cs on c.courseid = cs.courseid " +
                        "left outer join coursesectionclass c2 on cs.sectionid = c2.sectionid " +
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
                        "c2.dayofweek, c2.classbegin, c2.classend, c2.location";*/
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
            }
            else {
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

            Map<CourseSearchEntry,CourseSearchEntryProperty>propertyMap=
                    getEntryProperty(result,studentId,searchCourseType);

            /*if(studentId==11713020){
                System.out.println(studentId);
            }*/

            result=new ArrayList<>();

            for (CourseSearchEntryProperty csep:propertyMap.values()){
                //System.out.println(csep);

                if((ignoreConflict && csep.isHasConflict) ||(ignoreFull && csep.isFull)
                        ||(ignorePassed && csep.isHasPassed)||
                        (ignoreMissingPrerequisites && csep.isPrerequisitesMissing)
                        ||(!csep.isAcceptable4CourseType)){
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
        }
        catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    private static Map<CourseSearchEntry,CourseSearchEntryProperty> getEntryProperty(List<CourseSearchEntry> result,
                                                                                     int studentId, StudentService.CourseType type){
        Map<CourseSearchEntry,CourseSearchEntryProperty> entryPropertyMap=new HashMap<>();


        for (CourseSearchEntry entry:result){

            CourseSearchEntryProperty property=new CourseSearchEntryProperty(entry,studentId);

            property.checkAll(type);

            entryPropertyMap.put(entry,property);




            /*CourseSearchEntryProperty origin= entryPropertyMap.get(entry);
            if(origin==null){
                entryPropertyMap.put(entry,property);
            }else {
                origin.isAcceptable4CourseType= origin.isAcceptable4CourseType && property.isAcceptable4CourseType;
                origin.isFull= origin.isFull|| property.isFull;
                origin.isHasConflict= origin.isHasConflict||property.isHasConflict;
                origin.isHasPassed= origin.isHasPassed|| property.isHasPassed;
            }*/
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
}


