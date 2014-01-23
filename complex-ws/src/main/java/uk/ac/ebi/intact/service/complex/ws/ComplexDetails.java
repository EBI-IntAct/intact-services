package uk.ac.ebi.intact.service.complex.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 09/12/13
 */
@XmlRootElement(name = "ComplexDetails")
@XmlAccessorType(XmlAccessType.NONE)
public class ComplexDetails {
    private String systematicName;
    private Collection<String> synonyms;
    private String function;
    private String properties;
    private String ac;
    private String name;
    private String specie;
    private Collection<ComplexDetailsParticipants> participants;
    private Collection<ComplexDetailsCrossReferences> crossReferences;

    public ComplexDetails() {
        this.synonyms = new LinkedList<String>();
        this.participants = new ArrayList<ComplexDetailsParticipants>();
        this.crossReferences = new ArrayList<ComplexDetailsCrossReferences>();
    }

    public void setSystematicName ( String systematic ) {
        this.systematicName = systematic;
    }
    @XmlElement
    public String getSystematicName () { return this.systematicName; }
    public void setSynonyms ( List<String> syns ) { this.synonyms = syns; }
    public void addSynonym ( String syn ) { this.synonyms.add(syn); }
    @XmlElement
    public Collection<String> getSynonyms() { return this.synonyms; }
    public void setFunction ( String func ) { this.function = func; }
    @XmlElement
    public String getFunction () { return this.function; }
    public void setProperties ( String poper ) { this.properties = poper; }
    @XmlElement
    public String getProperties () { return this.properties; }
    public void setAc ( String id ) { this.ac = id; }
    @XmlElement
    public String getAc () { return this.ac; }
    public void setName ( String n ) { this.name = n; }
    @XmlElement
    public String getName () { return this.name; }
    public void setSpecie ( String s ) { this.specie = s; }
    @XmlElement
    public String getSpecie () { return this.specie; }
    public Collection<ComplexDetailsParticipants> getParticipants() {
        return participants;
    }
    public Collection<ComplexDetailsCrossReferences> getCrossReferences() {
        return crossReferences;
    }
}
