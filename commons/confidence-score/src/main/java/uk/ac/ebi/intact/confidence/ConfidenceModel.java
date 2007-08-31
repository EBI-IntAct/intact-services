/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.confidence.attribute.ClassifierInputWriter;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
import uk.ac.ebi.intact.confidence.util.DataMethods;

/**
 * TODO
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 29 Aug 2007
 * </pre>
 */
public class ConfidenceModel {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log		log	= LogFactory.getLog(ConfidenceModel.class);
	private String				dirPath;
	private MaxEntClassifier	classifier;

	public ConfidenceModel(){
	}
	public ConfidenceModel(String path) {
		if (path == null) {
			throw new NullPointerException();
		}
		dirPath = path;
	}

//	public static void main(String[] args) {
//		ConfidenceModel cm = new ConfidenceModel("E:\\tmp\\");
//		cm.buildModel();
//	}

	public void buildModel() {
		long start = System.currentTimeMillis();
		getConfidenceListsFromDb();
		long aux1 = System.currentTimeMillis();
		long timeDb = aux1 - start;
		log.info("time for db retrieve (milisec): " + timeDb);

		aux1 = System.currentTimeMillis();
		generateLowconf(10000);
		long aux2 = System.currentTimeMillis();
		long timeGenerate = aux2 - aux1;
		log.info("time for generating lowconf (milisec): " + timeGenerate);

		aux1 = System.currentTimeMillis();
		getInterProGoAndAlign();
		aux2 = System.currentTimeMillis();
		long timeAttribs = aux2 - aux1;
		log.info("time for getting the attributes (milisec): " + timeAttribs);

		aux1 = System.currentTimeMillis();
		createTadmClassifierInput();
		runTadm();
		createModel();
		aux2 = System.currentTimeMillis();
		long timeCreateModel = aux2 - aux1;
		log.info("time for training the model (milisec): " + timeCreateModel);

		aux1 = System.currentTimeMillis();
		classifyMedConfSet();
		long stop = System.currentTimeMillis();

		log.info("time for db read (milisec): " + timeDb);
		log.info("time to generate lowconf (milisec): " + timeGenerate);
		log.info("time for getting the attributes (milisec): " + timeAttribs);
		log.info("time for training the model (milisec): " + timeCreateModel);
		log.info("time for classifying the medconf set (milisec): " + (stop - aux1));
		log.info("total time in milisec: " + (stop - start));
	}

	private void getConfidenceListsFromDb() {
		IntactDbRetriever intactdb = new IntactDbRetriever();
		long start = System.currentTimeMillis();

		try {
			// TODO: replace with a proper way of writing to files
			String fileName = dirPath + "medconf_all.txt";
			FileWriter fw = new FileWriter(fileName);
			intactdb.retrieveMediumConfidenceSet(fw);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		log.info("time needed : " + (end - start));
		List<InteractionSimplified> highconf = intactdb.retrieveHighConfidenceSet();

		DataMethods dm = new DataMethods();
		highconf = dm.expand(highconf, new SpokeExpansion());
		// TODO: replace with a proper way of writing to files
		String filepath = dirPath + "highconf_all.txt";
		dm.export(highconf, new File(filepath), true);
	}

	private void generateLowconf(int nr) {
		DataMethods dm = new DataMethods();
		File inFile = new File(ConfidenceModel.class.getResource("40.S_cerevisiae.fasta").getFile());
		HashSet<String> yeastProteins = dm.readFasta(inFile, null);
		try {
			BinaryInteractionSet highConfBiSet = new BinaryInteractionSet(dirPath + "highconf_all.txt");
			BinaryInteractionSet medConfBiSet = new BinaryInteractionSet(dirPath + "medconf_all.txt");
			Collection<ProteinPair> all = highConfBiSet.getSet();
			all.addAll(medConfBiSet.getSet());
			BinaryInteractionSet forbidden = new BinaryInteractionSet(all);
			BinaryInteractionSet lowConf = dm.generateLowConf(yeastProteins, forbidden, nr);

			dm.export(lowConf, new File(dirPath + "lowconf_all.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getInterProGoAndAlign() {
		try {
			BinaryInteractionSet biSet = new BinaryInteractionSet(dirPath + "highconf_all.txt");
			AttributeGetter aG = new AttributeGetter(dirPath + "uniprot_sprot.dat", biSet);
			aG.setTmpDir(dirPath);
			biSet = new BinaryInteractionSet(dirPath + "highconf_all.txt");
			HashSet<String> againstProteins = biSet.getAllProtNames();
			aG.getAllAttribs(biSet, againstProteins, dirPath + "highconf_all_attribs.txt");

			biSet = new BinaryInteractionSet(dirPath + "medconf_all.txt");
			aG.getAllAttribs(biSet, againstProteins, dirPath + "medconf_all_attribs.txt");

			biSet = new BinaryInteractionSet(dirPath + "lowconf_all.txt");
			aG.getAllAttribs(biSet, againstProteins, dirPath + "lowconf_all_attribs.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createTadmClassifierInput() {
		try {
			ClassifierInputWriter ciw = new ClassifierInputWriter(dirPath + "highconf_all_attribs.txt", dirPath
					+ "lowconf_all_attribs.txt", dirPath + "tadm.input", "TADM");
			ciw.writeAttribList(dirPath + "all_attribs.txt");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void runTadm() {
		String cmd = "tadm -events_in " + dirPath + "tadm.input" + " -params_out " + dirPath + "weights.txt";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createModel() {
		try {
			classifier = new MaxEntClassifier(dirPath + "all_attribs.txt", dirPath + "weights.txt");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void classifyMedConfSet() {
		File file = new File(dirPath + "medconf_all_attribs.txt");
		BufferedReader br;
		try {
			FileWriter fw = new FileWriter(new File(dirPath + "medconf_score.txt"));

			br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				double tScore = classifier.trueScoreFromLine(line);
				String[] str = line.split(",");
				fw.append(str[0] + ": " + tScore);
			}
			fw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
