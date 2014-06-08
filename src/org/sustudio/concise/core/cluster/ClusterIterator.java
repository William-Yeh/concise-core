package org.sustudio.concise.core.cluster;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sustudio.concise.core.Workspace;

public abstract class ClusterIterator implements Iterator<Cluster>, Iterable<Cluster> {

	protected Cluster nextCluster;
	
	/** 暫存的工作目錄，ClusterIterator結束後應該刪除 */
	protected Directory temporaryDirectory;
	
	public ClusterIterator(Workspace workspace) {
		temporaryDirectory = workspace.getTempDirectory();
	}
	
	public Iterator<Cluster> iterator() {
		return this;
	}
	
	public boolean hasNext() {
		return nextCluster != null;
	}
	
	/**
	 * DO NOT USE.
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove() is not supported.");
	}
	
	/**
	 * 關閉並刪除暫存目錄
	 * @throws IOException
	 */
	protected void closeTemporaryDirectory() throws IOException {
		if (temporaryDirectory instanceof FSDirectory) {
			File tmpdir = ((FSDirectory) temporaryDirectory).getDirectory();
			FileUtils.deleteDirectory(tmpdir);
		}
		temporaryDirectory.close();
	}
	
	//
	// This is an alternative option
	//
	/*
	protected File tmpFile;
	protected File outputFile;
	
	protected void consoleJob() throws Exception {
		ProcessBuilder pb = new ProcessBuilder(
				"/bin/sh", "-c", 
				"cat " + tmpFile.getCanonicalPath() + 
				" | sort -n | uniq -c | sort -nr" +
						" > " + outputFile.getCanonicalPath());
		Process p = pb.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while (br.read() != -1) {
			
			// wait...
			Thread.sleep(1000);
		}
		br.close();
		p.destroy();
	}
	*/
	
}
