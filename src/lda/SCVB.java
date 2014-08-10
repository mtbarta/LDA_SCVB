package lda;

import java.lang.Math;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SCVB implements Runnable{
	public int iterations, K,W,D,burnIn;
	public int s, tau, rhoPhi_t, rhoTheta_t;
	public double kappa, rhoPhi, rhoTheta, alpha, eta;
	public double[][] nPhi, nTheta;
	public double[] nz;

	public BlockingQueue<Minibatch> minibatches;
	private ArrayList<Term> vocabulary;
	private HashMap<String,Integer> termDict;

	public SCVB (int iter, int numTopics, 
		int vocabSize, int numDocs, ArrayList<Term> Vocabulary) {
		// initialize paramters.
		iterations = iter;
		K = numTopics;
		W = vocabSize;
		D = numDocs;

		this.minibatches = new LinkedBlockingQueue<Minibatch>();
		this.vocabulary = Vocabulary;
		this.termDict = createTermDict(Vocabulary);

		s = 1;
		tau = 10;
		kappa = 0.9;

		rhoPhi_t = 1;
		rhoTheta_t = 1;
		rhoPhi = s / Math.pow((100 + rhoPhi_t), kappa);
		rhoTheta = s / Math.pow((10 + rhoTheta_t), kappa);

		alpha = 0.1;
		eta = 0.01;
		/* CVB0 statistics */
		//nPhi: the number of times a word appears across the corpus.
		nPhi = new double[W][K];
		//nTheta: the number of times a word appears in document D.
		nTheta = new double[D][K];
		//nz: the expected number of words assigned to a topic.
		nz = new double[K];
		//randomly initialize nPhi,nz, and nTheta
		//TODO: make everything atomic.
		for (int w = 0; w < W + 1; w++) {
			for (int k = 0; k < K; ++k) {
				nPhi[w][k] = ((Math.random() % (W * K))) / (W * K);
				nz[k] += nPhi[w][k];
			}
		}
		for (int d = 0; d < D + 1; d++) {
			for (int k = 0; k < K; ++k) {
				nTheta[d][k] = ((Math.random() % (D * K))) / (D * K);
			}
		}
	}
	@Override
	public void run() {
		Minibatch minibatch;
		try {
			minibatch = minibatches.take();
		
			//initialize variables
			double[][] nPhiHat = new double[W][K];
			double[] nzHat = new double[K];
			double[][] gamma = new double[W][K];
	
			//for each document in minibatch
			for (Document d : minibatch.docs){
				//complete burn in passes
				for( int b=0; b<burnIn; b++){
					rhoTheta = s / Math.pow((10 + rhoTheta_t), kappa);
					rhoTheta_t++;

					for(String t : d.termTable.keySet()){
						int tIndex = this.termDict.get(t);
						for(int k=0; k<K; k++){
							gamma[tIndex][k] = ((nPhi[tIndex][k] + eta) * (nTheta[d.docId][k] + alpha) / (nz[k] + eta * minibatch.M));
	
						nTheta[d.docId][k] = ((Math.pow((1 - rhoTheta), d.termTable.get(t)) * nTheta[d.docId][k])
								+ ((1 - Math.pow((1 - rhoTheta), d.termTable.get(t))) * d.Cj * gamma[tIndex][k]));
						}
					}
				}
				rhoTheta = s / Math.pow((10 + rhoTheta_t), kappa);
				rhoTheta_t++;

				for(String t : d.termTable.keySet()){
					int tIndex = this.termDict.get(t);
					for(int k=0; k<K; k++){
						gamma[tIndex][k] = ((nPhi[tIndex][k] + eta) * (nTheta[d.docId][k] + alpha) / (nz[k] + eta * minibatch.M));
	
					nTheta[d.docId][k] = ((Math.pow((1 - rhoTheta), d.termTable.get(t)) * nTheta[d.docId][k])
							+ ((1 - Math.pow((1 - rhoTheta), d.termTable.get(t))) * d.Cj * gamma[tIndex][k]));
					}
				}
				
				rhoPhi = s / Math.pow((100 + rhoPhi_t), kappa);
				rhoPhi_t++;
				for (int k = 0; k < K; k++) {
					for (int w = 0; w<W; w++) {
						nPhi[w][k] = ((1 - rhoPhi) * nPhi[w][k]) + (rhoPhi * nPhiHat[w][k]);
					}
					nz[k] = ((1 - rhoPhi) * nz[k]) + (rhoPhi * nzHat[k]);
				}
	
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private HashMap<String,Integer> createTermDict(ArrayList<Term> vocab){
		HashMap<String,Integer> termDict = new HashMap<String,Integer>();
		
		for (Term t : vocab){
			termDict.put(t.word,t.wordId);
		}
		return termDict;
	}
}
