-- #############################################################################
-- #                      intact__feature__main                            
-- #############################################################################



INSERT INTO intact__interactor__main
SELECT int_main.experiment_key,                 -- experiment_key
       int_main.interaction_key,                -- interaction_key
       com_roles.interactor_key,                -- interactor_key
       int_main.experiment_short,               -- experiment_short
       int_main.experiment_full,                -- experiment_full
       int_main.host_organism_taxid,            -- host_organism_taxid
       int_main.host_organism_short,            -- host_organism_short
       int_main.host_organism_full,             -- host_organism_full
       int_main.participant_identMethod_mi,     -- participant_identMethod_mi
       int_main.participant_identMethod_short,  -- participant_identMethod_short
       int_main.participant_identMethod_full,   -- participant_identMethod_full
       int_main.interaction_detectMethod_mi,    -- interaction_detectMethod_mi
       int_main.interaction_detectMethod_short, -- interaction_detectMethod_short
       int_main.interaction_detectMethod_full,  -- interaction_detectMethod_full
       int_main.interaction_count,              -- interaction_count
       int_main.interaction_type_mi,            -- interaction_type_mi
       int_main.interaction_type_short,         -- interaction_type_short
       int_main.interaction_type_full,          -- interaction_type_full
       int_main.interactor_count,               -- interactor_count
       int_type.interactor_shortlabel,          -- interactor_shortlabel
       int_type.interactor_fullname,            -- interactor_fullname
       int_type.interactor_type_mi,             -- interactor_type_mi
       int_type.interactor_type_short,          -- interactor_type_short
       int_type.interactor_type_full,           -- interactor_type_full
       com_roles.experimental_role_mi,          -- experimental_role_mi
       com_roles.experimental_role_short,       -- experimental_role_short
       com_roles.experimental_role_full,        -- experimental_role_full
       com_roles.biological_role_mi,            -- biological_role_mi
       com_roles.biological_role_short,         -- biological_role_short
       com_roles.biological_role_full,          -- biological_role_full
       int_type.interactor_biosource_taxid,     -- interactor_biosource_taxid
       int_type.interactor_biosource_short,     -- interactor_biosource_short
       int_type.interactor_biosource_full,      -- interactor_biosource_full
       com_roles.component_expressed_in_taxid,  -- component_expressed_in_taxid
       com_roles.component_expressed_in_short,  -- component_expressed_in_short
       com_roles.component_expressed_in_full,   -- component_expressed_in_full
       com_roles.stoichiometry,                 -- stoichiometry
       null AS molecule_count,                  -- molecule_count
       null AS interactor_sequence,             -- interactor_sequence
       null AS interactor_sequence_length,      -- interactor_sequence_length
       int_type.crc64                           -- crc64
FROM intact__interactor__main;




















-- #############################################################################
-- #                  intact__feature_alias__dm                            
-- #############################################################################                           

-- no feature have an alias...
-- all values at the moment null...
INSERT INTO intact__feature_alias__dm
SELECT f.ac,             -- feature_key
       ali.name,         -- name
       cv.mi,            -- alias_type_mi
       cv.shortlabel,    -- alias_type_short
       cv.fullname       -- alias_type_full
FROM ia_feature_alias ali LEFT OUTER JOIN v_cv_mi cv
                                       ON (cv.ac = ali.aliastype_ac)
                         RIGHT OUTER JOIN ia_feature f
                                       ON ( ali.parent_ac = f.ac );

        

-- #############################################################################
-- #                  intact__feature_xref__dm                             
-- #############################################################################

INSERT INTO intact__feature_xref__dm
SELECT f.ac,               -- feature_key
       xref.primaryid,     -- primary_id
       xref.secondaryid,   -- secondary_id
       cv1.mi,             -- database_mi
       cv1.shortlabel,     -- database_short
       cv1.fullname,       -- database_full
       cv2.mi,             -- qualifier_mi
       cv2.shortlabel,     -- qualifier_short
       cv2.fullname        -- qualifier_full
FROM ia_feature_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                          ON (cv1.ac = xref.database_ac)
                             LEFT OUTER JOIN v_cv_mi cv2
                                          ON (cv2.ac = xref.qualifier_ac)
                            RIGHT OUTER JOIN ia_feature f
                                          ON ( f.ac = xref.parent_ac);



-- #############################################################################
-- #                  intact__feature_anno__dm                          
-- #############################################################################

INSERT INTO intact__feature_anno__dm
SELECT f.ac,              -- feature_key
       anno.description,  -- description
       anno.mi,           -- topic_mi
       anno.shortlabel,   -- topic_short
       anno.fullname      -- topic_full
FROM ia_feature2annot f_anno LEFT OUTER JOIN v_annotation anno
                                          ON (anno.annotation_ac = f_anno.annotation_ac)
                            RIGHT OUTER JOIN ia_feature f
                                          ON (f.ac = f_anno.feature_ac);
 
