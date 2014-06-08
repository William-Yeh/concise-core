package org.sustudio.concise.core.cluster;

import java.util.Iterator;

import org.apache.lucene.store.Directory;
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
