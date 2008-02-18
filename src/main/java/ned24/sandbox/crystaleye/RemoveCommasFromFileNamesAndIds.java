package ned24.sandbox.crystaleye;

import java.io.File;

public class RemoveCommasFromFileNamesAndIds {

	String dataPath;

	private RemoveCommasFromFileNamesAndIds() {
		;
	}

	public RemoveCommasFromFileNamesAndIds(String dataPath) {
		this.dataPath = dataPath;
	}

	public void run() {
		File dataFile = new File(dataPath);
		for (File publisherFile : dataFile.listFiles()) {			
			for (File journalFile : publisherFile.listFiles()) {
				for (File yearFile : journalFile.listFiles()) {
					for (File issueFile : yearFile.listFiles()) {
						for (File articleFile : issueFile.listFiles()) {
							for (File folder : articleFile.listFiles()) {
								if (folder.isDirectory()) {
									for (File f : folder.listFiles()) {
										stripCommas(f);
									}
								}
							}
						}
					}
				}
			}
		}

		/*
			List<File> fileList = FileListing.byRegex(new File(dataPath), "[^\\.]*\\.complete\\.cml\\.xml");
			List<File> commaFiles = new ArrayList<File>();
			for (File file : fileList) {
				String path = file.getAbsolutePath();
				if (path.contains(",")) {
					File parent = file.getParentFile();
					File n = new File(parent.getAbsolutePath().replaceAll(",", "-"));
					parent.renameTo(n);
					commaFiles.add(n);
				}				
			}

			System.out.println("======================================================================");
			for (File file : commaFiles) {
				System.out.println(file.getAbsolutePath());
			}
			System.out.println("======================================================================");

			for (File file : commaFiles) {
				List<File> files = FileListing.byRegex(file, ".*");
				for (File f : files) {
					String path = f.getAbsolutePath();
					if (path.contains(",")) {
						if (path.endsWith(".complete.cml.xml") || path.endsWith(".raw.cml.xml")) {
							CMLCml cml = (CMLCml)IOUtils.parseCmlFile(f).getRootElement();
							String cmlId = cml.getId();
							cmlId = cmlId.replaceAll(",", "-");
							cml.setId(cmlId);

							CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
							String i = molecule.getId();
							i = i.replaceAll(",", "-");
							molecule.setId(i);

							for (CMLMolecule mol : molecule.getDescendantsOrMolecule()) {
								String id = mol.getId();
								id = id.replaceAll(",", "-");
								mol.setId(id);
							}
							IOUtils.writeXML(cml.getDocument(), f.getAbsolutePath());
						}

						System.out.println("RENAMING :"+path);
						f.renameTo(new File(path.replaceAll(",", "-")));
					}
				}
			}
		 */
		System.out.println("*******======= FINISHED");
	}

	private void stripCommas(File f) {
		String fPath = f.getAbsolutePath();
		if (fPath.contains(",")) {
			/*
			if (fPath.endsWith(".complete.cml.xml")) {
				CMLCml cml = (CMLCml)IOUtils.parseCmlFile(f).getRootElement();
				String cmlId = cml.getId();
				cmlId = cmlId.replaceAll(",", "-");
				cml.setId(cmlId);

				CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
				String i = molecule.getId();
				i = i.replaceAll(",", "-");
				molecule.setId(i);

				for (CMLMolecule mol : molecule.getDescendantsOrMolecule()) {
					String id = mol.getId();
					id = id.replaceAll(",", "-");
					mol.setId(id);
				}
				IOUtils.writeXML(cml.getDocument(), fPath);
			}
			*/

			fPath = fPath.replaceAll(",", "-");
			System.out.println("renaming "+f.getAbsolutePath());
			f.renameTo(new File(fPath));
		}
	}

	public static void main(String[] args) {
		RemoveCommasFromFileNamesAndIds remo = new RemoveCommasFromFileNamesAndIds("e:/crystaleye-test/data");
		remo.run();
	}
}
