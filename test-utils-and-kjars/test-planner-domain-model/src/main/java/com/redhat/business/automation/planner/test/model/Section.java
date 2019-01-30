package com.redhat.business.automation.planner.test.model;

import java.io.Serializable;
import java.util.Set;

public class Section implements Serializable {

    private static final long serialVersionUID = 1L;

    private CourseOffering course;
    private Timeslot timeslot;
    private Teacher teacher;
    private Set<Student> students;

    public CourseOffering getCourse() {
        return course;
    }

    public void setCourse( CourseOffering course ) {
        this.course = course;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot( Timeslot timeslot ) {
        this.timeslot = timeslot;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher( Teacher teacher ) {
        this.teacher = teacher;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents( Set<Student> students ) {
        this.students = students;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( course == null ) ? 0 : course.hashCode() );
        result = prime * result + ( ( students == null ) ? 0 : students.hashCode() );
        result = prime * result + ( ( teacher == null ) ? 0 : teacher.hashCode() );
        result = prime * result + ( ( timeslot == null ) ? 0 : timeslot.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Section other = (Section) obj;
        if ( course == null ) {
            if ( other.course != null ) return false;
        } else if ( !course.equals( other.course ) ) return false;
        if ( students == null ) {
            if ( other.students != null ) return false;
        } else if ( !students.equals( other.students ) ) return false;
        if ( teacher == null ) {
            if ( other.teacher != null ) return false;
        } else if ( !teacher.equals( other.teacher ) ) return false;
        if ( timeslot == null ) {
            if ( other.timeslot != null ) return false;
        } else if ( !timeslot.equals( other.timeslot ) ) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Section [course=" + course + ", timeslot=" + timeslot + ", teacher=" + teacher + ", students=" + students + "]";
    }
}