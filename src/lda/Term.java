package lda;


import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Term{
	private int wordId;
	private String word;
	List<Double> prob;
	public Term(int id, String vocabWord){
		setWordId(id);
		setWord(vocabWord);
		//TODO: need to check for value. concurrent access.
		prob = Collections.synchronizedList( new ArrayList<Double>());
	}
	
	public Term(int id, String vocabWord, int numTopics){
		setWordId(id);
		setWord(vocabWord);
		//TODO: need to check for value. concurrent access.
		prob = Collections.synchronizedList( new ArrayList<Double>(numTopics));
		for(int i=0; i<numTopics; i++){
			prob.add(0.0);
		}
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getWordId() {
		return wordId;
	}
	public void setWordId(int wordId) {
		this.wordId = wordId;
	}

	public List<Double> getProb() {
		return prob;
	}

	public void setProb(int index, double val) {
		this.prob.set(index, val);
	}
	public void addProb(double val){
		this.prob.add(val);
	}
}
