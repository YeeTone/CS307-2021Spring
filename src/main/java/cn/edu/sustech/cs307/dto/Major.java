package cn.edu.sustech.cs307.dto;

import java.util.Objects;

public class Major {
    public int id;
    public String name;
    public Department department;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Major major = (Major) o;
        return id == major.id && Objects.equals(name, major.name) && Objects.equals(department, major.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, department);
    }
}
