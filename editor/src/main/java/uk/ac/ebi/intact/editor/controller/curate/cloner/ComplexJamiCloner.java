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
package uk.ac.ebi.intact.editor.controller.curate.cloner;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

/**
 * Editor specific cloning routine for complex participants.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id: InteractionIntactCloner.java 14783 2010-07-29 12:52:28Z brunoaranda $
 * @since 2.0.1-SNAPSHOT
 */
public class ComplexJamiCloner {

    public static Complex cloneInteraction(InteractionEvidence evidence) throws IntactClonerException {
        Complex clone = new IntactComplex(evidence.getShortName());

        clone.setInteractionType(evidence.getInteractionType());
        if (evidence.getExperiment() != null){
            clone.setOrganism(evidence.getExperiment().getHostOrganism());
            if (evidence.getExperiment().getPublication() != null){
                clone.setSource(evidence.getExperiment().getPublication().getSource());
            }
        }

        for (Object obj : evidence.getIdentifiers()){
            Xref ref = (Xref)obj;
            clone.getIdentifiers().add(new InteractorXref(ref.getDatabase(), ref.getId(), ref.getVersion(), ref.getQualifier()));
        }

        for (Object obj : evidence.getXrefs()){
            Xref ref = (Xref)obj;
            if (XrefUtils.isXrefFromDatabase(ref, Xref.IMEX_MI, Xref.IMEX)
                    && XrefUtils.doesXrefHaveQualifier(ref, Xref.PRIMARY_MI, Xref.PRIMARY)){
                // ignore IMEx id
            }
            else{
                clone.getXrefs().add(new InteractorXref(ref.getDatabase(), ref.getId(), ref.getVersion(), ref.getQualifier()));
            }
        }

        for (Object obj : evidence.getAnnotations()){
            Annotation annotation = (Annotation)obj;
            clone.getAnnotations().add(new InteractorAnnotation(annotation.getTopic(), annotation.getValue()));
        }

        for (Object obj : evidence.getParticipants()){
            Participant participant = (Participant)obj;
            ModelledParticipant r = ParticipantJamiCloner.cloneParticipant(participant);
            clone.addParticipant(r);
        }

        for (Object obj : evidence.getConfidences()){
            Confidence confidence = (Confidence)obj;
            clone.getModelledConfidences().add(new ComplexConfidence(confidence.getType(), confidence.getValue()));
        }

        for (Object obj : evidence.getParameters()){
            Parameter param = (Parameter)obj;
            clone.getModelledParameters().add(new ComplexParameter(param.getType(), param.getValue()));
        }

        // don't need to add it to the feature component because it is already done by the cloner
        return clone;
    }

    public static Complex cloneComplex(Complex complex) throws IntactClonerException {
        Complex clone = new IntactComplex(complex.getShortName());

        clone.setOrganism(complex.getOrganism());
        clone.setEvidenceType(complex.getEvidenceType());
        clone.setSource(complex.getSource());

        for (Object obj : complex.getAliases()){
            Alias alias = (Alias)obj;
            clone.getAliases().add(new InteractorAlias(alias.getType(), alias.getName()));
        }

        for (Object obj : complex.getIdentifiers()){
            Xref ref = (Xref)obj;
            clone.getIdentifiers().add(new InteractorXref(ref.getDatabase(), ref.getId(), ref.getVersion(), ref.getQualifier()));
        }

        for (Object obj : complex.getXrefs()){
            Xref ref = (Xref)obj;
            if (XrefUtils.isXrefFromDatabase(ref, Xref.IMEX_MI, Xref.IMEX)
                    && XrefUtils.doesXrefHaveQualifier(ref, Xref.PRIMARY_MI, Xref.PRIMARY)){
                // ignore IMEx id
            }
            else{
                clone.getXrefs().add(new InteractorXref(ref.getDatabase(), ref.getId(), ref.getVersion(), ref.getQualifier()));
            }
        }

        for (Object obj : complex.getAnnotations()){
            Annotation annotation = (Annotation)obj;
            clone.getAnnotations().add(new InteractorAnnotation(annotation.getTopic(), annotation.getValue()));
        }

        for (Object obj : complex.getParticipants()){
            Participant participant = (Participant)obj;
            ModelledParticipant r = ParticipantJamiCloner.cloneParticipant(participant);
            clone.addParticipant(r);
        }

        for (Object obj : complex.getModelledConfidences()){
            Confidence confidence = (Confidence)obj;
            clone.getModelledConfidences().add(new ComplexConfidence(confidence.getType(), confidence.getValue()));
        }

        for (Object obj : complex.getModelledParameters()){
            Parameter param = (Parameter)obj;
            clone.getModelledParameters().add(new ComplexParameter(param.getType(), param.getValue()));
        }

        // don't need to add it to the feature component because it is already done by the cloner
        return clone;
    }
}

