package JsonParse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(
                new FileReader("D:\\计算机\\数据库\\Project\\release of project 1\\data\\course_info.json"));
        String content = reader.readLine();
        //System.out.println("content = " + content);

        // Gson is an example
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<List<Course>>() {
        }.getType();
        List<Course> courses = gson.fromJson(content, type);

        for(Course c:courses){
            c.resetPrerequisite();
            c.changeIntoPostFix();
            c.setPreTree();
            System.out.println(Arrays.toString(c.getPreSplit()));
            System.out.println(Arrays.toString(c.getPrePostfix()));
        }

        System.out.println(gson.toJson(courses));

        /*System.out.println("sum = " + sum);
        System.out.println("courses = " + courses.size());
        System.exit(0);*/

        DatabaseManipulation dm =
                new DatabaseManipulation();

        dm.openDatasource();

        int totalWeek=0;
        for (Course c:courses){
            for (ClassList cl:c.getClassList()){
                totalWeek+=16;
            }
        }


        //dm.checkIfAllCourse_ClassEnterDB(courses);

        /*for (Course c : courses) {
            try{
                String[]teacher=c.getTeacher().trim().split(",");
                for (String t:teacher){
                    dm.addOneTeacher(t);
                }
            }catch (NullPointerException ignored){

            }

        }*/

        /*for (Course c:courses){
            int[]weekCount=new int[16];
            for (int i=0;i<c.getClassList().length;i++){
                for (int j = 0; j < c.getClassList()[i].getWeekList().length; j++) {
                    weekCount[c.getClassList()[i].getWeekList()[j]-1]++;
                }
            }
            System.out.print(c.getCourseId());
            System.out.println("Arrays.toString(weekCount) = " + Arrays.toString(weekCount));
        }*/

        /*for (Course c:courses){
            for (ClassList cl:c.getClassList()){
                dm.addOneLocation(cl.getLocation());
            }
        }*/

        /*for (Course c:courses){
            dm.addOneCourseDept(c.getCourseDept());
        }*/

        /*for (Course c : courses) {
            dm.addOneCourse(c);
        }*/

        /*for (Course c : courses) {
            dm.addClassWeekList(c);
        }
*/
        /*for (Course c:courses){
            dm.addOneTeacherClass(c);
        }*/


        dm.closeDatasource();
    }
}

class Course {
    // TODO:
    private int totalCapacity;
    private String courseId;
    private String prerequisite;
    private int courseHour;
    private double courseCredit;
    private String courseName;
    private String className;
    private String teacher;
    private String courseDept;
    private ClassList[] classList;
    private String[]preSplit;
    private String[]prePostfix;
    private PrerequisiteTreeNode preTree;

    public String getTeacher(){
        return teacher;
    }

    public String getCourseId() {
        return courseId;
    }

    public ClassList[] getClassList() {
        return classList;
    }

    public String getCourseDept() {
        return courseDept;
    }

    public double getCourseCredit() {
        return courseCredit;
    }

