package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

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
	
	public Vocabulary(String loc) throws FileNotFoundException{
		/* import vocab file, without building it from documents */
		File vocab = new File(loc);
		Scanner reader = new Scanner(vocab);
		while (reader.hasNext()){
			String line = reader.next();
			this.put(line, length);
			length++;
		}
		reader.close();
	}
	
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
