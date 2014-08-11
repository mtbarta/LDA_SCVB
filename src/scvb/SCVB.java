package scvb;
/*
 * A lot of variables named based on the paper:
 * 
 * James Foulds, Levi Boyles, Christopher DuBois, Padhraic Smyth, and Max Welling. 
 * 2013. Stochastic collapsed variational Bayesian inference for latent 
 * Dirichlet allocation. In Proceedings of the 19th ACM SIGKDD international 
 * conference on Knowledge discovery and data mining (KDD '13), 
 * Inderjit S. Dhillon, Yehuda Koren, Rayid Ghani, Ted E. Senator, Paul Bradley,
 * Rajesh Parekh, Jingrui He, Robert L. Grossman, and Ramasamy Uthurusamy
 * (Eds.). ACM, New York, NY, USA, 446-454. DOI=10.1145/2487575.2487697 
 * http://doi.acm.org/10.1145/2487575.2487697
 */
import java.lang.Math;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lda.Document;
import lda.Minibatch;
import lda.Term;

public class SCVB implements Runnable{
	protected int iterations;
	private final int K;
	private final int W;
	private final int D;
	private final int C;
	protected int burnIn;
	private int s, tau, rhoPhi_t, rhoTheta_t;
	private double kappa, rhoPhi, rhoTheta, alpha, eta;
	private double[][] nPhi;
	private double[][] nTheta;
	protected double[] nz;

	private ArrayList<Term> vocabulary;
	public BlockingQueue<Minibatch> minibatches;
	private HashMap<String,Integer> termDict;


