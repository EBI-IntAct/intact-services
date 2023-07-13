#!/bin/bash

#SBATCH --time=06-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p production   # partition(s)
#SBATCH --mem=6G   # memory per node
#SBATCH -J "PROTEIN_UPDATE"   # job name
#SBATCH -o "/nfs/production/hhe/intact/data/protein-update-report/protein-update-%j.out"   # job output file
#SBATCH --mail-type=ALL

##################################################################
# Runs the protein update
#
# Usage $0 database folder
#
#
##################################################################

if [ $# -ne 5 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 DATABASE_PROFILE[ebi-test, ebi-prod, etc] FOLDER BLAST[true, false] INPUT_FILE LOG_FILE"
      echo ""
      exit 1
fi

DATABASE=$1
FOLDER=$2
BLAST=$3
INPUT_FILE=$4
LOG=$5

rm -rf target
mkdir target

# Make sure we are using institution intact by default.
INTACT_OPTS="-Duk.ac.ebi.intact.INSTITUTION_LABEL=intact -Duk.ac.ebi.intact.AC_PREFIX=EBI"

mvn clean -U -X install -Pupdate-selection,${DATABASE},oracle -Dfolder=${FOLDER} -Dblast=${BLAST} -DinputFile=${INPUT_FILE} -Ddb=oracle -Dmaven.test.skip -Dmaven.repo.local=repository

./updateReport.sh ${FOLDER} ${LOG}
