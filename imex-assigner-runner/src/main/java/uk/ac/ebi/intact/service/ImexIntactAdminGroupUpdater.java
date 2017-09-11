package uk.ac.ebi.intact.service;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.Operation;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.model.Publication;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ReportWriterListener;
import uk.ac.ebi.intact.jami.service.PublicationService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will remove IntAct admin group from some publications.
 * <p>
 * It will add the intact_curators admin group and reset the institution group
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/08/13</pre>
 */

public class ImexIntactAdminGroupUpdater {

    public static void main(String[] args) {
        String localTrustStore = System.getProperty("javax.net.ssl.trustStore");
        String localTrustStorePwd = System.getProperty("javax.net.ssl.keyStorePassword");
        if (localTrustStore == null) {
            System.out.println("It appears you haven't setup a local trust store (other than the one embedded in the JDK)." +
                    "\nShould you want to specify one, use: -Djavax.net.ssl.trustStore=<path.to.keystore> " +
                    "\nAnd if it is password protected, use: -Djavax.net.ssl.keyStorePassword=<password>");
        } else {
            System.out.println("Using local trust store: " + localTrustStore + (localTrustStorePwd == null ? " (no password set)" : " (with password set)"));
        }

        // two possible arguments
        if (args.length != 2) {
            System.err.println("Usage: selectionAssigner <inputFile> <ADMINToRemove>");
            System.exit(1);
        }
        final String fileInputName = args[0];
        final String adminToRemove = args[1];

        System.out.println("File containing publication acs for which we want to reset ADMIN group = " + fileInputName);
        System.out.println("Admin group to remove = " + adminToRemove);

        ApplicationContext ctx = new ClassPathXmlApplicationContext("/META-INF/imex-assigner.spring.xml","/META-INF/jpa-imex-assigner.spring.xml");
        ImexCentralManager ia = (ImexCentralManager) ctx.getBean("imexCentralManager");
        ImexCentralClient client = (ImexCentralClient) ctx.getBean("imexCentralClient");
        PublicationService service = (PublicationService) ctx.getBean("publicationService");

        try {
            System.out.println("Reading file containing publication acs to reset...");
            List<String> publicationAcs = new ArrayList<String>();

            File inputFile = new File(fileInputName);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            try {
                String line = reader.readLine();

                while (line != null) {
                    publicationAcs.add(line);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
            ia.registerListenersIfNotDoneYet();


            System.out.println("folder where are the log files = " + ia.getImexUpdateConfig().getUpdateLogsDirectory().getAbsolutePath());

            System.out.println("Starting the IMEx ADMIN reset a selection of publication in " + fileInputName);

            // Having a look to the documentation of the intact bridges for this method ("Update the publication admin group given a valid pubmed identifier and a valid operator")
            // we can infer that the content of the file are in fact pubmed ids so for the new jami-imex-id we can assume the source is pubmed
            for (String ac : publicationAcs) {
                Publication intact = service.getIntactDao().getPublicationDao().getByPubmedId(ac);

                if (intact != null) {

                    try {
                        System.out.println("Reset ADMIN group from " + ac);
                        System.out.println("Add INTACT CURATOR " + ac);
                        client.updatePublicationAdminGroup(ac, "pubmed", Operation.ADD, "INTACT CURATORS");

                    } catch (BridgeFailedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        System.out.println("drop " + adminToRemove);
                        client.updatePublicationAdminGroup(ac, "pubmed", Operation.DROP, adminToRemove);
                    } catch (BridgeFailedException e) {
                        e.printStackTrace();
                    }

                    try {
                        System.out.println("Synchronize... " + ac);
                        ia.updateIntactPublicationHavingIMEx(service.getIntactDao().getPublicationDao().getByPubmedId(ac).getAc());

                    } catch (PublicationImexUpdaterException e) {
                        e.printStackTrace();
                    } catch (EnricherException e) {
                        e.printStackTrace();
                    }
                }
            }

            ReportWriterListener[] writers = ia.getListenerList().getListeners(ReportWriterListener.class);

            if (writers != null) {
                for (ReportWriterListener writer : writers) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        System.out.println("Impossible to close report listener writers, " + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible to find the input file or log file repository.");
            e.printStackTrace();
        }

        System.out.println("Finished the selection IMEx assigner.");
    }
}
