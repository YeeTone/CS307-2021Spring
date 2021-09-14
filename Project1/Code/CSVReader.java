package JsonParse;

import java.io.*;
import java.util.*;

public class CSVReader {
    public static void main (String[] args) throws FileNotFoundException {
        try {
            DataManipulation dm = new DataFactory().createDataManipulation(args[0]);
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader("D:\\计算机\\数据库\\Project\\release of project 1\\data\\select_course.csv"));
            String line;

            ArrayList<Course_selection_info>arrayList=new ArrayList<>(10000000);
            ArrayList<Student>students=new ArrayList<>();
            while ((line=reader.readLine()) != null) {
                //String line = reader.readLine();
                //*Student student = new Student(line);
                //College college=College.newInstance(line);//*
//                String[] s = reader.readLine().split(",");
//                String college_info = s[2];
//                String[] depart = college_info.split("\\(");
//                String data = depart[0] +  "," + depart[1].substring(0,depart[1].length()-1);
                //dm.addOneCollege(college);
//                String student_info = s[0] + "," + s[1] + "," + s[3] + "," + dm.FullInformationOfStudentInfo(depart[0]);
                //dm.addOneStudent(student);
                Course_selection_info[]infos=Course_selection_info.infos(line);
                //dm.addOneCourseSelection(Course_selection_info);
                arrayList.addAll(Arrays.asList(infos));
                students.add(new Student(line));

//                String[] Course_selection_info = new String[s.length-4];
//                int index = 0;
//                for (int i = 4; i < s.length; i++) {
//                    Course_selection_info[index++] = s[i] + "," + s[3];
//                }
//
//                for (int i = 0; i < Course_selection_info.length; i++) {
//
//
//                        dm.addOneCourse(Course_selection_info[i]);
//                }
            }

            long t1=System.currentTimeMillis();

            /*for (Course_selection_info info:arrayList){
                info.toUpper();
            }*/


            /*arrayList.sort(Comparator.comparing(Course_selection_info::getStudent_id));*/

            for (Course_selection_info course_selection_info : arrayList) {
                int index=Collections.binarySearch(students,new Student(course_selection_info.getStudent_id(),false),
                        Comparator.comparing(o->o.sid));

            }

            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);


            // dm.addOneCollege("拉文克劳,Ravenclaw");
//            System.out.println(dm.allContinentNames());
//            System.out.println(dm.continentsWithCountryCount());
//            System.out.println(dm.FullInformationOfMoviesRuntime(65, 75));
////            System.out.println(dm.findMovieById(10));
            dm.closeDatasource();
        } catch (IllegalArgumentException | FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class College {
    //    long college_id;
    Long college_id;
    String college_chinese_name;
    String college_english_name;
    /*public College(String line) {
        String[] data;
        data = line.split(",");
        String name = data[2];
        String[] names;
        names = name.split("\\(");
        this.college_chinese_name = names[0];
        this.college_english_name = names[1].substring(0,names.length-1);
    }*/

    static HashMap<String, College> cnNameCollegeHashMap = new HashMap<String, College>();
    private College(String cn,String en){
        this.college_chinese_name=cn;
        this.college_english_name=en;
    }

    public static College newInstance(String line){
        String[] data;
        data = line.split(",");
        String name = data[2];
        String[] names;
        names = name.split("\\(");
        if(!cnNameCollegeHashMap.containsKey(names[0])){
            College c=new College(names[0],names[1]);
            cnNameCollegeHashMap.put(names[0],c);
            return c;
        }else {
            return cnNameCollegeHashMap.get(names[0]);
        }
    }

    public String getCollege_chinese_name() {
        return college_chinese_name;
    }

    public String getCollege_english_name() {
        return college_english_name;
    }
}


class Student {
    String name;
    String gender;
    long college_id;
    String college_name;
    String sid;
    String college_chinese_name;
    public Student(String sid,boolean b){
        this.sid=sid;
    }

    public Student(String line) {
        String[] data;
        data = line.split(",");
        this.name = data[0];
        this.gender = data[1];
        this.sid = data[3];
        this.college_name = data[2];
        String[] data2;
        data2 = data[2].split("\\(");
        this.college_chinese_name = data2[0];
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getSid() {
        return sid;
    }

    public String getCollege_name() {
        return college_name;
    }

    public String getCollege_chinese_name() {
        return college_chinese_name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", sid='" + sid + '\'' +
                '}';
    }
}


class Course_selection_info {
    private String[] courses_id;
    private String student_id;
    public String course_id;
    public Course_selection_info(String line) {
        int count = 0;
        String[] data;
        data = line.split(",");
        courses_id = new String[data.length-4];
        for (int i = 4; i < data.length; i++) {
            this.courses_id[count++] = data[i].toUpperCase().trim();
        }
        this.student_id = data[3];
    }
    public Course_selection_info(String student_id,String course_id){
        this.student_id=student_id;
        this.course_id=course_id;
    }
    public static Course_selection_info[] infos(String line){
        int count = 0;
        String[] data;
        data = line.split(",");
        String[]courses_id = new String[data.length-4];
        String student_id = data[3];

        Course_selection_info[]infos=new Course_selection_info[courses_id.length];
        for (int i = 4 , j = 0; i < data.length; i++,j++) {
            courses_id[count] = data[i].toUpperCase().trim();
            infos[j]=new Course_selection_info(student_id,courses_id[count]);
            count++;
        }
        return infos;
    }

    public String getStudent_id() {
        return student_id;
    }

    public String[] getCourses_id() {
        return courses_id;
    }

    @Override
    public String toString() {
        return "Course_selection_info{" +
                "student_id='" + student_id + '\'' +
                ", course_id='" + course_id + '\'' +
                '}';
    }

    public void toUpper(){
        student_id=student_id.toUpperCase();
    }
}

