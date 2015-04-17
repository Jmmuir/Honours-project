package application.model;

public class FileViewMovable extends FileView {
	
	private String originalSource;
	private String sourceDir;
	
	public final static String DROPBOXSOURCE = "Dropbox";
	public final static String PRIVATESOURCE = "Private";

	public FileViewMovable(String fileName, String fileSize, Boolean isFolder, String originalSource, String sourceDir) {
		super(fileName, fileSize, isFolder);
		this.originalSource = originalSource;
		this.sourceDir = sourceDir;
	}
	
	public FileViewMovable(FileView plain, String originalSource, String sourceDir) {
		super(plain.getFileName(), plain.getFileSize(), plain.isFolder());
		this.originalSource = originalSource;
		this.sourceDir = sourceDir;
	}
	
	public String getSource(){
		return this.originalSource;
	}
	
	public String getSourceDir(){
		return this.sourceDir;
	}

}
