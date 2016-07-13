#!/bin/sh

# Runs the imex report
#
# Usage $0 database folder
#
#
##################################################################

if [ $# -ne 1 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 FOLDER LOG_FILE"
      echo ""
      exit 1
fi

FOLDER=$1

UPDATE_REPORT=${FOLDER}/imex_update_report.txt

cd ${FOLDER}

echo "IMEx Update summary" >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo "- updated intact entries: " >> $UPDATE_REPORT
wc -l intact-update.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo "- New IMEx ids assigned : " >> $UPDATE_REPORT
wc -l new-imex-assigned.csv >> $UPDATE_REPORT
cat new-imex-assigned.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

echo "- Errors during the update: " >> $UPDATE_REPORT
wc -l imex-errors.csv >> $UPDATE_REPORT
cat imex-errors.csv >> $UPDATE_REPORT
echo "" >> $UPDATE_REPORT

cat ${UPDATE_REPORT}
