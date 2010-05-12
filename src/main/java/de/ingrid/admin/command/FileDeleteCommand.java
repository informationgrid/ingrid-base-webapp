package de.ingrid.admin.command;

import java.io.File;

/**
 * Class to add a file command for deletion of files or directories.
 *
 */
public class FileDeleteCommand extends Command{
	
	private String deleteFile;
	
	public FileDeleteCommand(String path){
		deleteFile = path;
	}
	
	/* (non-Javadoc)
	 * @see de.ingrid.admin.command.Command#execute()
	 */
	public void execute(){
		if(deleteFile != null){
			deleteMappingFilesFromDirectory(new File(deleteFile));
		}
	}
	
	/**
	 * Function to delete unused mapping files or directories.
	 * 
	 * @param filePath
	 */
	public void deleteMappingFilesFromDirectory(File filePath){
		if(filePath.isDirectory()){
			File[] files = filePath.listFiles();
			for(int i=0; i < files.length ;i++){
				if(files[i].isFile()){
					files[i].delete();
				}else{
					deleteMappingFilesFromDirectory(files[i]);
				}
			}
			filePath.delete();
		}else if(filePath.isFile()){
			filePath.delete();
		}
	}
}
