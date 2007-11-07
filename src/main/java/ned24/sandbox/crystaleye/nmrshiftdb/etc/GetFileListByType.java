package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.FileListTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;

public class GetFileListByType implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = HSR0_HALOGEN_NAME;
		//String protocolName = HSR1_HALOGEN_NAME;
		//String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		String protocolName = HSR1_HALOGEN_AND_MORGAN_NAME;
		
		String cmlDir = CML_DIR+protocolName;
		String folderName = "RMSD-vs-C";
		
		System.out.println(cmlDir);
		FileListTool ft = new FileListTool(cmlDir);
		ft.setIncludeNotRemoved(false);folderName+="_nr";
		//ft.setIncludeHumanEdited(true);folderName+="_he";
		//ft.setIncludeMisassigned(true);folderName+="_m";
		//ft.setIncludePoorStructures(true);folderName+="_ps";
		//ft.setIncludePossMisassigned(true);folderName+="_pm";
		ft.setIncludeTautomers(true);folderName+="_ta";
		//ft.setIncludeTooLargeRing(true);folderName+="_lr";
		List<File> fileList = ft.getFileList();
		for (File file : fileList) {
			String filename = file.getName();
			int idx = filename.indexOf(".");
			String name = filename.substring(0,idx);
			System.out.println("<a href='./html/hsr1_hal_morgan/"+name+"-misassignment/index.html'>"+name+"</a><br />");
		}
		System.out.println(fileList.size());
	}
}
