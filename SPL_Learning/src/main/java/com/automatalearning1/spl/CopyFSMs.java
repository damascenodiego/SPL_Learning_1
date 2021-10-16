package com.automatalearning1.spl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.google.common.io.Files;

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.SimpleConfiguration;
import uk.le.ac.fts.FtsUtils;

public class CopyFSMs {

	// Finds FSMs of all configurations available in a folder and copies them in the
	// same folder with a name related to the corresponding configuration file.
	private static final String HELP = "h";
	public static final String FSM = "fsm";
	public static final String DIR = "dir";
	public static final String PNAME = "pname";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption(HELP) || !line.hasOption(FSM) || !line.hasOption(DIR)) {
				formatter.printHelp("CreateFSMs", options);
				System.exit(0);
			}
			
			String p_name = new String(line.getOptionValue(PNAME));
			String[] f_list_1 = new String[100];
			String[] f_list_2 = new String[100];
			
			switch(p_name) {
			case "ws":
				f_list_1 = new String[] {"sLow", "wLow", "sHigh", "wHigh", "PermWiper"};
				f_list_2 = new String[] {"sL", "wL", "sH", "wH", "pW"};
				break;
			default:
				break;
			}
			
			List<String> all_features = Arrays.asList(f_list_2);

			File fsm_dir = new File(line.getOptionValue(FSM));

			File configs_dir = new File(line.getOptionValue(DIR));

			File[] configFilesList = configs_dir.listFiles();
			File[] fsmFilesList = fsm_dir.listFiles();
			
			for (int i = 0; i < configFilesList.length; i++) {
				File configFile = configFilesList[i];
				String config = configFile.getPath();

				String fileExtension = "";
				String fileName = configFile.getName();
				int j = fileName.lastIndexOf('.');
				if (j >= 0) {
					fileExtension = fileName.substring(j + 1);
				}

				if (fileExtension.equals("config")) {
//					System.out.println(config);
					SimpleConfiguration config_i = FtsUtils.getInstance().loadConfiguration(config);
					Feature[] config_i_features = config_i.getFeatures();
					List<String> features_i = new ArrayList<>();
					for (Feature f : config_i_features) {
						features_i.add(f.toString());
					}
					
					System.out.println("i: " + i);
					System.out.println(features_i);
					
					for (int s = 0; s < features_i.size(); s++) {
						String f_1 = features_i.get(s);
						String f_2 = getFeatureName(f_1, f_list_1, f_list_2);
						features_i.set(s, f_2);
					}
					
					System.out.println(features_i);
					
					for (int k = 0; k < fsmFilesList.length; k++) {
						File fsmFile = fsmFilesList[k];
						String fsmFileName = fsmFile.getName();
						if(fsmFileName.endsWith("txt")) {
							BufferedReader br = new BufferedReader(new FileReader(fsmFile));
							String line_text = br.readLine();
							String[] features = line_text.split("\t");
//							System.out.println(Arrays.toString(features));
							
							List<String> features_k = new ArrayList<>();
							for (String f: features) {
								if(Arrays.asList(f_list_2).contains(f) && !f.contains("not")) {
									features_k.add(f);
								}
							}
//							System.out.println(features_k);
							double similarity_score =  ConfigurationSimilarity(features_i, features_k, all_features);
							if (similarity_score == 1) {
								System.out.println(fsmFileName);
								File new_file = new File(configFile.toString().replace(".config", "_text.txt"));
								Files.copy(fsmFile, new_file);
								break;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Finished!");

	}

	private static double ConfigurationSimilarity(List<String> features_i_1, List<String> features_j_1,
			List<String> all_features_1) {
		// TODO Auto-generated method stub
		List<String> intersection_i_j = Intersection(features_i_1, features_j_1);

		List<String> all_minus_i = Difference(all_features_1, features_i_1);

		List<String> all_minus_j = Difference(all_features_1, features_j_1);

		List<String> intersection_remained = Intersection(all_minus_i, all_minus_j);

		double similarity = (intersection_i_j.size() + intersection_remained.size()) / ((double) all_features_1.size());
//		System.out.println(similarity);

		return similarity;
	}

	private static List<String> Intersection(List<String> list_1, List<String> list_2) {
		List<String> intersection_list = new ArrayList<>();
		for (String f : list_1) {
			if (list_2.contains(f)) {
				intersection_list.add(f);
			}
		}
		return intersection_list;
	}

	private static List<String> Difference(List<String> list_1, List<String> list_2) {
		List<String> difference_list = new ArrayList<>();
		for (String f : list_1) {
			if (!list_2.contains(f)) {
				difference_list.add(f);
			}
		}
		return difference_list;
	}

	private static String getFeatureName(String featureName_1, String[] feature_list_1, String[] feature_list_2) {
		// TODO Auto-generated method stub
		String featureName_2 = "";
		for (int i = 0; i < feature_list_1.length; i++) {
			if (feature_list_1[i].equals(featureName_1)) {
				featureName_2 = feature_list_2[i];
			}
		}
		return featureName_2;
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(HELP, false, "Help menu");
		options.addOption(FSM, true, "Directory containing all FSMs (FSMs of all possible products)");
		options.addOption(DIR, true, "Directory of the config files");
		options.addOption(PNAME, true, "Project name");
		return options;
	}

}
