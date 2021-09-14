package JsonParse;

public interface DataManipulation {

    public void openDatasource();
    public void closeDatasource();
    public int addOneTeacher(String teacher);
    public int addOneCourse(Course c);
    public int addOneLocation(String location);
    public int addOneCourseDept(String courseDept);
    public int addOneCollege(College college);
    public int addOneStudent(Student student);
    public int addOneCourseSelection(Course_selection_info course_selection_info);
    public String continentsWithCountryCount();
    public void addOneCoursePrerequisite(Course c,PrerequisiteTreeNode root);
}