	public SCVB (int iter, int numTopics, 
		int numDocs, int numTermsInCorpus, ArrayList<Term> Vocabulary) {
		// initialize parameters.
		iterations = iter;
		this.K = numTopics;
		this.W = Vocabulary.size();
		this.D = numDocs;
		this.C = numTermsInCorpus;

		this.vocabulary = Vocabulary;
		this.minibatches = new LinkedBlockingQueue<Minibatch>();
		this.termDict = createTermDict(Vocabulary);
		
		//step-size schedule parameters that are incorporated into rho.
		this.s = 1;
		this.tau = 10;
		this.kappa = 0.9;
		
		// _t variables below are for each doc iteration in the minibatch.
		this.rhoPhi_t = 1;
		this.rhoTheta_t = 1;
		this.rhoPhi = s / Math.pow((this.tau + this.rhoPhi_t), this.kappa);
		this.rhoTheta = s / Math.pow((this.tau + this.rhoTheta_t), this.kappa);
		
		//hyperparams.
		this.alpha = 0.1;
		this.eta = 0.01;
		
		/* CVB0 statistics */
		
		//TODO: no concurrency check on below variables. Make sure values update properly.
		
		//nPhi: the number of times a word appears across the corpus.
		setnPhi(new double[W][K]);
		//nTheta: the number of times a word appears in document D.
		setnTheta(new double[getD()][K]);
		//nz: the expected number of words assigned to a topic.
		nz = new double[K];
		
		//randomly initialize nPhi,nz, and nTheta
		for (int w = 0; w < W; w++) {
			for (int k = 0; k < K; ++k) {
				getnPhi()[w][k] = ((Math.random() % (W * K))) / (W * K);
				nz[k] += getnPhi()[w][k];
			}
		}
		for (int d = 0; d < D; d++) {
			for (int k = 0; k < K; ++k) {
				getnTheta()[d][k] = ((Math.random() % (D * K))) / (D * K);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * Each time scvb0 is called from LDA, it processes a minibatch.
	 * Done in parallel at the batch level to avoid some concurrency issues.
	 */
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
					rhoTheta = s / Math.pow((this.tau + rhoTheta_t), kappa);
					rhoTheta_t++;

					for(String t : d.getTermTable().keySet()){
						int tIndex = this.termDict.get(t);
						for(int k=0; k<K; k++){
							gamma[tIndex][k] = ((this.nPhi[tIndex][k] + eta) * (this.nTheta[d.getDocId()][k] + alpha) / (nz[k] + eta * minibatch.getM()));
	
						this.nTheta[d.getDocId()][k] = ((Math.pow((1 - rhoTheta), d.getTermTable().get(t)) * this.nTheta[d.getDocId()][k])
								+ ((1 - Math.pow((1 - rhoTheta), d.getTermTable().get(t))) * d.getCj() * gamma[tIndex][k]));
						}
					}
				}
				rhoTheta = s / Math.pow((10 + rhoTheta_t), kappa);
				rhoTheta_t++;

				for(String t : d.getTermTable().keySet()){
					int tIndex = this.termDict.get(t);
					for(int k=0; k<K; k++){
						gamma[tIndex][k] = ((this.nPhi[tIndex][k] + eta) * (this.nTheta[d.getDocId()][k] + alpha) / (nz[k] + eta * minibatch.getM()));
	
						getnTheta()[d.getDocId()][k] = ((Math.pow((1 - rhoTheta), d.getTermTable().get(t)) * this.nTheta[d.getDocId()][k])
							+ ((1 - Math.pow((1 - rhoTheta), d.getTermTable().get(t))) * d.getCj() * gamma[tIndex][k]));
					
						nPhiHat[tIndex][k] = nPhiHat[tIndex][k] + (C * gamma[tIndex][k]/ minibatch.getM());
						nzHat[k] = nzHat[k] + (C * gamma[tIndex][k]/ minibatch.getM());
					}
				}
				//minibatch processing is finished. update variables.
				rhoPhi = s / Math.pow((tau + rhoPhi_t), kappa);
				rhoPhi_t++;
				for (int k = 0; k < getK(); k++) {
					for (int w = 0; w<W; w++) {
						synchronized(this.nPhi){
							this.nPhi[w][k] = ((1 - rhoPhi) * this.nPhi[w][k]) + (rhoPhi * nPhiHat[w][k]);
						}
					}
					synchronized(this.nz){
						nz[k] = ((1 - rhoPhi) * nz[k]) + (rhoPhi * nzHat[k]);
					}
				}
	
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void normalize(){
		for(int k=0; k<this.getK(); k++){
			double k_tot = 0;
			for(Term term : this.vocabulary){
				k_tot += this.nPhi[term.getWordId()][k];
			}
			for(Term term : this.vocabulary){
				double temp = nPhi[term.getWordId()][k] / k_tot;
				nPhi[term.getWordId()][k] = temp;
//				if (term.prob.size() < k){
//					term.prob.add(-1.0);
//				}
				term.setProb(k,temp);
			}
		}
		//make below concurrent?
		for(int d=0; d< D; d++){
			double k_tot = 0;
			for(int k=0; k<K; k++){
				k_tot += this.nTheta[d][k];
			}
			for(int k=0; k<K; k++){
				double temp = this.nTheta[d][k] / k_tot;
				this.nTheta[d][k] = temp;
			}
		}
	}
	/*
	 * Creates word,wordID pairs for easy matching to the results.
	 */
	private HashMap<String,Integer> createTermDict(ArrayList<Term> vocab){
		HashMap<String,Integer> termDict = new HashMap<String,Integer>();
		
		for (Term t : vocab){
			String word = t.getWord();
			int wordID = t.getWordId();
			
			termDict.put(word, wordID);
		}
		return termDict;
	}
	
	//GETTERS AND SETTERS
	
	public int getBurnIn() {
		return burnIn;
	}

	public void setBurnIn(int burnIn) {
		this.burnIn = burnIn;
	}

	public int getS() {
		return s;
	}

	public void setS(int s) {
		this.s = s;
	}

	public int getTau() {
		return tau;
	}

	public void setTau(int tau) {
		this.tau = tau;
	}

	public double getKappa() {
		return kappa;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getEta() {
		return eta;
	}

	public void setEta(double eta) {
		this.eta = eta;
	}

	public int getK() {
		return K;
	}

	public double[][] getnPhi() {
		return nPhi;
	}

	public void setnPhi(double[][] nPhi) {
		this.nPhi = nPhi;
	}

	public int getD() {
		return D;
	}


	public double[][] getnTheta() {
		return nTheta;
	}

	public void setnTheta(double[][] nTheta) {
		this.nTheta = nTheta;
	}
}
