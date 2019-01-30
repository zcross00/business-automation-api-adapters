package com.redhat.business.automation.planner.test.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Timeslot implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime start;
    private LocalDateTime end;

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart( LocalDateTime start ) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd( LocalDateTime end ) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( end == null ) ? 0 : end.hashCode() );
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        result = prime * result + ( ( start == null ) ? 0 : start.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Timeslot other = (Timeslot) obj;
        if ( end == null ) {
            if ( other.end != null ) return false;
        } else if ( !end.equals( other.end ) ) return false;
        if ( id == null ) {
            if ( other.id != null ) return false;
        } else if ( !id.equals( other.id ) ) return false;
        if ( start == null ) {
            if ( other.start != null ) return false;
        } else if ( !start.equals( other.start ) ) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Timeslot [id=" + id + ", start=" + start + ", end=" + end + "]";
    }
}