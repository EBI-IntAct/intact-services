package uk.ac.ebi.intact.protein_update;

import uk.ac.ebi.intact.uniprot.service.SimpleUniprotRemoteService;

/**
 * Created by anjali on 22/09/16.
 */
public class ProteinUpdateTest {

    public static void main(String[] args) {
        SimpleUniprotRemoteService simpleUniprotRemoteService=new SimpleUniprotRemoteService();
        simpleUniprotRemoteService.retrieve("P07260");
    }
}
