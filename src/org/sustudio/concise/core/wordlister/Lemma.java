package org.sustudio.concise.core.wordlister;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Lemma 物件
 * 
 * @author Kuan-ming
 *
 */
public class Lemma extends ArrayList<String> {

	private static final long serialVersionUID = -6795228919205580975L;

	/**
	 * constructor.
	 * @param word 作為lemma的基本詞
	 */
	public Lemma(String word) {
		super();
		add(word);
	}
	
	/**
	 * 改變lemma的基本詞
	 * @param word
	 */
	public void setWord(final String word) {
		set(0, word);
	}
	
	/**
	 * 傳回 lemma 的基本詞
	 * @return
	 */
	public String getWord() {
		return get(0);
	}
	
	/**
	 * 新增 form
	 * @param form
	 */
	public void addForm(final String form) {
		add(form);
	}
	
	/**
	 * 取代整個 form
	 * @param forms
	 */
	public void setForms(final List<String> forms) {
		final Iterator<String> iter = iterator();
		iter.next(); // the word
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		addAll(forms);
	}
	
	/**
	 * 傳回所有的 form
	 * @return
	 */
	public List<String> getForms() {
		return subList(1, size());
	}
	
	/**
	 * 不論大小寫，是否包含要測試的字詞
	 * @param word
	 * @return
	 */
	public boolean ignoreCaseContains(final String word) {
		if (contains(word))
			return true;
		for (String wordToTest : this) {
			if (wordToTest.toLowerCase().equals(word.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 這組lemma的form當中是否包含某個詞
	 * @param word
	 * @return
	 */
	public boolean containsForm(final String word) {
		return getForms().contains(word);
	}
	
	/**
	 * 不論大小寫，form裡頭是否包含要測試的字詞
	 * @param word
	 * @return
	 */
	public boolean ignoreCaseContainsForm(final String word) {
		if (containsForm(word))
			return true;
		for (String formToTest : getForms()) {
			if (formToTest.toLowerCase().equals(word.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 移除 form. 
	 * @param form
	 * @return
	 */
	public boolean removeForm(String form) {
		if (indexOf(form) > 0) {
			return remove(form);
		}
		return false;
	}
	
	public String toString() {
		return getWord() + " -> " + StringUtils.join(getForms(), ", ");
	}
}
