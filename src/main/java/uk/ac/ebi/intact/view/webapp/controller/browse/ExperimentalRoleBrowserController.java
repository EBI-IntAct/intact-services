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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;

/**
 * Controller for GoBrowsing
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("experimentalRoleBrowser")
@Scope("request")
public class ExperimentalRoleBrowserController extends OntologyBrowserController {

    public static final String FIELD_NAME = "experimentalRole_id";

    @Override
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {
        try {
            return new LazyLoadedOntologyTerm( ontologySearcher, "MI:0495", "experimental role");
        } catch (SolrServerException e) {
            e.printStackTrace();
            addErrorMessage("Could not load the tree", "");
        }
        return null;
    }

    @Override
    public String getFieldName() {
        return FIELD_NAME;
    }
}