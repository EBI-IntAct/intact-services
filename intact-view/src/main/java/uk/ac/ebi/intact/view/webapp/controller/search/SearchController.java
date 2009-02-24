package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.model.InteractorSearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.SolrSearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.TooManyResultsException;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Search controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBean")
@Scope("conversation.access")
@ConversationName("general")
@ViewController(viewIds = {"/main.xhtml",
                           "/pages/search/search.xhtml",
                           "/pages/interactions/interactions.xhtml",
                           "/pages/list/protein_list.xhtml",
                           "/pages/list/compound_list.xhtml",
                           "/pages/molecule/molecule.xhtml",
                           "/pages/graph/graph.xhtml",
                           "/pages/browse/browse.xhtml",
                           "/pages/browse/gobrowser.xhtml"})
public class SearchController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    public static final String QUERY_PARAM = "query";
    public static final String ONTOLOGY_QUERY_PARAM = "ontologyQuery";
    public static final String TERMID_QUERY_PARAM = "termId";

    // table IDs
    public static final String INTERACTIONS_TABLE_ID = "interactionResults";
    public static final String PROTEINS_TABLE_ID = "proteinListResults";
    public static final String COMPOUNDS_TABLE_ID = "compoundListResults";
    public static final String DNA_TABLE_ID = "dnaListResults";
    public static final String RNA_TABLE_ID = "rnaListResults";


    // injected
    @Autowired
    private AppConfigBean appConfigBean;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    @Autowired
    private UserQuery userQuery;

    private int totalResults;
    private int interactorTotalResults;
    private int proteinTotalResults;
    private int smallMoleculeTotalResults;

    private int nucleicacidTotalResults;
    private int dnaTotalResults;
    private int rnaTotalResults;



    private boolean showProperties;
    private boolean showAlternativeIds;
    private boolean showBrandNames;

    private boolean expandedView;

    // results
    private SolrSearchResultDataModel results;
    private InteractorSearchResultDataModel proteinResultDataModel;
    private SearchResultDataModel smallMoleculeResults;
    private SearchResultDataModel dnaResults;
    private SearchResultDataModel rnaResults;


    // io
    private String exportFormat;

    //sorting
    private static final String DEFAULT_SORT_COLUMN = "relevancescore_s";
    private static final boolean DEFAULT_SORT_ORDER = true;

    private String userSortColumn = DEFAULT_SORT_COLUMN;
    //as the Sort constructor is Sort(String field, boolean reverse)
    private boolean ascending = DEFAULT_SORT_ORDER;

    public SearchController() {
    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();

        String queryParam = context.getExternalContext().getRequestParameterMap().get(QUERY_PARAM);
        String ontologyQueryParam = context.getExternalContext().getRequestParameterMap().get(ONTOLOGY_QUERY_PARAM);
         
        if (queryParam != null && queryParam.length()>0) {
            if (log.isDebugEnabled()) log.debug("Searching using query parameter: "+queryParam);

            //for sorting and ordering
            this.setUserSortColumn(DEFAULT_SORT_COLUMN);
            this.setAscending( DEFAULT_SORT_ORDER );

            userQuery.reset();
            userQuery.setSearchQuery( queryParam );

            SolrQuery solrQuery = userQuery.createSolrQuery();

            doBinarySearch( solrQuery );
        }

        if ( ontologyQueryParam != null && ontologyQueryParam.length()>0) {
            if (log.isDebugEnabled()) log.debug("Searching using ontology query parameter: "+queryParam);

            userQuery.reset();
            userQuery.setOntologySearchQuery(ontologyQueryParam);

            SolrQuery solrQuery = userQuery.createSolrQuery();

            doBinarySearch( solrQuery );
        }
    }

    public String doBinarySearchAction() {
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch( solrQuery );

        return "interactions";
    }

    public String doOntologySearchAction() {
        final String query = userQuery.getOntologySearchQuery();

        if ( query == null) {
            addErrorMessage("The ontology query box was empty", "No search was submitted");
            return "search";
        }

        SolrQuery solrQuery = userQuery.createSolrQuery();
        solrQuery.setQuery("*:*");

        doBinarySearch( solrQuery );

        return "interactions";
    }

    public void doFilteredBinarySearch(ActionEvent evt) {
        final Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String termParam = UserQueryUtils.getCurrentQueryTermParam( userQuery );
        String termId = params.get(termParam);

        if( termId == null ) {
            throw new IllegalStateException( "TermId was null" );
        }

        // TODO is this needed?
        //userQuery.addProperty( termId );

        if (true) throw new UnsupportedOperationException("This has been modified");

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch( solrQuery );
    }

    private void doBinarySearch(SolrQuery solrQuery) {

        SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();

        try {
            results = new SolrSearchResultDataModel(solrServer, solrQuery);

            totalResults = results.getRowCount();

            if (log.isDebugEnabled()) log.debug("\tResults: " + results.getRowCount());

            if (totalResults == 0) {
                addErrorMessage("Your query didn't return any results", "Use a different query");
            }

        } catch (TooManyResultsException e) {
            addErrorMessage("Too many results found", "Please, refine your query");
            e.printStackTrace();
        }

        // TODO implement interactor query
        //doInteractorSearch(userQuery);
    }

    public void onListDisclosureChanged(DisclosureEvent evt) {
        if (evt.isExpanded()) {
             doProteinsSearch();
        }
    }

    public void doInteractorSearch(UserQuery query) {
        doProteinsSearch();
        doSmallMoleculeSearch(query);
        doDnaSearch(query);
        doRnaSearch(query);
        //for dgi
        //interactorTotalResults = getSumOfAll(smallMoleculeTotalResults, proteinTotalResults);
        //for intact
        nucleicacidTotalResults = dnaTotalResults + rnaTotalResults;
        interactorTotalResults = smallMoleculeTotalResults + proteinTotalResults + dnaTotalResults + rnaTotalResults;

    }

    private void doProteinsSearch() {
        final SolrQuery solrQuery = userQuery.createSolrQuery();

        if (log.isDebugEnabled()) log.debug("Searching proteins for query: " + solrQuery);

        proteinResultDataModel = new InteractorSearchResultDataModel(intactViewConfiguration.getInteractionSolrServer(), 
                                                                     solrQuery,
                                                                     CvInteractorType.PROTEIN_MI_REF);
        proteinTotalResults = proteinResultDataModel.getRowCount();
    }

    private void doSmallMoleculeSearch(UserQuery query) {

         if (true) throw new UnsupportedOperationException("This has been modified");

        // TODO fix this
        //query.setInteractorTypeMi(CvInteractorType.SMALL_MOLECULE_MI_REF);
        String indexDirectory = "";

        try {
            //String interactorQuery = query.getInteractorQuery();
            String interactorQuery = null;
            if (log.isDebugEnabled()) log.debug("Searching small molecules with interactorQuery: " + interactorQuery);
            smallMoleculeResults = new SearchResultDataModel(interactorQuery, indexDirectory, userQuery.getPageSize());
            smallMoleculeTotalResults = smallMoleculeResults.getRowCount();
        } catch (TooManyResultsException e) {
            addErrorMessage("Too many small molecules found", "Please, refine your query");
            smallMoleculeTotalResults = 0;
            interactorTotalResults = 0;
        }
    }

    private void doDnaSearch(UserQuery query) {
        //dna->MI:0319,ds dna->MI:0681, ss dna->MI:0680
        //String[] interactorTypes = new String[] {CvInteractorType.DNA_MI_REF,"MI:0680","MI:0681"};

         if (true) throw new UnsupportedOperationException("This has been modified");

        // TODO fix this
        //query.setInteractorTypeMi( CvInteractorType.DNA_MI_REF );
        String indexDirectory = "";

        try {
            String interactorQuery = null;
            //String interactorQuery = query.getInteractorQuery();
            if (log.isDebugEnabled()) log.debug("Searching dna with interactorQuery : " + interactorQuery);
            dnaResults = new SearchResultDataModel(interactorQuery, indexDirectory, userQuery.getPageSize());
            dnaTotalResults = dnaResults.getRowCount();

            if ( log.isDebugEnabled() )log.debug( "dnaTotalResults " + dnaTotalResults);


        } catch (TooManyResultsException e) {
            addErrorMessage("Too many dnas found", "Please, refine your query");
            dnaTotalResults = 0;
            interactorTotalResults = 0;
        }
    }


    private void doRnaSearch( UserQuery query ) {
        //rna->MI:0320,mrna->MI:0324,trna->MI:0325,rrna->0608,snrna->0607,sirna->0610
        //String[] interactorTypes = new String[]{CvInteractorType.RNA_MI_REF, "MI:0324", "MI:0325","MI:0608","MI:0607","MI:0610"};

        if (true) throw new UnsupportedOperationException("This has been modified");

        // TODO fix this
        //query.setInteractorTypeMi( CvInteractorType.RNA_MI_REF );
        String indexDirectory = "";

        try {
            String interactorQuery = null;
            //String interactorQuery = query.getInteractorQuery();
            if ( log.isDebugEnabled() ) log.debug( "Searching rna with interactorQuery : " + interactorQuery );
            rnaResults = new SearchResultDataModel( interactorQuery, indexDirectory, userQuery.getPageSize() );
            rnaTotalResults = rnaResults.getRowCount();

            if ( log.isDebugEnabled() ) log.debug( "rnaTotalResults " + rnaTotalResults );


        } catch ( TooManyResultsException e ) {
            addErrorMessage( "Too many rnas found", "Please, refine your query" );
            rnaTotalResults = 0;
            interactorTotalResults = 0;
        }
    }


    public void rangeChanged(RangeChangeEvent evt) {
        results.setRowIndex(evt.getNewStart());

        UIXTable table = (UIXTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(INTERACTIONS_TABLE_ID);
        table.setFirst(evt.getNewStart());

        refreshTable(INTERACTIONS_TABLE_ID, results);
    }

    public void doSearchInteractionsFromCompoundListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection( COMPOUNDS_TABLE_ID, "intact" );
    }

    public void doSearchInteractionsFromProteinListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection( PROTEINS_TABLE_ID, "intact" );
    }

    public void doSearchInteractionsFromDnaListSelection( ActionEvent evt ) {
        doSearchInteractionsFromListSelection( DNA_TABLE_ID, "intact" );
    }

    public void doSearchInteractionsFromRnaListSelection( ActionEvent evt ) {
        doSearchInteractionsFromListSelection( RNA_TABLE_ID, "intact" );
    }

    private void doSearchInteractionsFromListSelection(String tableName, String database ) {
        final List<IntactBinaryInteraction> selected = getSelected(tableName);

        if (selected.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder( selected.size() * 10 );
        sb.append("id:(");

        for (Iterator<IntactBinaryInteraction> iterator = selected.iterator(); iterator.hasNext();) {
            IntactBinaryInteraction intactBinaryInteraction = iterator.next();

            String identifier = null;

            for (CrossReference xref : intactBinaryInteraction.getInteractorA().getIdentifiers()) {
                if (database.equals(xref.getDatabase())) {
                    identifier = xref.getIdentifier();
                }
            }

            sb.append(identifier);

            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        sb.append(")");

        final String query = sb.toString();
        userQuery.setSearchQuery( query );

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);

        resetSelection(tableName);
    }

    public void resetSearch(ActionEvent event) {
        this.userQuery.reset();
    }

    public void userSort( ValueChangeEvent event ) {
        String sortableColumn = (String)event.getNewValue();

        if ( sortableColumn == null ) {
            sortableColumn = DEFAULT_SORT_COLUMN;
        }

        userQuery.setUserSortColumn( sortableColumn );
        this.setUserSortColumn( sortableColumn );
        userQuery.setUserSortOrder(DEFAULT_SORT_ORDER);
        this.setAscending( DEFAULT_SORT_ORDER );

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch( solrQuery );

    }

    public void userSortOrder( ValueChangeEvent event ) {
        Boolean sortOrder =  (Boolean) event.getNewValue();

        if ( sortOrder == null ) {
            userQuery.setUserSortOrder( DEFAULT_SORT_ORDER );
        } else {
            userQuery.setUserSortColumn( this.getUserSortColumn());
            userQuery.setUserSortOrder( sortOrder );
            this.setAscending(sortOrder);

            SolrQuery solrQuery = userQuery.createSolrQuery();

            doBinarySearch( solrQuery );
        }

    }


    // Getters & Setters
    /////////////////////

    public SolrSearchResultDataModel getResults() {
        return results;
    }

    public void setResults( SolrSearchResultDataModel results ) {
        this.results = results;
    }

    public boolean isShowProperties() {
        return showProperties;
    }

    public void setShowProperties( boolean showProperties ) {
        this.showProperties = showProperties;
    }

    public boolean isShowAlternativeIds() {
        return showAlternativeIds;
    }

    public void setShowAlternativeIds( boolean showAlternativeIds ) {
        this.showAlternativeIds = showAlternativeIds;
    }

    public boolean isShowBrandNames() {
        return showBrandNames;
    }

    public void setShowBrandNames( boolean showBrandNames ) {
        this.showBrandNames = showBrandNames;
    }

    public boolean isExpandedView() {
        return expandedView;
    }

    public void setExpandedView( boolean expandedView ) {
        this.expandedView = expandedView;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat( String exportFormat ) {
        this.exportFormat = exportFormat;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults( int totalResults ) {
        this.totalResults = totalResults;
    }

    public int getInteractorTotalResults() {
        return interactorTotalResults;
    }

    public void setInteractorTotalResults( int interactorTotalResults ) {
        this.interactorTotalResults = interactorTotalResults;
    }

    public InteractorSearchResultDataModel getProteinResultDataModel() {
        return proteinResultDataModel;
    }

    public void setProteinResultDataModel( InteractorSearchResultDataModel proteinResultDataModel) {
        this.proteinResultDataModel = proteinResultDataModel;
    }

    public int getSmallMoleculeTotalResults() {
        return smallMoleculeTotalResults;
    }

    public int getProteinTotalResults() {
        return proteinTotalResults;
    }

    public SearchResultDataModel getSmallMoleculeResults() {
        return smallMoleculeResults;
    }

    public String getUserSortColumn() {
        return userSortColumn;
    }

    public void setUserSortColumn( String userSortColumn ) {
        this.userSortColumn = userSortColumn;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending( boolean ascending ) {
        this.ascending = ascending;
    }

    public int getDnaTotalResults() {
        return dnaTotalResults;
    }

    public void setDnaTotalResults( int dnaTotalResults ) {
        this.dnaTotalResults = dnaTotalResults;
    }

    public int getRnaTotalResults() {
        return rnaTotalResults;
    }

    public void setRnaTotalResults( int rnaTotalResults ) {
        this.rnaTotalResults = rnaTotalResults;
    }

    public int getNucleicacidTotalResults() {
        return nucleicacidTotalResults;
    }

    public void setNucleicacidTotalResults( int nucleicacidTotalResults ) {
        this.nucleicacidTotalResults = nucleicacidTotalResults;
    }

    public SearchResultDataModel getDnaResults() {
        return dnaResults;
    }

    public void setDnaResults( SearchResultDataModel dnaResults ) {
        this.dnaResults = dnaResults;
    }

    public SearchResultDataModel getRnaResults() {
        return rnaResults;
    }

    public void setRnaResults( SearchResultDataModel rnaResults ) {
        this.rnaResults = rnaResults;
    }
}