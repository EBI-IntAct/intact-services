/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.expansion;

import java.util.Collection;

import uk.ac.ebi.intact.confidence.model.InteractionSimplified;


/**
 * Interface for applying an expansion to an interaction.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13-Oct-2006</pre>
 */
public interface ExpansionStrategy {

    /**
     * Expand an interaction into a 
     * @param interaction
     * @return
     */
    public Collection<InteractionSimplified> expand( InteractionSimplified interaction );
}