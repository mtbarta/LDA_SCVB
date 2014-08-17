package util;

import java.util.ArrayList;
import java.util.HashMap;

public class Vocabulary extends HashMap<String, Integer> {
	/**
	 * wordId,Term pairs.
	 */
	private int length = 0;
	private static final long serialVersionUID = 1L;
	
	public Vocabulary(ArrayList<Document> docs){
		for(Document doc : docs){
			for (String term : doc.getTermTable().keySet()){
				if (!this.containsKey(term)){
					this.put(term, length);
					length++;
					
				} else{
					continue;
				}
			}
		}
	}
	/*
	 * overload constructor to update vocabulary on command.
	 */
	public Vocabulary(){}
	
	public void update(Document doc){
		for (String term : doc.getTermTable().keySet()){
			if (!this.containsKey(term)){
				this.put(term, length);
				length++;
			} else{
				continue;
			}
		}
	}
	
}
