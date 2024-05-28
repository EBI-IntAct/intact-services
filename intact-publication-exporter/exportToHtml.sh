#!/bin/bash

#SBATCH --time=06-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p production   # partition(s)
#SBATCH --mem=4G   # memory per node
#SBATCH -J "HTML_EXPORT"   # job name
#SBATCH -o "/nfs/production/hhe/intact/data/intact-publication-exporter-logs/export-html-%j.out"   # job output file
#SBATCH --mail-user=intact-dev@ebi.ac.uk   # email address
#SBATCH --mail-type=ALL

MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

echo "Publication xml file $1"

MAVEN_PROFILE=$2

echo "use profile ${MAVEN_PROFILE}"

mvn -U clean install -Pexport-html,${MAVEN_PROFILE} -DxmlFile=$1 -Dmaven.repo.local=repository -Dmaven.test.skip -Ddb=postgres