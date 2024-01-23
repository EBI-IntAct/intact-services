package uk.ac.ebi.intact.service;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dbupdate.cv.GlobalCvUpdateRunner;

/**
 * The global cv update runner
 *
 */
public class CvUpdateRunner
{
    public static void main( String[] args )
    {
        IntactContext.initContext(new String[]{"/META-INF/jpa-cv-update.spring.xml", "/META-INF/cvupdaterunner.spring.xml",
                "/META-INF/beanscv.spring.xml"});

        GlobalCvUpdateRunner cm = (GlobalCvUpdateRunner)
                IntactContext.getCurrentInstance().getSpringContext().getBean("globalCvUpdateRunner");

        System.out.println("getOntologyIDs: " + cm.getCvUpdateManager().getMiOntologyManager().getOntologyIDs());
        try {
            System.out.println("isUpToDate: " + cm.getCvUpdateManager().getMiOntologyManager().isUpToDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        cm.getCvUpdateManager().getMiOntologyManager().getOntologyIDs().forEach(ontology -> {
            System.out.println("getOntologyIDs(" + ontology + "): " + cm.getCvUpdateManager().getMiOntologyManager().getOntologyAccess(ontology).getDatabaseName());
        });

        System.out.println( "folder where are the log files = " + cm.getCvUpdateManager().getReportDirectory().getAbsolutePath() );

        System.out.println("Starting the global update for PSI-MI and PSI-MOD");
        cm.updateAll();

        System.out.println("Finished the global cv update.");
    }
}
