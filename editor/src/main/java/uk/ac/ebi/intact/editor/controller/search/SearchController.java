package uk.ac.ebi.intact.editor.controller.search;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hibernate.Hibernate;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.application.SearchThreadConfig;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Search controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
@Scope( "conversation.access" )
@ConversationName ("search")
@SuppressWarnings("unchecked")
public class SearchController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( SearchController.class );

    private String query;
    private String quickQuery;

    private int threadTimeOut = 5;

    private AnnotatedObject annotatedObject;

    @Autowired
    private DaoFactory daoFactory;

    private LazyDataModel<Publication> publications;

    private LazyDataModel<Experiment> experiments;

    private LazyDataModel<Interaction> interactions;

    private LazyDataModel<Interactor> molecules;

    private LazyDataModel<CvObject> cvobjects;

    private LazyDataModel<Feature> features;

    private LazyDataModel<BioSource> organisms;

    private LazyDataModel<Component> participants;

    private List<Future> runningTasks;

    //////////////////
    // Constructors

    public SearchController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return annotatedObject;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        this.annotatedObject = annotatedObject;
    }

    ///////////////
    // Actions

    public void searchIfQueryPresent(ComponentSystemEvent evt) {
        if (query != null && !query.isEmpty()) {
            doSearch();
        }
    }

    @Transactional( value = "transactionManager", readOnly = true )
    public String doQuickSearch() {
        this.query = quickQuery;
        return doSearch();
    }

    public void clearQuickSearch(ActionEvent evt) {
        this.quickQuery = null;
        this.query = null;
    }

    @Transactional( value = "transactionManager", readOnly = true )
    public String doSearch() {

        log.info( "Searching for '" + query + "'..." );

        if ( !StringUtils.isEmpty( query ) ) {
            final String originalQuery = query;
            query = query.toLowerCase().trim();

            String q = query;

            q = q.replaceAll( "\\*", "%" );
            q = q.replaceAll( "\\?", "%" );
            if ( !q.startsWith( "%" ) ) {
                q = "%" + q;
            }
            if ( !q.endsWith( "%" ) ) {
                q = q + "%";
            }

            if ( !query.equals( q ) ) {
                log.info( "Updated query: '" + q + "'" );
            }

            // TODO implement simple prefix for the search query so that one can aim at an AC, shortlabel, PMID...

            // Note: the search is NOT case sensitive !!!
            // Note: the search includes wildcards automatically
            final String finalQuery = q;

            SearchThreadConfig threadConfig = (SearchThreadConfig) getSpringContext().getBean("searchThreadConfig");

            ExecutorService executorService = threadConfig.getExecutorService();

            if (runningTasks == null){
                runningTasks = new ArrayList<Future>();
            }
            else {
                runningTasks.clear();
            }

            Runnable runnablePub = new Runnable() {
                @Override
                public void run() {
                   loadPublication( finalQuery, originalQuery );
                }
            };

            Runnable runnableExp = new Runnable() {
                @Override
                public void run() {
                   loadExperiments( finalQuery, originalQuery );
                }
            };

            Runnable runnableInt = new Runnable() {
                @Override
                public void run() {
                   loadInteractions( finalQuery, originalQuery );
                }
            };

            Runnable runnableMol = new Runnable() {
                @Override
                public void run() {
                   loadMolecules( finalQuery, originalQuery );
                }
            };

            Runnable runnableCvs = new Runnable() {
                @Override
                public void run() {
                   loadCvObjects( finalQuery, originalQuery );
                }
            };

            Runnable runnableFeatures = new Runnable() {
                @Override
                public void run() {
                   loadFeatures( finalQuery, originalQuery );
                }
            };

            Runnable runnableOrganisms = new Runnable() {
                @Override
                public void run() {
                   loadOrganisms( finalQuery, originalQuery );
                }
            };

            Runnable runnableComponents = new Runnable() {
                @Override
                public void run() {
                    loadParticipants(finalQuery, originalQuery);
                }
            };

            runningTasks.add(executorService.submit(runnablePub));
            runningTasks.add(executorService.submit(runnableExp));
            runningTasks.add(executorService.submit(runnableInt));
            runningTasks.add(executorService.submit(runnableMol));
            runningTasks.add(executorService.submit(runnableCvs));
            runningTasks.add(executorService.submit(runnableFeatures));
            runningTasks.add(executorService.submit(runnableOrganisms));
            runningTasks.add(executorService.submit(runnableComponents));

            checkAndResumeTasks();
        } else {
            resetSearchResults();
        }

        return "search.results";
    }

    private void checkAndResumeTasks() {

        for (Future f : runningTasks){
            try {
                f.get(threadTimeOut, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("The editor search task was interrupted, we cancel the task.", e);
                if (!f.isCancelled()){
                    f.cancel(true);
                }
            } catch (ExecutionException e) {
                log.error("The editor search task could not be executed, we cancel the task.", e);
                if (!f.isCancelled()){
                    f.cancel(true);
                }
            } catch (TimeoutException e) {
                log.error("Service task stopped because of time out " + threadTimeOut + "seconds.", e);

                if (!f.isCancelled()){
                    f.cancel(true);
                }
            }
        }

        runningTasks.clear();
    }

    private void resetSearchResults() {
        publications = null;
        experiments = null;
        interactions = null;
        molecules = null;
        cvobjects = null;
    }

    public boolean isEmptyQuery() {
        return StringUtils.isEmpty( query );
    }

    public boolean hasNoResults() {
        return publications == null || (publications.getRowCount() == 0)
                && (experiments != null && experiments.getRowCount() == 0)
                && (interactions != null && interactions.getRowCount() == 0)
                && (molecules != null && molecules.getRowCount() == 0)
                && (cvobjects != null && cvobjects.getRowCount() == 0)
                && (features != null && features.getRowCount() == 0)
                && (organisms != null && organisms.getRowCount() == 0)
                && (participants != null && participants.getRowCount() == 0);

    }

    public boolean matchesSingleType() {
        int matches = 0;

        if ( publications != null && publications.getRowCount() > 0 ) matches++;
        if ( experiments != null && experiments.getRowCount() > 0 ) matches++;
        if ( interactions != null && interactions.getRowCount() > 0 ) matches++;
        if ( molecules != null && molecules.getRowCount() > 0 ) matches++;
        if ( cvobjects != null && cvobjects.getRowCount() > 0 ) matches++;
        if ( features != null && features.getRowCount() > 0 ) matches++;
        if ( organisms != null && organisms.getRowCount() > 0 ) matches++;
        if ( participants != null && participants.getRowCount() > 0 ) matches++;

        return matches == 1;
    }

    private void loadCvObjects( String query, String originalQuery ) {

        log.info( "Searching for CvObject matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // all cvobjects
        cvobjects = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                              "select distinct i " +
                                                              "from CvObject i left join i.xrefs as x " +
                                                              "where    ( i.ac = :ac " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(i.fullName) like :query " +
                                                              "      or lower(i.identifier) like :query " +
                                                              "      or lower(x.primaryId) like :query ) ",

                                                              "select count(distinct i) " +
                                                              "from CvObject i left join i.xrefs as x " +
                                                              "where   (i.ac = :ac " +
                                                              "      or lower(i.identifier) like :query " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(i.fullName) like :query " +
                                                              "      or lower(x.primaryId) like :query )",

                                                              params, "i", "updated", false);

        log.info( "CvObject found: " + cvobjects.getRowCount() );
    }

    private void loadMolecules( String query, String originalQuery ) {

        log.info( "Searching for Molecules matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // all molecules but interactions
        molecules = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                              "select distinct i " +
                                                              "from InteractorImpl i left join i.xrefs as x " +
                                                              "where    ( i.ac = :ac " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(x.primaryId) like :query ) " +
                                                              "      and i.cvInteractorType.identifier <> 'MI:0317'",

                                                              "select count(distinct i) " +
                                                              "from InteractorImpl i left join i.xrefs as x " +
                                                              "where   (i.ac = :ac " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(x.primaryId) like :query )" +
                                                              "     and i.cvInteractorType.identifier <> 'MI:0317'",

                                                              params, "i", "updated", false );

        log.info( "Molecules found: " + molecules.getRowCount() );
    }

    public int countInteractionsByMoleculeAc( Interactor molecule ) {
        return getDaoFactory().getInteractorDao().countInteractionsForInteractorWithAc( molecule.getAc() );
    }

    public int countFeaturesByParticipantAc( Component comp ) {
        return getDaoFactory().getFeatureDao().getByComponentAc(comp.getAc()).size();
    }

    public CvExperimentalRole getExperimentalRoleForParticipantAc( Component comp ) {
        return getDaoFactory().getComponentDao().getByAc(comp.getAc()).getCvExperimentalRole();
    }

    public String getIdentityXref( Interactor molecule ) {
        // TODO handle multiple identities (return xref and iterate to display them all)
        Collection<InteractorXref> xrefs;

        if (!Hibernate.isInitialized(molecule.getXrefs())) {
            Interactor reloadedMolecule = getDaoFactory().getInteractorDao().getByAc(molecule.getAc());
            xrefs = AnnotatedObjectUtils.searchXrefsByQualifier( reloadedMolecule, CvXrefQualifier.IDENTITY_MI_REF );
        } else {
            xrefs = AnnotatedObjectUtils.searchXrefsByQualifier( molecule, CvXrefQualifier.IDENTITY_MI_REF );
        }


        if ( xrefs.isEmpty() ) {
            return "-";
        }
        return xrefs.iterator().next().getPrimaryId();
    }

    private void loadInteractions( String query, String originalQuery ) {

        log.info( "Searching for Interactions matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // Load experiment eagerly to avoid LazyInitializationException when redering the view
        interactions = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct i " +
                                                                 "from InteractionImpl i left join i.xrefs as x " +
                                                                 "where    i.ac = :ac " +
                                                                 "      or lower(i.shortLabel) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 "select count(i.ac) " +
                                                                 "from InteractionImpl i left join i.xrefs as x " +
                                                                 "where    i.ac = :ac " +
                                                                 "      or lower(i.shortLabel) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params, "i", "updated", false );

        log.info( "Interactions found: " + interactions.getRowCount() );
    }

    public Experiment getFirstExperiment( Interaction interaction ) {
        Collection<Experiment> exps;

        if (Hibernate.isInitialized(interaction.getExperiments())) {
            exps = interaction.getExperiments();
        } else {
            Query query = getDaoFactory().getEntityManager().createQuery("select e from Experiment e join e.interactions as i " +
                                                                  "where i.ac = :interactionAc");
            query.setParameter("interactionAc", interaction.getAc());

            exps = query.getResultList();
        }

        if (exps.isEmpty()) {
            return null;
        }

        return exps.iterator().next();
    }

    private void loadExperiments( String query, String originalQuery ) {

        log.info( "Searching for experiments matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        experiments = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                "select distinct e " +
                                                                "from Experiment e left join e.xrefs as x " +
                                                                "where    e.ac = :ac " +
                                                                "      or lower(e.shortLabel) like :query " +
                                                                "      or lower(x.primaryId) like :query ",

                                                                "select count(distinct e) " +
                                                                "from Experiment e left join e.xrefs as x " +
                                                                "where    e.ac = :ac " +
                                                                "      or lower(e.shortLabel) like :query " +
                                                                "      or lower(x.primaryId) like :query ",

                                                                params, "e", "updated", false );

        log.info( "Experiment found: " + experiments.getRowCount() );
    }

    private void loadPublication( String query, String originalQuery ) {
        log.info( "Searching for publications matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // TODO add: author
        publications = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct p " +
                                                                 "from Publication p left join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 "select count(distinct p) " +
                                                                 "from Publication p left join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params, "p", "updated", false );

        log.info( "Publications found: " + publications.getRowCount() );
    }

    private void loadFeatures( String query, String originalQuery ) {
        log.info( "Searching for features matching '" + query + "' or AC '"+originalQuery+"'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        features = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct p " +
                                                                 "from Feature p left join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 "select count(distinct p) " +
                                                                 "from Feature p left join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params, "p", "updated", false);

        log.info( "Features found: " + features.getRowCount() );
    }

    private void loadOrganisms( String query, String originalQuery ) {
        log.info( "Searching for organisms matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // Load experiment eagerly to avoid LazyInitializationException when redering the view
        organisms = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct b " +
                                                                 "from BioSource b " +
                                                                 "where    b.ac = :ac " +
                                                                 "      or lower(b.shortLabel) like :query " +
                                                                 "      or lower(b.taxId) like :query ",

                                                                 "select count(distinct b) " +
                                                                 "from BioSource b " +
                                                                 "where    b.ac = :ac " +
                                                                 "      or lower(b.shortLabel) like :query " +
                                                                 "      or lower(b.taxId) like :query ",

                                                                 params, "b", "updated", false);

        log.info( "Organisms found: " + organisms.getRowCount() );
    }

    private void loadParticipants( String query, String originalQuery ) {
        log.info( "Searching for participants matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // Load experiment eagerly to avoid LazyInitializationException when redering the view
        participants = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                "select distinct p " +
                        "from Component p left join p.xrefs as x " +
                        "where    p.ac = :ac " +
                        "      or lower(p.shortLabel) like :query "+
                        "      or lower(x.primaryId) like :query ",

                "select count(distinct p) " +
                        "from Component p left join p.xrefs as x " +
                        "where p.ac = :ac " +
                        "      or lower(p.shortLabel) like :query "+
                        "      or lower(x.primaryId) like :query ",

                params, "p", "updated", false);

        log.info( "Participants found: " + participants.getRowCount() );
    }

    public int countExperimentsForPublication( Publication publication ) {
        return getDaoFactory().getPublicationDao().countExperimentsForPublicationAc( publication.getAc() );
    }

    public int countInteractionsForPublication( Publication publication ) {
        return getDaoFactory().getPublicationDao().countInteractionsForPublicationAc( publication.getAc() );
    }

    ///////////////////////////
    // Getters and Setters

    public String getQuery() {
        return query;
    }

    public void setQuery( String query ) {
        this.query = query;
    }

    public LazyDataModel<Publication> getPublications() {
        return publications;
    }

    public LazyDataModel<Experiment> getExperiments() {
        return experiments;
    }

    public LazyDataModel<Interaction> getInteractions() {
        return interactions;
    }

    public LazyDataModel<Interactor> getMolecules() {
        return molecules;
    }

    public LazyDataModel<CvObject> getCvobjects() {
        return cvobjects;
    }

     public LazyDataModel<Feature> getFeatures() {
        return features;
    }

    public LazyDataModel<BioSource> getOrganisms() {
        return organisms;
    }

    public LazyDataModel<Component> getParticipants() {
        return participants;
    }

    public String getQuickQuery() {
        return quickQuery;
    }

    public void setQuickQuery(String quickQuery) {
        this.quickQuery = quickQuery;
    }

    public int getThreadTimeOut() {
        return threadTimeOut;
    }

    public void setThreadTimeOut(int threadTimeOut) {
        this.threadTimeOut = threadTimeOut;
    }
}
