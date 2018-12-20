package com.redhat.business.automation.adapter.rules.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Strings;

public class RulesExecutionRequest {

    private GroupArtifactVersion groupArtifactVersion;
    private String ksession;
    private String processId;
    private Collection<InternalRuleIO> facts;

    private RulesExecutionRequest( Builder builder ) {
        this.ksession = builder.getKsession();
        this.facts = builder.getFacts();
        this.groupArtifactVersion = builder.getGav();
        this.processId = builder.getProcessId();
    }

    public GroupArtifactVersion getGav() {
        return groupArtifactVersion;
    }

    public void setGroupArtifactVersion( GroupArtifactVersion groupArtifactVersion ) {
        this.groupArtifactVersion = groupArtifactVersion;
    }

    public String getKsession() {
        return ksession;
    }

    public void setKsession( String ksession ) {
        this.ksession = ksession;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId( String processId ) {
        this.processId = processId;
    }

    public Collection<InternalRuleIO> getFacts() {
        return facts;
    }

    public void setFacts( Collection<InternalRuleIO> facts ) {
        this.facts = facts;
    }

    public static class Builder {

        private GroupArtifactVersion gav;
        private String ksession;
        private String processId;

        private Collection<InternalRuleIO> facts = new ArrayList<InternalRuleIO>();

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

        public Builder addInOutFact( String identifier, Object fact ) {
            if ( Strings.isNullOrEmpty( identifier ) ) {
                throw new IllegalArgumentException( "Identifier for InOut Fact " + fact + " is null or blank" );
            } else {
                facts.add( new InternalRuleIO( identifier, fact ) );
            }

            return this;
        }

        public Builder addInOnlyFacts( Collection<?> facts ) {
            facts.stream().forEach( fact -> {
                this.facts.add( new InternalRuleIO( fact ) );
            } );

            return this;
        }

        public Builder addInOnlyFact( Object fact ) {
            facts.add( new InternalRuleIO( fact ) );
            return this;
        }

        public Builder addOutOnlyFact( String identifier, Class<?> clazz ) {
            facts.add( new InternalRuleIO( identifier, clazz ) );
            return this;
        }

        public String getKsession() {
            return ksession;
        }

        public GroupArtifactVersion getGav() {
            return this.gav;
        }

        public String getProcessId() {
            return processId;
        }

        public Collection<InternalRuleIO> getFacts() {
            return facts;
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