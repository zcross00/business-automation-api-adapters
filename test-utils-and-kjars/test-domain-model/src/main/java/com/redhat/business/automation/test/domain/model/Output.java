package com.redhat.business.automation.test.domain.model;

import java.io.Serializable;

public class Output implements Serializable {

    private static final long serialVersionUID = 6670062674397234400L;

    private long count;

    public Output( long count ) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount( long count ) {
        this.count = count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) ( count ^ ( count >>> 32 ) );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Output other = (Output) obj;
        if ( count != other.count )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Output [count=" + count + "]";
    }

}