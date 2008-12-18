/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psicquic.ws;

import org.hupo.psi.mi.psicquic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.engine.SearchEngine;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import psidev.psi.mi.xml.converter.impl254.EntrySetConverter;
import psidev.psi.mi.xml.dao.inMemory.InMemoryDAOFactory;
import psidev.psi.mi.xml254.jaxb.EntrySet;
import uk.ac.ebi.intact.psicquic.ws.config.PsicquicConfig;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.IntactTab2Xml;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;

import java.io.IOException;
import java.util.*;

/**
 * This web service is based on a PSIMITAB lucene's directory to search and return the results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class IntactPsicquicService implements PsicquicService {

    private final Logger logger = LoggerFactory.getLogger(IntactPsicquicService.class);

    private static final String RETURN_TYPE_XML25 = "psi-mi/xml25";
    private static final String RETURN_TYPE_MITAB25 = "psi-mi/tab25";
    private static final String RETURN_TYPE_COUNT = "count";

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final int BLOCKSIZE_MAX = 200;
    private static final String RETURN_TYPE_DEFAULT = RETURN_TYPE_MITAB25;

    private static final List<String> SUPPORTED_RETURN_TYPES = Arrays.asList(RETURN_TYPE_XML25, RETURN_TYPE_MITAB25, RETURN_TYPE_COUNT);
    
    @Autowired
    private PsicquicConfig config;

    public IntactPsicquicService() {

    }

    public QueryResponse getByInteractor(DbRef dbRef, RequestInfo requestInfo) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        String query = createQuery("identifiers", dbRef);

        return getByQuery(query, requestInfo);
    }

    public QueryResponse getByInteraction(DbRef dbRef, RequestInfo requestInfo) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        String query = createQuery("interaction_id", dbRef);

        return getByQuery(query, requestInfo);
    }

    public QueryResponse getByInteractorList(List<DbRef> dbRefs, RequestInfo requestInfo, String operand) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        String query = createQuery("identifiers", dbRefs, operand);

        return getByQuery(query, requestInfo);
    }

    public QueryResponse getByInteractionList(List<DbRef> dbRefs, RequestInfo requestInfo) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = createQuery("interaction_id", dbRefs, "OR");

        return getByQuery(query, requestInfo);
    }

    private String createQuery(String fieldName, DbRef dbRef) {
        return createQuery(fieldName, Collections.singleton(dbRef), null);
    }

    private String createQuery(String fieldName, Collection<DbRef> dbRefs, String operand) {
        StringBuilder sb = new StringBuilder(dbRefs.size() * 64);
        sb.append(fieldName).append(":(");

        for (Iterator<DbRef> dbRefIterator = dbRefs.iterator(); dbRefIterator.hasNext();) {
            DbRef dbRef = dbRefIterator.next();

            sb.append(createQuery(dbRef));

            if (dbRefIterator.hasNext()) {
                sb.append(" ").append(operand).append(" ");
            }
        }

        sb.append(")");

        return sb.toString();
    }

    private String createQuery(DbRef dbRef) {
        String db = dbRef.getDbAc();
        String id = dbRef.getId();

        return "("+((db == null || db.length() == 0)? "\""+id+"\"" : "\""+db+"\" AND \""+id+"\"")+")";
    }

    public QueryResponse getByQuery(String query, RequestInfo requestInfo) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        final int blockSize = Math.min(requestInfo.getBlockSize(), BLOCKSIZE_MAX);

        final String resultType = requestInfo.getResultType();

        if (resultType != null && !getSupportedReturnTypes().contains(resultType)) {
            throw new NotSupportedTypeException("Not supported return type: "+resultType+" - Supported types are: "+getSupportedReturnTypes());
        }

        logger.debug("Searching: {} ({}/{})", new Object[] {query, requestInfo.getFirstResult(), blockSize});

        SearchEngine searchEngine;

        try {
            searchEngine = new IntactSearchEngine(config.getIndexDirectory());
        } catch (IOException e) {
            throw new PsicquicServiceException("Problem creating SearchEngine using directory: "+config.getIndexDirectory(), e);
        }

        SearchResult searchResult = searchEngine.search(query, requestInfo.getFirstResult(), blockSize);

        // preparing the response
        QueryResponse queryResponse = new QueryResponse();
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setBlockSize(blockSize);
        resultInfo.setFirstResult(requestInfo.getFirstResult());
        resultInfo.setTotalResults(searchResult.getTotalCount());

        queryResponse.setResultInfo(resultInfo);

        ResultSet resultSet = createResultSet(searchResult, requestInfo);
        queryResponse.setResultSet(resultSet);

        return queryResponse;
    }

    public String getVersion() {
        return config.getVersion();
    }

    public List<String> getSupportedReturnTypes()  {
        return SUPPORTED_RETURN_TYPES;
    }

    public List<String> getSupportedDbAcs() {
        return Collections.EMPTY_LIST;
    }

    protected ResultSet createResultSet(SearchResult searchResult, RequestInfo requestInfo) throws PsicquicServiceException,
                                                                                                   NotSupportedTypeException {
        ResultSet resultSet = new ResultSet();

        String resultType = (requestInfo.getResultType() != null)? requestInfo.getResultType() : RETURN_TYPE_DEFAULT;

        if (RETURN_TYPE_MITAB25.equals(resultType)) {
            if (logger.isDebugEnabled()) logger.debug("Creating PSI-MI TAB");

            String mitab = createMitabResults(searchResult);
            resultSet.setMitab(mitab);
        } else if (RETURN_TYPE_XML25.equals(resultType)) {
            if (logger.isDebugEnabled()) logger.debug("Creating PSI-MI XML");

            EntrySet jEntrySet = createEntrySet(searchResult);
            resultSet.setEntrySet(jEntrySet);
        } else if (RETURN_TYPE_COUNT.equals(resultType)) {
            if (logger.isDebugEnabled()) logger.debug("Count query");
            // nothing to be done here
        } else {
            throw new NotSupportedTypeException("Not supported return type: "+resultType+" - Supported types are: "+getSupportedReturnTypes());
        }

        return resultSet;
    }

    protected String createMitabResults(SearchResult searchResult) {
        DocumentDefinition docDef = new IntactDocumentDefinition();

        List<BinaryInteraction> binaryInteractions = searchResult.getData();

        StringBuilder sb = new StringBuilder(binaryInteractions.size() * 512);

        for (BinaryInteraction binaryInteraction : binaryInteractions) {
            String binaryInteractionString = docDef.interactionToString(binaryInteraction);
            sb.append(binaryInteractionString);
            sb.append(NEW_LINE);
        }
        return sb.toString();
    }

    private EntrySet createEntrySet(SearchResult searchResult) throws PsicquicServiceException {
        Tab2Xml tab2Xml = new IntactTab2Xml();
        try {
            psidev.psi.mi.xml.model.EntrySet mEntrySet = tab2Xml.convert(searchResult.getData());

            EntrySetConverter converter = new EntrySetConverter();
            converter.setDAOFactory(new InMemoryDAOFactory());

            return converter.toJaxb(mEntrySet);

        } catch (Exception e) {
            throw new PsicquicServiceException("Problem converting results to PSI-MI XML", e);
        }
    }
}

