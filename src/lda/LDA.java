package lda;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.io.IOException;

import util.HashMapSort;

public class LDA{
	int numDocs, numTerms, numTermsInCorpus, numTopics, iter,miniBatchSize;
	private Set<Term> vocabulary;
	SCVB scvb0;
	

	public LDA(int iter, int topics, int docs, int miniBatchSize){
		numDocs = docs;
		numTopics = topics;
		this.iter = iter;
		vocabulary = new HashSet<Term>();
		this.miniBatchSize = miniBatchSize;
	}
	/* normalize probabilities to 1. used on each inference
	* iteration.
	* 
	*@returns: void
	*/
	public void normalize(SCVB sampling){
		for(int k=0; k<sampling.K; k++){
			double k_tot = 0;
			for(Term term : this.vocabulary){
				k_tot = sampling.nPhi[term.wordId][k];
			}
			for(Term term : this.vocabulary){
				double temp = sampling.nPhi[term.wordId][k] / k_tot;
				sampling.nPhi[term.wordId][k] = temp;
				term.prob.add(temp);
			}
		}
		//make below concurrent
		for(int d=0; d<sampling.D+1; d++){
			double k_tot = 0;
			for(int k=0; k<sampling.K; k++){
				k_tot += sampling.nTheta[d][k];
			}
			for(int k=0; k<sampling.K; k++){
				double temp = sampling.nTheta[d][k] / k_tot;
				sampling.nTheta[d][k] = temp;
			}
		}
	}
	/* reads file. Creates SCVB loop to parralellize
	* document parsing. Populates SCVB minibatch list.
	*
	*@returns: void
	*/
	public void parse(String file) 
			throws InterruptedException, ExecutionException, IOException{
		//BufferedReader reader = new BufferedReader(new FileReader(file));

		this.scvb0 = new SCVB(this.iter, this.numTopics, 
				this.numTerms,this.numDocs,0);
		//find number of batches to run...
		/* for each batch, find out how many minibatches to run,
		* create minibatches, store to SCVB, and parallelize run.
		*/
		//provide file directory
		File fileDir = new File(file);
		Vectorizer vect = new Vectorizer(this.numDocs);
		HashMap<Integer,Document> docs = vect.readAll(fileDir);
		createVocabulary(docs);
		this.scvb0.minibatch = vect.createMiniBatches(docs,this.miniBatchSize,
				this.numDocs);
	}
	public void createVocabulary(HashMap<Integer,Document> docs){
		//assign wordIds.
		int count = 0;
		Set<String> tempWords = new HashSet<String>();
		for(Document doc : docs.values()){
			for (String word : doc.termTable.keySet()){
				tempWords.add(word);
			}
		}
		for (String word : tempWords){
			this.vocabulary.add(new Term(count,word));
			count++;
		}
	}
	public Set<Term> getVocabulary() {
		return this.vocabulary;
	}
	public void setTermList(Set<Term> vocab) {
		this.vocabulary = vocab;
	}
	public double[][] getDocTopics(){
		double[][] results = new double[this.numDocs][this.numTopics];
		for(int d=0; d<this.scvb0.D; d++){
			for(int k=0; k<this.scvb0.K; k++){
				results[d][k] = this.scvb0.nTheta[d][k];
			}
		}
		return results;
	}
	public ArrayList<HashMap<String,Double>> getTopicWords(int topWords){
		ArrayList<HashMap<String,Double>> results = 
				new ArrayList<HashMap<String,Double>>();
		
		for(int i=0; i<this.numTopics; i++){
			HashMap<String,Double> justTopicResults = new HashMap<String,Double>(this.vocabulary.size());
			for(Term t : this.vocabulary){
				double prob = t.prob.get(i);
				justTopicResults.put(t.word, prob);
			}
			HashMap<String,Double> topicResults = 
					HashMapSort.sortByValue(justTopicResults, topWords);
			results.add(topicResults);
		}
		return results;
	}
}

