package scvb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import util.Vocabulary;

public class SCVB0 implements Runnable{
	private static int K, W, D, C;

	private static int burnIn;
	private static int s, tau;
	private static double kappa, alpha, eta;
	
	private static double[][] nPhi;
	private static double[][] nTheta;
	private static double[] nz;
	//private static double rhoPhi, rhoTheta;
	
	private ArrayList<Minibatch> minibatches;
	private static  AtomicInteger miniBatchCount = new AtomicInteger();
	
	private Vocabulary vocab;
	
	public SCVB0(int numTopics, int numDocs, int numWordsInCorpus, Vocabulary vocab){
		SCVB0.K = numTopics;
		SCVB0.W = vocab.size();
		SCVB0.D = numDocs;
		//C = number of words in the Corpus.
		SCVB0.C = numWordsInCorpus;
		
		this.vocab = vocab;
		this.minibatches = new ArrayList<Minibatch>();
		//step-size schedule parameters that are incorporated into rho.
		SCVB0.s = 1;
		SCVB0.tau = 10;
		SCVB0.kappa = 0.9;
		
		//hyperparams.
		SCVB0.alpha = 0.1;
		SCVB0.eta = 0.01;
		
		//nPhi: the number of times a word appears across the corpus.
		//topic term count count.
		nPhi = new double[W][K];
		//nTheta: the number of times a word appears in document D.
		//doc topic count.
		nTheta = new double[D][K];
		//nz: the expected number of words assigned to a topic.
		nz = new double[K];
		
		//randomly initialize nPhi, nTheta. nz is the sum of nPhi over words.
		for (int w = 0; w < W; w++) {
			for (int k = 0; k < K; ++k) {
				nPhi[w][k] = ((Math.random() % (W * K))) / (W * K);
				nz[k] += nPhi[w][k];
			}
		}
		for (int d = 0; d < D; d++) {
			for (int k = 0; k < K; ++k) {
				nTheta[d][k] = ((Math.random() % (D * K))) / (D * K);
			}
		}
		
	}
	public void run(){
		//initialize update variables
		double[][] nPhiHat = new double[W][K];
		double[] nzHat = new double[K];
		double[] gamma = new double[K];
				
		int bCount = SCVB0.miniBatchCount.intValue() % (this.minibatches.size() + 1);
		Minibatch minibatch = minibatches.get(bCount);
		//variables dependent on each minibatch
		//step-sizes and counts
		int rhoPhi_t = 1;
		int rhoTheta_t = 1;
		double rhoPhi = s / Math.pow((SCVB0.tau *10 + rhoPhi_t), SCVB0.kappa);
		double rhoTheta = s / Math.pow((SCVB0.tau + rhoTheta_t), SCVB0.kappa);
		
		//begin actual algorithm.
		for (int j=0; j<SCVB0.D; j++){
			//for each document j
			HashMap<String,Integer> docTerms = minibatch.docs.get(j).getTermTable();
			int docId = minibatch.docs.get(j).getDocId();
			int docCj = minibatch.docs.get(j).getCj();
			for (int burn=0; burn<SCVB0.burnIn; burn++){
				//for each specified burn in run
				for(String t : docTerms.keySet()){
					//for each token
					int termIndex = vocab.get(t);
					//update gamma and nTheta
					double gammaSum = 0.0;
					for(int k=0; k<K; k++){
						gamma[k] = ((SCVB0.nPhi[termIndex][k] + eta) * (SCVB0.nTheta[docId][k] + alpha) / (nz[k] + eta * SCVB0.W));
						gammaSum += gamma[k];
					}
					for(int k=0; k<K; K++){
						gamma[k] = gamma[k] / gammaSum;
						
						double clumpingCONST = Math.pow((1 - rhoTheta), docTerms.get(t));
						double partOne = clumpingCONST * SCVB0.nTheta[docId][k];
						double partTwo = (1 - clumpingCONST) * docCj * gamma[k];
						SCVB0.nTheta[docId][k] = partOne + partTwo;
					}
				}
			}
			//rhoTheta = s / Math.pow((10 + rhoTheta_t), kappa);
			rhoTheta_t++;

			for(String t : docTerms.keySet()){
				int termIndex = vocab.get(t);
				double gammaSum = 0.0;
				for(int k=0; k<K; k++){

					gamma[k] = (SCVB0.nPhi[termIndex][k] + eta) / (nz[k] + eta * SCVB0.W)* (SCVB0.nTheta[docId][k] + alpha);
					gammaSum += gamma[k];
				}
				for(int k=0; k<K; k++){
					gamma[k] = gamma[k] / gammaSum;
					
					double clumpingCONST = Math.pow((1 - rhoTheta), docTerms.get(t));
					double partOne = clumpingCONST * SCVB0.nTheta[docId][k];
					double partTwo = (1 - clumpingCONST) * docCj * gamma[k];
					SCVB0.nTheta[docId][k] = partOne + partTwo;
					
					//M = numWords in minibatch
					//nphihat is word topic counts hat.
					nPhiHat[termIndex][k] = nPhiHat[termIndex][k] + (C * gamma[k]); /// minibatch.getCj()
					nzHat[k] = nzHat[k] + (C * gamma[k]); /// minibatch.getCj()
				}
			}
			
			//minibatch processing is finished. update variables.
			rhoPhi = s / Math.pow((tau + rhoPhi_t), kappa);
			rhoPhi_t++;
			for (int k = 0; k < SCVB0.K; k++) {
				for (int w = 0; w<W; w++) {
					synchronized(SCVB0.nPhi){
						SCVB0.nPhi[w][k] = ((1 - rhoPhi) * SCVB0.nPhi[w][k]) + (rhoPhi * nPhiHat[w][k]);

					}
				}
				synchronized(SCVB0.nz){
					nz[k] = ((1 - rhoPhi) * nz[k]) + (rhoPhi * nzHat[k]);
				}
			}
		}
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public double getnPhi(int term, int topic) {
		return nPhi[term][topic];
	}
	public double getnTheta(int doc, int topic) {
		return nTheta[doc][topic];
	}
	public double getEta() {
		return eta;
	}
	public int getW() {
		return W;
	}
	public double getAlpha() {
		return alpha;
	}
	public static double getNz(int topic) {
		return nz[topic];
	}
	public int getMinibatchSize() {
		return this.minibatches.size();
	}
	public static void setTau(int tau) {
		SCVB0.tau = tau;
	}
	public static void setKappa(double kappa) {
		SCVB0.kappa = kappa;
	}
	public static void setAlpha(double alpha) {
		SCVB0.alpha = alpha;
	}
	public static void setEta(double eta) {
		SCVB0.eta = eta;
	}
	public void addMinibatch(Minibatch mb){
		this.minibatches.add(mb);
	}

}
