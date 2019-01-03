package com.redhat.business.automation.adapter.rules.api;

public class QueryDescriptor {

    private final String name;
    private final String outputId;

    public QueryDescriptor( String name, String outputId ) {
        this.name = name;
        this.outputId = outputId;
    }

    public String getName() {
        return name;
    }

    public String getOutputId() {
        return outputId;
    }

    @Override
    public String toString() {
        return "QueryDescriptor [name=" + name + ", outputId=" + outputId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( outputId == null ) ? 0 : outputId.hashCode() );
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
        QueryDescriptor other = (QueryDescriptor) obj;
        if ( name == null ) {
            if ( other.name != null )
                return false;
        } else if ( !name.equals( other.name ) )
            return false;
        if ( outputId == null ) {
            if ( other.outputId != null )
                return false;
        } else if ( !outputId.equals( other.outputId ) )
            return false;
        return true;
    }

}