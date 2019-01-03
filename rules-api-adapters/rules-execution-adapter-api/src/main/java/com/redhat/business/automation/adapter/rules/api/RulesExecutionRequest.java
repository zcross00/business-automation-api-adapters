package com.redhat.business.automation.adapter.rules.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RulesExecutionRequest {

    private final GroupArtifactVersion groupArtifactVersion;
    private final String ksession;
    private final String processId;
    private final List<Object> facts;
    private final Set<QueryDescriptor> queries;

    private RulesExecutionRequest( Builder builder ) {
        this.ksession = builder.getKsession();
        this.groupArtifactVersion = builder.getGav();
        this.processId = builder.getProcessId();
        this.facts = builder.getFacts();
        this.queries = builder.getQueries();
    }

    public GroupArtifactVersion getGav() {
        return groupArtifactVersion;
    }

    public String getKsession() {
        return this.ksession;
    }

    public String getProcessId() {
        return this.processId;
    }

    public Set<QueryDescriptor> getQueries() {
        return this.queries;
    }

    public List<Object> getFacts() {
        return this.facts;
    }

    public static class Builder {

        private GroupArtifactVersion gav;
        private String ksession;
        private String processId;
        private List<Object> facts = new ArrayList<>();
        private Set<QueryDescriptor> queries = new HashSet<QueryDescriptor>();

        private boolean useGav = false;

        /**
         * 
         * @param group    - maven group id
         * @param artifact - maven artifact id
         * @param version  - the exact version of the Kie Container you want to use, otherwise you can use LATEST to pick the most up to date Kie Container
         */
        public Builder useGAV( String group, String artifact, String version ) {
            this.gav = new GroupArtifactVersion( group, artifact, version );
            this.useGav = true;
            return this;
        }

        public Builder ksession( String ksession ) {
            this.ksession = ksession;
            return this;
        }

        public Builder processId( String processId ) {
            this.processId = processId;
            return this;
        }

        public Builder addFact( Object fact ) {
            this.facts.add( fact );
            return this;
        }

        public Builder addFacts( Collection<Object> facts ) {
            this.facts.addAll( facts );
            return this;
        }

        public Builder addQuery( String queryName, String outputId ) {
            Optional<QueryDescriptor> query = queries.stream().filter( q -> q.getName().equals( queryName ) ).findFirst();
            if ( !query.isPresent() ) {
                this.queries.add( new QueryDescriptor( queryName, outputId ) );
            } else {
                throw new IllegalArgumentException( "A query with name \"" + queryName + "\" has alreadye been registered" );
            }
            return this;
        }

        public GroupArtifactVersion getGav() {
            return gav;
        }

        public String getKsession() {
            return ksession;
        }

        public String getProcessId() {
            return processId;
        }

        public List<Object> getFacts() {
            return facts;
        }

        public Set<QueryDescriptor> getQueries() {
            return queries;
        }

        public RulesExecutionRequest build() {
            if ( ksession == null ) {
                throw new IllegalArgumentException( "KSession ID must be specified for rules execution" );
            }
            if ( useGav ) {
                if ( !gav.isValid() ) {
                    throw new IllegalArgumentException( "Group, artifact, and version MUST be specified when using Maven to resolve rules dynamically" );
                }
            }

            return new RulesExecutionRequest( this );
        }
    }
}