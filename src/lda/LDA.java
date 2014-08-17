package lda;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import reader.Reader;
import scvb.*;
import util.Document;
import util.Vocabulary;

public class LDA {
	private Vocabulary vocab;
	//TODO:store doc sizes separately since it's all that needs to be kept.
	private HashMap<Integer, Integer> docSizes;
	private static SCVB0 SCVB0;
	int numDocs, numWordsInCorpus, numTopics, iter;
	private String documentLocation;
	/*
	 * TODO: create functions to set optional variables below.
	 */
	private int NUM_THREADS;
	private boolean createVocabulary;
	private double setAlpha;
	private double setEta;
	private int batchSize;
	private String setVocabulary;
	private String processing;
	

	public LDA(int iter, int docs, int topics){
		numDocs = docs;
		numTopics = topics;
		this.iter = iter;
		
		createVocabulary = true;
		NUM_THREADS = 3;
		processing = "batch";
		batchSize = 100;
	}
	
	public void train(String dataLoc) 
			throws FileNotFoundException{
		this.documentLocation = dataLoc;
		Reader reader = new Reader();
		if(this.processing.equals("batch")){
			assert new File(this.documentLocation).listFiles().length == this.batchSize;
		}
		/*
		 * 
		 */
		if (this.createVocabulary == true){
			ArrayList<Document> docs = reader.readFilesFromDirectory(this.documentLocation);
			this.numWordsInCorpus = reader.getWordsInCorpus();
			this.docSizes = reader.getDocSizes();
			this.vocab = new Vocabulary(docs);
		}
		/*
		 * TODO: implement a way to read documents based off existing vocab.
		 */
		this.SCVB0 = new SCVB0(this.NUM_THREADS, this.numDocs,this.numWordsInCorpus , this.vocab);
		
	}
	public void update(){
		System.out.println("LDA updates:");
		//for each iteration of LDA, run the scvb algorithm.
		for (int i=0; i<this.iter; i++){
			System.out.printf("    Iteration: %d", i+1);
			if(this.NUM_THREADS > 1){
				ExecutorService scvbService = Executors.newFixedThreadPool(this.NUM_THREADS);
				for (int m=0; m<scvb.SCVB0.getMinibatchSize(); m++){
					scvbService.submit(SCVB0);
				}
				scvbService.shutdown();
				try {
					scvbService.awaitTermination(365, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//SCVB0.normalize();
				System.out.println("...... Done.");
			} else {
				SCVB0.run();
				//SCVB0.normalize();
				System.out.println("...... Done.");
			}
		}
	}
	/*
	 * computation below is unnormalized.
	 */
	public double computeTermTopicProb(String term, int topic){
		int termIndex = this.vocab.get(term);
		return (scvb.SCVB0.getnPhi(termIndex,topic) + scvb.SCVB0.getEta())
				/ (scvb.SCVB0.getNz(topic) + scvb.SCVB0.getEta() * scvb.SCVB0.getW());
	}

	public double computeDocTopicProb(int docId, int topic){
		return (scvb.SCVB0.getnTheta(docId,topic) + scvb.SCVB0.getAlpha())
				/ this.docSizes.get(docId) + this.numTopics * scvb.SCVB0.getAlpha();
	}
	public void processing(String var){
		if (var.equals("online")){
			this.processing = "online";
			this.NUM_THREADS = 1;
			this.batchSize = 1;
		}
		if (var.equals("batch")){
			this.processing = "batch";
			this.batchSize = this.numDocs;
		}
	}
	
	public void processing(String var, int batchSize){
		if (var.equals("online")){
			this.processing = "online";
			this.batchSize = 1;
		}
		if (var.equals("batch")){
			this.processing = "batch";
			this.batchSize = batchSize;
		}
	}
}
