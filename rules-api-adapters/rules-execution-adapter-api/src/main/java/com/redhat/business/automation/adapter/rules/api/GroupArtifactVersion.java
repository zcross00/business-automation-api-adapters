package com.redhat.business.automation.adapter.rules.api;

import java.util.stream.Stream;

import com.google.common.base.Strings;

public class GroupArtifactVersion {

    private final String group;
    private final String artifact;
    private final String version;

    public GroupArtifactVersion( String group, String artifact, String version ) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    public boolean isValid() {
        return !Stream.of( group, artifact, version ).anyMatch( Strings::isNullOrEmpty );
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( artifact == null ) ? 0 : artifact.hashCode() );
        result = prime * result + ( ( group == null ) ? 0 : group.hashCode() );
        result = prime * result + ( ( version == null ) ? 0 : version.hashCode() );
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
        GroupArtifactVersion other = (GroupArtifactVersion) obj;
        if ( artifact == null ) {
            if ( other.artifact != null )
                return false;
        } else if ( !artifact.equals( other.artifact ) )
            return false;
        if ( group == null ) {
            if ( other.group != null )
                return false;
        } else if ( !group.equals( other.group ) )
            return false;
        if ( version == null ) {
            if ( other.version != null )
                return false;
        } else if ( !version.equals( other.version ) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "GroupArtifactVersion [group=" + group + ", artifact=" + artifact + ", version=" + version + "]";
    }

}