package ned24.sandbox.crystaleye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import uk.ac.cam.ch.crystaleye.FileListing;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class ExtractDistances {
	
	private static String FILE_MIME = ".got";

	public static void main(String args[]) {
		Options options = new Options();
		Option option = null;

		option = new Option("f", "Parse single file");
		options.addOption(option);
		option = new Option("d", "Parse all files under directory");
		options.addOption(option);
		option = OptionBuilder.withArgName("FILE/DIRECTORY").hasArg().withDescription("Starting file or directory").create("p");
		options.addOption(option);

		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;

		try {
			commandLine = parser.parse(options, args);
		}
		catch(ParseException e) {
			e.printStackTrace();
			return;
		}

		String startPath = commandLine.getOptionValue("p");
		File startFile = new File(startPath);
		boolean parseFile = false;
		boolean parseDir = false;
		if (commandLine.hasOption("f")) {
			parseFile = true;
		} else if (commandLine.hasOption("d")) {
			parseDir = true;
		}
		
		if (parseFile) {
			processFile(startFile);
		} else if (parseDir) {
			List<File> fileList;
			try {
				fileList = FileListing.byMime(startFile, FILE_MIME);
				for (File file : fileList) {
					processFile(file);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}

	private static void processFile(File file) {
		BufferedReader input = null;

		Pattern p1 = Pattern.compile("\\s+\\d+\\s+(\\w+\\d+)\\s+\\w+(\\s+.+)$");
		Pattern p2 = Pattern.compile("\\s+(O4|Ca)\\s+\\w+\\s+\\d+\\s+\\d+\\.\\d+\\s+\\d+\\s+(\\d+\\.\\d+)\\s*$");
		StringBuilder sb = new StringBuilder();
		try {
			input = new BufferedReader(new FileReader(file));
			String line = null;
			boolean process = false;
			boolean additiveAtom = false;
			int dashLineNum = 0;
			String currentAtom = "";
			int i = 0;
			while (( line = input.readLine()) != null){
				if (line != null && !"".equals(line)) {
					if (line.contains("Cutoff for distances")) {
						process = true;
						continue;
					} else if (line.contains("General input information")) {
						process = false;
						continue;
					}
					if (process) {
						if (line.contains("--------------------------------------------------------------------------------")) {
							dashLineNum++;
							i = 0;
							additiveAtom = false;
							continue;
						}
						if (dashLineNum > 1) {
							if (i == 0) {
								Matcher m = p1.matcher(line);
								if (m.find()) {
									additiveAtom = true;
									currentAtom = m.group(1);
									line = m.group(2);
								}
							}
							if (additiveAtom) {
								Matcher m = p2.matcher(line);
								if (m.find()) {
									sb.append(currentAtom+","+m.group(1)+","+m.group(2)+" \n");
								}
							}
							i++;
						}
					}
				}
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new RuntimeException("Could not find file: "+file);
		}
		catch (IOException ex){
			throw new RuntimeException("Error reading file: "+file);
		}
		finally {
			try {
				if (input!= null) {
					input.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		String outPath = file.getAbsolutePath();
		int idx = outPath.indexOf(FILE_MIME);
		outPath = outPath.substring(0,idx)+".summary"+FILE_MIME;
		IOUtils.writeText(sb.toString(), outPath);
	}
}
