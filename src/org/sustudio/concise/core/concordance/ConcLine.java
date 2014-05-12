package org.sustudio.concise.core.concordance;

import java.io.Serializable;

public class ConcLine implements Serializable {

	private static final long serialVersionUID = -5213719109742486995L;
	
	public int docId;
	public int wordId;
	private String left;
	private String right;
	private String node;
	private String docTitle;
	//private String docFilepath;
	
	public int getDocId() {
		return docId;
	}
	
	public void setDocId(int id) {
		this.docId = id;
	}
	
	public int getWordId() {
		return wordId;
	}
	
	public void setWordId(int id) {
		this.wordId = id;
	}
	
	public String getLeft() {
		return left == null ? "" : left;
	}
	
	public void setLeft(String left) {
		this.left = left.trim();
	}
	
	public String getRight() {
		return right == null ? "" : right;
	}
	
	public void setRight(String right) {
		this.right = right.trim();
	}
	
	public String getNode() {
		return node;
	}
	
	public void setNode(String node) {
		this.node = node.trim();
	}
	
	public String getDocTitle() {
		return docTitle;
	}
	
	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}
	
	public String toString() {
		return left + " " + Conc.preNodeTag + node + Conc.postNodeTag + " " + right;
	}

}
