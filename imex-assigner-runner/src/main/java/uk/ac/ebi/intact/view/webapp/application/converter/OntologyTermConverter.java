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
package uk.ac.ebi.intact.view.webapp.application.converter;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.InteractionOntologyTerm;
import uk.ac.ebi.intact.view.webapp.controller.application.OntologyBean;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesConverter( value = "ontologyTermConverter", forClass = InteractionOntologyTerm.class )
public class OntologyTermConverter implements Converter {

    private OntologyBean ontologyBean;

    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (this.ontologyBean == null){
            this.ontologyBean = (OntologyBean) IntactContext.getCurrentInstance().getSpringContext().getBean("ontologyBean");
        }

        Object object = ontologyBean.findByIdentifier(value);
        if (object == null){
            object = ontologyBean.findByName(value);
        }
        return object;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        if (value == null) {
            return null;
        }

        if (value instanceof InteractionOntologyTerm) {
            InteractionOntologyTerm term = (InteractionOntologyTerm) value;
            if (term.getIdentifier() != null ){
                return term.getIdentifier();
            }
            else {
                return term.getName();
            }
        } else {
            throw new ConverterException("An OntologyTerm class was expected but an illegal class passed to converter: "+value.getClass().getName());
        }
    }
}
