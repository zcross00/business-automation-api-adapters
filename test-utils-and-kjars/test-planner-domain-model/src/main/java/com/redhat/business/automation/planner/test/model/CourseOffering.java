package com.redhat.business.automation.planner.test.model;

import java.io.Serializable;
import java.util.Set;

public class CourseOffering implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Set<Tag> tags;

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags( Set<Tag> tags ) {
        this.tags = tags;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( tags == null ) ? 0 : tags.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        CourseOffering other = (CourseOffering) obj;
        if ( id == null ) {
            if ( other.id != null ) return false;
        } else if ( !id.equals( other.id ) ) return false;
        if ( name == null ) {
            if ( other.name != null ) return false;
        } else if ( !name.equals( other.name ) ) return false;
        if ( tags == null ) {
            if ( other.tags != null ) return false;
        } else if ( !tags.equals( other.tags ) ) return false;
        return true;
    }

    @Override
    public String toString() {
        return "CourseOffering [id=" + id + ", name=" + name + ", tags=" + tags + "]";
    }
}