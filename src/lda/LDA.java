package lda;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;

import scvb.SCVB;
import util.HashMapSort;

public class LDA{
	int numDocs, numTermsInCorpus, numTopics, iter,miniBatchSize;
	private ArrayList<Term> vocabulary;
	SCVB scvb0;
	final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
	

	public LDA(int iter, int topics, int docs, int miniBatchSize){
		numDocs = docs;
		numTopics = topics;
		this.iter = iter;
		this.vocabulary = new ArrayList<Term>();
		this.miniBatchSize = miniBatchSize;
		
	}
	/* normalize probabilities to 1. used on each inference
	* iteration.
	* 
	*@returns: void
	*/

	/* reads file. Creates SCVB loop to parralellize
	* document parsing. Populates SCVB minibatch list.
	*
	*@returns: void
	*/
	public void parse(String file) 
			throws InterruptedException, ExecutionException, IOException{
		//provide file directory
		File fileDir = new File(file);
		System.out.println("Starting Vectorization of documents...");
		Vectorizer vect = new Vectorizer(this.numDocs);
		HashMap<Integer,Document> docs = vect.readAll(fileDir);
		this.numTermsInCorpus = docs.size();
		System.out.println("Creating Vocabulary...");
		createVocabulary(docs);
		System.out.println("Initializing SCVB...");
		this.scvb0 = new SCVB(this.iter, this.numTopics, 
				this.numDocs, this.numTermsInCorpus, this.vocabulary);
		System.out.println("Providing minibatches...");
		this.scvb0.minibatches = vect.createMiniBatches(docs,this.miniBatchSize,
				this.numDocs);
	}
	
	public void update(){
		System.out.println("LDA updates:");
		//for each iteration of LDA, run the scvb algorithm.
		for (int i=0; i<this.iter; i++){
			System.out.printf("    Iteration: %d", i+1);
			
			ExecutorService scvbService = Executors.newFixedThreadPool(NUM_THREADS);
			for (int m=0; m<this.scvb0.minibatches.size(); m++){
				scvbService.submit(scvb0);
			}
			scvbService.shutdown();
			try {
				scvbService.awaitTermination(365, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			scvb0.normalize();
			System.out.println("...... Done.");
		}
	}
	
	private void createVocabulary(HashMap<Integer,Document> docs){
		//assign wordIds.
		int count = 0;
		Set<String> tempWords = new HashSet<String>();
		for(Document doc : docs.values()){
			for (String word : doc.getTermTable().keySet()){
				tempWords.add(word);
			}
		}
		for (String word : tempWords){
			this.vocabulary.add(new Term(count,word,this.numTopics));
			count++;
		}
	}
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		
		LDA vec = new LDA(2,2,1,1);
		try {
			vec.parse("./text");
			vec.update();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println();
		System.out.print("Runtime: ");
		System.out.print(totalTime);
		
		
		util.PrintResults.printDocTopics(vec.getDocTopics());
		util.PrintResults.printTopicWords(vec.getTopicWords(5));
	}
	
	public ArrayList<Term> getVocabulary() {
		return this.vocabulary;
	}
	
	public void setTermList(ArrayList<Term> vocab) {
		this.vocabulary = vocab;
	}
	
	public double[][] getDocTopics(){
		double[][] results = new double[this.numDocs][this.numTopics];
		for(int d=0; d<this.scvb0.getD(); d++){
			for(int k=0; k<this.scvb0.getK(); k++){
				results[d][k] = this.scvb0.getnTheta()[d][k];
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
				justTopicResults.put(t.getWord(), prob);
			}
			HashMap<String,Double> topicResults = 
					HashMapSort.sortByValue(justTopicResults, topWords);
			results.add(topicResults);
		}
		return results;
	}
}

