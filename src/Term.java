package LDA;

import java.util.List;
import java.util.ArrayList;

public class Term {
	int wordId;
	String word;
	List<Double> prob;
	public Term(int id, String vocabWord){
		wordId = id;
		word = vocabWord;
		prob = new ArrayList<Double>();
	}
}

