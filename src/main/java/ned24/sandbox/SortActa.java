package ned24.sandbox;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class SortActa {
	
	public static void main(String[] args) throws IOException {
		String path = "/scratch/crystaleye-work/acta";
		
		for (File journalDir : new File(path).listFiles()) {
			for (File yearDir : journalDir.listFiles()) {
				for (File issueDir : yearDir.listFiles()) {
					String issueName = issueDir.getName();
					if (issueName.contains("-")) {
						continue;
					}
					String in = createActaIssue(issueName);
					File realIssueDir = new File(yearDir, in);
					for (File articleDir : issueDir.listFiles()) {
						String name = articleDir.getName();
						File realArticleDir = new File(realIssueDir, name);
						if (!realArticleDir.exists()) {
							System.out.println(articleDir);
							System.out.println(realArticleDir);
							System.out.println("=========================================");
							FileUtils.moveDirectory(articleDir, realArticleDir);
						}
					}
					
				}
			}
		}
	}
	
	private static String createActaIssue(String ss) {
		String issue = ss+"-00";
		if (issue.length() < 5) {
			issue = "0"+issue;
		}
		return issue;
	}

}