    public int getCourseHour() {
        return courseHour;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getClassName() {
        return className;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public String[] getPreSplit() {
        return preSplit;
    }

    public String[] getPrePostfix() {
        return prePostfix;
    }

    public void resetPrerequisite(){
        if(this.prerequisite!=null&&this.prerequisite.length()!=0){
            this.prerequisite=this.prerequisite.replace('（','(')
                    .replace('）',')').replaceAll("SUSTech English III","SUSTECHENGLISHIII")
            .replaceAll("大学物理 B","大学物理B")
            .replaceAll("化学原理 A","化学原理A")
            .replaceAll("学术英语 III","学术英语III")
            .replaceAll("化学原理 B","化学原理B");

            String[]split=this.prerequisite.split(" ");

            if(split.length==0){
                this.preSplit=null;
            }
            else {
                ArrayList<String>anotherSplit=new ArrayList<>();
                for (String s:split){

                    if(s.length()<=1){
                        continue;
                    }
                    if(s.startsWith("信号和系统")&&split[0].endsWith("(控制工程基础")){
                        System.out.println(s);
                        System.out.println(1);
                    }

                    boolean up_down = s.charAt(s.length() - 1) == ')' && s.charAt(s.length() - 2) != '上' && s.charAt(s.length() - 2) != '下';
                    if(s.charAt(0)=='('){
                        anotherSplit.add("(");
                        if(up_down){
                            anotherSplit.add(s.substring(1,s.length()-1));
                            anotherSplit.add(")");
                        }else {
                            anotherSplit.add(s.substring(1));
                        }

                    }else if(up_down){
                        anotherSplit.add(s.substring(0,s.length()-1));
                        anotherSplit.add(")");
                    }else {
                        anotherSplit.add(s);
                    }
                }
                this.preSplit=new String[anotherSplit.size()];
                this.preSplit=anotherSplit.toArray(this.preSplit);
            }
        }

    }

    public void changeIntoPostFix(){
        if(this.preSplit==null){
            return;
        }

        Stack<String>stack=new Stack<>();
        ArrayList<String>postfix=new ArrayList<>();
        for(String s:preSplit){
            switch (s){
                case "(":{
                    stack.push(s);
                    break;
                }
                case ")":{
                    while (!stack.isEmpty()&&!stack.peek().equals("(")){
                        postfix.add(stack.pop());
                    }
                    if(!stack.isEmpty()&&stack.peek().equals("(")){
                        stack.pop();
                    }
                    break;
                }
                case "或者":{
                    stack.push(s);
                    break;
                }
                case "并且":{
                    while (!stack.isEmpty()&&!stack.peek().equals("(")){
                        postfix.add(stack.pop());
                    }
                    stack.add(s);
                    break;
                }
                default:{
                    postfix.add(s);
                }
            }
        }
        while (!stack.isEmpty()){
            postfix.add(stack.pop());
        }

        this.prePostfix=new String[postfix.size()];

        this.prePostfix=postfix.toArray(this.prePostfix);
    }

    public void setPreTree(){
        if(this.prePostfix==null){
            return;
        }
        this.preTree=PrerequisiteTreeNode.getTree(this.prePostfix);
    }

    public PrerequisiteTreeNode getPreTree() {
        return preTree;
    }
}

class ClassList {
    // TODO: define data-class as the json structure

    private int[] weekList;
    private String location;
    private String classTime;
    private int weekday;

    public String getClassTime() {
        return classTime;
    }

    public int getWeekday() {
        return weekday;
    }

    public int[] getWeekList() {
        return weekList;
    }

    public String getLocation() {
        return location;
    }
}

class PrerequisiteTreeNode{
    private int group;
    private String type;
    private String name;
    private PrerequisiteTreeNode leftChild;
    private PrerequisiteTreeNode rightChild;

    public void setLeftChild(PrerequisiteTreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(PrerequisiteTreeNode rightChild) {
        this.rightChild = rightChild;
    }

    public void setType(String type) {
        switch (type){
            case "并且":{
                this.type="and";
                break;
            }
            case "或者":{
                this.type="or";
                break;
            }
            default:{
                this.name=type;
                this.type="course";
                break;
            }
        }
    }

    public static boolean isOperand(String s){
        return (s.equals("并且")||s.equals("或者"));
    }

    public static PrerequisiteTreeNode getTree(String[]postfix){
        LinkedList<PrerequisiteTreeNode>stack=new LinkedList<>();
        PrerequisiteTreeNode node = null;
        for (String s : postfix) {
            if (!isOperand(s)) {
                node = new PrerequisiteTreeNode();
                node.setLeftChild(null);
                node.setRightChild(null);
                node.setType(s);
                stack.push(node);
            } else {
                PrerequisiteTreeNode left = stack.pop();
                PrerequisiteTreeNode right = stack.pop();
                node = new PrerequisiteTreeNode();
                node.setLeftChild(left);
                node.setRightChild(right);
                node.setType(s);
                stack.push(node);
            }
        }

        return stack.getLast();
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public PrerequisiteTreeNode getLeftChild() {
        return leftChild;
    }

    public PrerequisiteTreeNode getRightChild() {
        return rightChild;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
