package uk.ac.ebi.intact.service;

import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class can assign IMEx identifiers to a selection of publication acs in a file (one ac per line)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/04/12</pre>
 */

public class ImexPublicationSelectionAssigner {

    public static void main( String[] args )
    {
        String localTrustStore = System.getProperty( "javax.net.ssl.trustStore" );
        String localTrustStorePwd = System.getProperty( "javax.net.ssl.keyStorePassword" );
        if(localTrustStore==null) {
            System.out.println( "It appears you haven't setup a local trust store (other than the one embedded in the JDK)." +
                    "\nShould you want to specify one, use: -Djavax.net.ssl.trustStore=<path.to.keystore> " +
                    "\nAnd if it is password protected, use: -Djavax.net.ssl.keyStorePassword=<password>" );
        } else {
            System.out.println( "Using local trust store: " + localTrustStore + (localTrustStorePwd == null ? " (no password set)" : " (with password set)" ) );
        }

        // two possible arguments
        if( args.length != 1 ) {
            System.err.println( "Usage: selectionAssigner <inputFile>" );
            System.exit( 1 );
        }
        final String fileInputName = args[0];

        System.out.println("File containing publication acs to assign = " + fileInputName);

        IntactContext.initContext(new String[]{"/META-INF/jpa-imex-assigner.spring.xml", "/META-INF/imex-assigner.spring.xml"});

        try {
            System.out.println("Reading file containing publication acs to assign...");
            List<String> publicationAcs = new ArrayList<String>();

            File inputFile = new File(fileInputName);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String line = reader.readLine();

            while (line != null){
                publicationAcs.add(line);
                line = reader.readLine();
            }

            reader.close();

            ImexCentralManager ia = (ImexCentralManager)
                    IntactContext.getCurrentInstance().getSpringContext().getBean("imexCentralManager");
            ia.registerListenersIfNotDoneYet();

            System.out.println("folder where are the log files = " + ia.getImexUpdateConfig().getUpdateLogsDirectory().getAbsolutePath());

            System.out.println("Starting the IMEx assigner on a selection of publication in " + fileInputName);
            
            for (String ac : publicationAcs){
                try {
                    System.out.println("Assign IMEx to publication " + ac);

                    ia.assignImexAndUpdatePublication(ac);
                } catch (PublicationImexUpdaterException e) {
                    e.printStackTrace();
                } catch (ImexCentralException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible to find the input file or log file repository.");
            e.printStackTrace();
        }                

        System.out.println("Finished the selection IMEx assigner.");
    }
}
