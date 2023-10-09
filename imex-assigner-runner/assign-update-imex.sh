#!/bin/bash

#SBATCH --time=06-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p production   # partition(s)
#SBATCH --mem=4G   # memory per node
#SBATCH -J "IMEX_ASSIGNER"   # job name
#SBATCH -o "/nfs/production/hhe/intact/data/imex-assigner-report/assign-update-imex-%j.out"   # job output file
#SBATCH --mail-user=intact-dev@ebi.ac.uk   # email address
#SBATCH --mail-type=ALL

MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

echo "Log directory $1"
echo "IMEx central username $2"
echo "IMEx central password $3"
echo "IMEx central endpoint $4"

MAVEN_PROFILE=$5

echo "use profile ${MAVEN_PROFILE}"

mvn -U clean install -Pexec,${MAVEN_PROFILE} -Dupdate.log.directory=$1 -Dic.username=$2 -Dic.password=$3 -Dic.endpoint=$4 -Dmaven.repo.local=repository -Dmaven.test.skip -Ddb=oracle

./updateReport.sh $1
