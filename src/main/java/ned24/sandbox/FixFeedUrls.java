package ned24.sandbox;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FixFeedUrls {
	
	public static void main(String[] args) throws IOException {
		String dirPath = "C:\\Users\\ned24.AD\\Desktop\\all";
		String newDirPath = "C:\\Users\\ned24.AD\\Desktop\\all-new\\";
		File dir = new File(dirPath);
		for (File feedFile : dir.listFiles()) {
			String feedContents = FileUtils.readFileToString(feedFile);
			feedContents = feedContents.replaceAll("http://wwmm.ch.cam.ac.uk/crystaleye/feed/atom/", "http://wwmm.ch.cam.ac.uk/crystaleye/feed/all/");
			FileUtils.writeStringToFile(new File(newDirPath+feedFile.getName()), feedContents);
		}
	}

}
