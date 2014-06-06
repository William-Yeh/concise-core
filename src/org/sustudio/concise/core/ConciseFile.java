package org.sustudio.concise.core;

import java.io.File;

/**
 * 新增 workspace 在其中的 File 物件，方便調用
 * 
 * @author Kuan-ming Su
 *
 */
public class ConciseFile extends File {

	private static final long serialVersionUID = 8547249546187142581L;
	private final Workspace workspace;
	
	public ConciseFile(String pathname, Workspace workspace) {
		super(pathname);
		this.workspace = workspace;
	}
	
	public ConciseFile(String parent, String child, Workspace workspace) {
		super(parent, child);
		this.workspace = workspace;
	}
	
	public ConciseFile(File parent, String child, Workspace workspace) {
		super(parent, child);
		this.workspace = workspace;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
}
