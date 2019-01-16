package com.redhat.business.automation.adapter.embedded.common;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.business.automation.adapter.rules.api.GroupArtifactVersion;

/**
 * 
 * Provides shared methods between Drools, jBPM, and OptaPlanner for accessing KieModules via KieScanner
 *
 */
public class KieScannerClient {

    private static final Logger LOG = LoggerFactory.getLogger( KieScannerClient.class );

    public KieContainer loadNewKieContainer( GroupArtifactVersion gav ) throws EmbeddedKieScannerException {
        try {
            ReleaseId releaseId = KieServices.Factory.get().newReleaseId( gav.getGroup(), gav.getArtifact(), gav.getVersion() );
            KieContainer container = KieServices.Factory.get().newKieContainer( releaseId );
            KieScanner scanner = KieServices.Factory.get().newKieScanner( container );
            scanner.scanNow();
            scanner.stop();
            return container;
        } catch ( RuntimeException e ) {
            LOG.error( "Unable to load rules for " + gav.toString() );
            throw new EmbeddedKieScannerException( "There was an error loading the dynamic rules artifact " + gav.toString(), e );
        }
    }

}
