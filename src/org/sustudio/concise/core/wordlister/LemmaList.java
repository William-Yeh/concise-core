package org.sustudio.concise.core.wordlister;

import java.util.ArrayList;
import java.util.Iterator;

public class LemmaList extends ArrayList<Lemma> {

	private static final long serialVersionUID = -3812239704583673606L;

	/**
	 * 傳回 lemma 是某個字的 {@link Lemma} 物件
	 * @param lemmaWord
	 * @return
	 */
	public Lemma get(String lemmaWord) {
		Iterator<Lemma> iter = iterator();
		while (iter.hasNext()) {
			Lemma lemma = iter.next();
			if ( lemmaWord.equals(lemma.getWord()) ) {
				return lemma;
			}
		}
		return null;
	}
	
	/**
	 * 忽略大小寫，傳回 lemma 是某個字的 {@link Lemma} 物件
	 * @param lemmaWord
	 * @return
	 */
	public Lemma getIgnoreCase(String lemmaWord) {
		lemmaWord = lemmaWord.toLowerCase();
		Iterator<Lemma> iter = iterator();
		while (iter.hasNext()) {
			Lemma lemma = iter.next();
			if ( lemmaWord.equals(lemma.getWord().toLowerCase()) ) {
				return lemma;
			}
		}
		return null;
	}
	
	
	/**
	 * 傳回含有 form 的 lemma 物件。如果找不到，則傳回 null
	 * @param form
	 * @return
	 */
	public Lemma getLemmaWithForm(String formWord) {
		Iterator<Lemma> iter = iterator();
		while (iter.hasNext()) {
			Lemma lemma = iter.next();
			if ( lemma.containsForm(formWord) ) {
				return lemma;
			}
		}
		return null;
	}
	
}
