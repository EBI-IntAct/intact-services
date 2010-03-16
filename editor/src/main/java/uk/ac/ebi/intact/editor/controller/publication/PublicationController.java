/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.publication;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class PublicationController extends JpaAwareController {

    private Publication publication;
    private String ac;

    private String openQuery;

    public PublicationController() {
    }

    public void loadData(ComponentSystemEvent event) {
        loadByAc();
    }

    private void loadByAc() {
        if (ac != null) {
            if (publication == null || !ac.equals(publication.getAc())) {
                publication = getDaoFactory().getPublicationDao().getByAc(ac);
            }
        } else if (publication != null) {
            ac = publication.getAc();
        }
    }

    public void openByPmid(ActionEvent evt) {
        if (openQuery == null || openQuery.trim().length() == 0) {
            addErrorMessage("PMID is empty", "No PMID was supplied");
        } else {
            Publication publicationToOpen = getDaoFactory().getPublicationDao().getByPubmedId(openQuery);

            if (publicationToOpen == null) {
                addErrorMessage("PMID not found", "There is no publication with PMID '"+openQuery+"'");
            } else {
                publication = publicationToOpen;
                ac = publication.getAc();
            }
        }

    }

    public String getAc() {
        if (ac == null && publication != null) {
            return publication.getAc();
        }
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getOpenQuery() {
        return openQuery;
    }

    public void setOpenQuery(String openQuery) {
        this.openQuery = openQuery;
    }
}
