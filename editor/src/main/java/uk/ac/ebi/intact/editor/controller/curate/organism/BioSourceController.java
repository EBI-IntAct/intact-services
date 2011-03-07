package uk.ac.ebi.intact.editor.controller.curate.organism;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyTerm;
import uk.ac.ebi.intact.bridges.taxonomy.UniprotTaxonomyService;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class BioSourceController extends AnnotatedObjectController {

    private String ac;
    private BioSource bioSource;

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return bioSource;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
       this.bioSource = (BioSource) annotatedObject;
    }

    public void loadData(ComponentSystemEvent evt) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (ac != null) {
                bioSource = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getBioSourceDao(), ac);
            } else {
                bioSource = new BioSource();
            }

        }

        generalLoadChecks();
    }

    public String newOrganism() {
        BioSource bioSource = new BioSource();
        setBioSource(bioSource);

        //getUnsavedChangeManager().markAsUnsaved(bioSource);

        return navigateToObject(bioSource);
    }

    public void autoFill(ActionEvent evt) {
        final String taxIdStr = bioSource.getTaxId();

        if (taxIdStr == null || taxIdStr.isEmpty()) {
            return;
        }

        try {
            final int taxId = Integer.valueOf(taxIdStr);

            UniprotTaxonomyService uniprotTaxonomyService = new UniprotTaxonomyService();
            final TaxonomyTerm term = uniprotTaxonomyService.getTaxonomyTerm(taxId);

            String name;

            if (term.getCommonName() != null) {
                name = term.getCommonName();
            } else {
                name = term.getScientificName();
            }

            bioSource.setShortLabel(name.toLowerCase());
            bioSource.setFullName(term.getScientificName());

            setTaxId(taxIdStr);
        } catch (Throwable e) {
            addErrorMessage("Problem auto-filling from Uniprot Taxonomy", e.getMessage());
            handleException(e);
        }
    }

    @Override
    public void doPostSave() {
        BioSourceService bioSourceService = (BioSourceService) getSpringContext().getBean("bioSourceService");
        bioSourceService.refresh(null);
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public BioSource getBioSource() {
        return bioSource;
    }

    public void setBioSource(BioSource bioSource) {
        this.bioSource = bioSource;
        this.ac = bioSource.getAc();
    }

    public void setTaxId(String taxId) {
        bioSource.setTaxId(taxId);
        replaceOrCreateXref("MI:0942", CvXrefQualifier.IDENTITY, taxId);
    }

    public String getTaxId() {
        return bioSource.getTaxId();
    }


}
