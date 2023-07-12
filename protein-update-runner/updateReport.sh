#!/bin/bash

##################################################################
# Runs the protein update
#
# Usage $0 database folder
#
#
##################################################################

if [ $# -ne 2 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 FOLDER LOG_FILE"
      echo ""
      exit 1
fi

FOLDER=$1
LOG_FILE=$2

UPDATE_REPORT=${FOLDER}/protein_update_report.txt

cd ${FOLDER}

echo "Protein Update summary" >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Updated uniprot entries: " >> $UPDATE_REPORT
tail -n +2 update_cases.csv | wc -l >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Feature annotations added/removed: " >> $UPDATE_REPORT
tail -n +2 feature_changed.csv | wc -l >> $UPDATE_REPORT
cat feature_changed.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Ranges successfully shifted and/or feature sequence successfully updated: " >> $UPDATE_REPORT
tail -n +2 range_changed.csv | wc -l >> $UPDATE_REPORT
cat range_changed.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Participants having range conflicts: " >> $UPDATE_REPORT
tail -n +2 out_of_date_participants.csv | wc -l >> $UPDATE_REPORT
cat out_of_date_participants.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT
echo -n "    - Invalid ranges: " >> $UPDATE_REPORT
tail -n +2 invalid_range.csv | wc -l >> $UPDATE_REPORT
cat invalid_range.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT
echo -n "    - Out of date ranges: " >> $UPDATE_REPORT
tail -n +2 out_of_date_range.csv | wc -l >> $UPDATE_REPORT
cat out_of_date_range.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Created proteins: " >> $UPDATE_REPORT
tail -n +2 created.csv | wc -l >> $UPDATE_REPORT
cat created.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Duplicated protein events: " >> $UPDATE_REPORT
tail -n +2 duplicates.csv | wc -l >> $UPDATE_REPORT
cat duplicates.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Proteins pointing to a secondary uniprot ac: " >> $UPDATE_REPORT
tail -n +2 secondary_proteins.csv | wc -l >> $UPDATE_REPORT
cat secondary_proteins.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Dead proteins: " >> $UPDATE_REPORT
tail -n +2 dead_proteins.csv | wc -l >> $UPDATE_REPORT
cat dead_proteins.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Isoforms anf feature chains with invalid intact parent: " >> $UPDATE_REPORT
tail -n +2 updated_intact_parents.csv | wc -l >> $UPDATE_REPORT
cat updated_intact_parents.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Proteins having a sequence identical to one of the isoform/feature chains of the uniprot entry before updating the sequence: " >> $UPDATE_REPORT
tail -n +2 transcript_same_sequence.csv | wc -l >> $UPDATE_REPORT
cat transcript_same_sequence.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Proteins with a sequence conservation inferior to 0.35: " >> $UPDATE_REPORT
tail -n +2 sequence_changed_caution.csv | wc -l >> $UPDATE_REPORT
cut -f 1,2 sequence_changed_caution.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Duplicated participants which have been deleted: " >> $UPDATE_REPORT
tail -n +2 deleted_component.csv | wc -l >> $UPDATE_REPORT
cat deleted_component.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Deleted proteins: " >> $UPDATE_REPORT
tail -n +2 deleted.csv | wc -l >> $UPDATE_REPORT
echo -n "    - Duplicated proteins deleted: " >> $UPDATE_REPORT
grep "Duplicate" deleted.csv | wc -l >> $UPDATE_REPORT
echo -n "    - Proteins without any interactions: " >> $UPDATE_REPORT
grep "Protein without " deleted.csv | wc -l >> $UPDATE_REPORT
echo -n "    - Protein transcript without any interactions: " >> $UPDATE_REPORT
grep "Protein transcript " deleted.csv | wc -l >> $UPDATE_REPORT
cat deleted.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Non uniprot proteins: " >> $UPDATE_REPORT
tail -n +2 non_uniprot.csv | wc -l >> $UPDATE_REPORT
#cat non_uniprot.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Uniprot remapping: " >> $UPDATE_REPORT
tail -n +2 protein_mapping.csv | wc -l >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins could be remapped to uniprot using sequence and/or identifiers: " >> $UPDATE_REPORT
cut -f 5 protein_mapping.csv | grep -v "-" | wc -l >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins without any identifiers or sequence: " >> $UPDATE_REPORT
grep "update_checking : FAILED" protein_mapping.csv | wc -l >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins need to be reviewed because there are conflicts in the remapping results when using the sequence separately from the identifiers (different uniprot entries can be remapped depending on the sequence and/or identifiers): " >> $UPDATE_REPORT
grep "update_checking : TO_BE_REVIEWED" protein_mapping.csv | wc -l >> $UPDATE_REPORT
grep "update_checking : TO_BE_REVIEWED" protein_mapping.csv >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins which could be remapped to several Swissprot entries using Uniprot Protein API sequence but need to be reviewed: " >> $UPDATE_REPORT
grep "UniprotProteinAPI_sequence_Swissprot : TO_BE_REVIEWED" protein_mapping.csv | wc -l >> $UPDATE_REPORT
grep "UniprotProteinAPI_sequence_Swissprot : TO_BE_REVIEWED" protein_mapping.csv >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins which could be remapped to several Trembl entries using Uniprot Protein API sequence but need to be reviewed: " >> $UPDATE_REPORT
grep "UniprotProteinAPI_sequence_Trembl : TO_BE_REVIEWED" protein_mapping.csv | wc -l >> $UPDATE_REPORT
grep "UniprotProteinAPI_sequence_Trembl : TO_BE_REVIEWED" protein_mapping.csv >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins which could be remapped to several uniprot entries using Uniprot Protein API accession but need to be reviewed: " >> $UPDATE_REPORT
grep "UniprotProteinAPI_accession : TO_BE_REVIEWED" protein_mapping.csv | wc -l >> $UPDATE_REPORT
grep "UniprotProteinAPI_accession : TO_BE_REVIEWED" protein_mapping.csv >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins which could be remapped to a Trembl entry using Uniprot Protein API but could match several Swissprot entries using the Blast: " >> $UPDATE_REPORT
grep "BLAST_Swissprot_Remapping : TO_BE_REVIEWED" protein_mapping.csv | wc -l >> $UPDATE_REPORT
grep "BLAST_Swissprot_Remapping : TO_BE_REVIEWED" protein_mapping.csv >> $UPDATE_REPORT
echo -n "    - Non uniprot proteins which could be remapped to a Trembl entry using Uniprot Protein API and have been successfully remap to a Swissprot entry using the Blast: " >> $UPDATE_REPORT
grep "BLAST_Swissprot_Remapping : COMPLETED" protein_mapping.csv | wc -l >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "- Update errors: " >> $UPDATE_REPORT
tail -n +2 process_errors.csv | wc -l >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Alias duplicates: " >> $UPDATE_REPORT
more process_errors.csv | grep alias_duplicates | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep alias_duplicates >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Annotations duplicates: " >> $UPDATE_REPORT
more process_errors.csv | grep annotations_duplicates | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep annotations_duplicates >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Both isoform and chain xrefs: " >> $UPDATE_REPORT
more process_errors.csv | grep both_isoform_and_chain_xrefs | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep both_isoform_and_chain_xrefs >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Dead parent xref: " >> $UPDATE_REPORT
more process_errors.csv | grep dead_parent_xref | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep dead_parent_xref >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Dead protein with transcripts not dead: " >> $UPDATE_REPORT
more process_errors.csv | grep dead_protein_with_transcripts_not_dead | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep dead_protein_with_transcripts_not_dead >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Dead uniprot ac: " >> $UPDATE_REPORT
more process_errors.csv | grep dead_uniprot_ac | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep dead_uniprot_ac >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Duplicated components: " >> $UPDATE_REPORT
more process_errors.csv | grep duplicated_components | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep duplicated_components >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Feature conflicts: " >> $UPDATE_REPORT
more process_errors.csv | grep feature_conflicts | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep feature_conflicts >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Impossible merge: " >> $UPDATE_REPORT
more process_errors.csv | grep impossible_merge | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep impossible_merge | awk '{print $1 "\t" $2 "\t" $3 "\t" $4}' >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Impossible protein remapping: " >> $UPDATE_REPORT
more process_errors.csv | grep impossible_protein_remapping | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep impossible_protein_remapping | awk '{print $1 "\t" $2}' >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Impossible transcript update: " >> $UPDATE_REPORT
more process_errors.csv | grep impossible_transcript_update | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep impossible_transcript_update | awk '{print $1 "\t" $2 "\t" $3 "\t" $4}' >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Impossible update master: " >> $UPDATE_REPORT
more process_errors.csv | grep impossible_update_master | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep impossible_update_master >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Invalid parent xref: " >> $UPDATE_REPORT
more process_errors.csv | grep invalid_parent_xref | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep invalid_parent_xref >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Multi uniprot identities: " >> $UPDATE_REPORT
more process_errors.csv | grep multi_uniprot_identities | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep multi_uniprot_identities >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Not matching protein transcript: " >> $UPDATE_REPORT
more process_errors.csv | grep not_matching_protein_transcript | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep not_matching_protein_transcript | awk '{print $1 "\t" $2 "\t" $3 "\t" $4}' >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Organism conflict with uniprot protein: " >> $UPDATE_REPORT
more process_errors.csv | grep organism_conflict_with_uniprot_protein | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep organism_conflict_with_uniprot_protein | awk '{print $1 "\t" $2 "\t" $3 "\t" $4}' >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Protein with ac null to delete: " >> $UPDATE_REPORT
more process_errors.csv | grep protein_with_ac_null_to_delete >> $UPDATE_REPORT
more process_errors.csv | grep protein_with_ac_null_to_delete | wc -l >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Several intact parents: " >> $UPDATE_REPORT
more process_errors.csv | grep several_intact_parents | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep several_intact_parents >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Several uniprot entries different organisms: " >> $UPDATE_REPORT
more process_errors.csv | grep several_uniprot_entries_different_organisms | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep several_uniprot_entries_different_organisms | awk '{print $1 "\t" $2}' >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Several uniprot entries same organism: " >> $UPDATE_REPORT
more process_errors.csv | grep several_uniprot_entries_same_organim | wc -l >> $UPDATE_REPORT
echo -n "Without duplicates:  " >> $UPDATE_REPORT
more process_errors.csv | grep several_uniprot_entries_same_organim | awk '{print $1 "\t" $2}' | sort -V -u | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep several_uniprot_entries_same_organim | awk '{print $1 "\t" $2}' | sort -V -u >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Transcript without parent: " >> $UPDATE_REPORT
more process_errors.csv | grep transcript_without_parent | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep transcript_without_parent >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Uniprot sequence null: " >> $UPDATE_REPORT
more process_errors.csv | grep uniprot_sequence_null | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep uniprot_sequence_null >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Uniprot sequence null intact sequence not null: " >> $UPDATE_REPORT
more process_errors.csv | grep uniprot_sequence_null_intact_sequence_not_null | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep uniprot_sequence_null_intact_sequence_not_null >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Xref duplicates: " >> $UPDATE_REPORT
more process_errors.csv | grep xref_duplicates | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep xref_duplicates >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo -n "Fatal error during update: " >> $UPDATE_REPORT
more process_errors.csv | grep fatal_error_during_update | wc -l >> $UPDATE_REPORT
more process_errors.csv | grep fatal_error_during_update | awk '{print $1 "\t" $2 "\t" $3}' >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo "- Error types found (check that all are in the previous section): " >> $UPDATE_REPORT
tail -n +2 process_errors.csv | cut -f 4 | sort -u >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT
