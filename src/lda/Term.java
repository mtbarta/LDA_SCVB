package lda;


import java.util.List;
import java.util.ArrayList;

public class Term{
	int wordId;
	String word;
	ArrayList<Double> prob;
	public Term(int id, String vocabWord){
		wordId = id;
		word = vocabWord;
		prob = new ArrayList<Double>();
	}

}
