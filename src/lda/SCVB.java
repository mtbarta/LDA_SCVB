package lda;

import java.lang.Math;
import java.util.List;
import java.util.ArrayList;

public class SCVB {
	public int iterations, K,W,D,C,burnIn;
	public int s, tau, rhoPhi_t, rhoTheta_t;
	public double kappa, rhoPhi, rhoTheta, alpha, eta;
	public double[][] nPhi, nTheta;
	public double[] nz;

	public List<Minibatch> minibatch;

	public SCVB (int iter, int numTopics, 
		int vocabSize, int numDocs, int corpusSize) {
		// initialize paramters.
		iterations = iter;
		K = numTopics;
		W = vocabSize;
		D = numDocs;
		C = corpusSize;

		minibatch = new ArrayList<Minibatch>();

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
	public void Run(Minibatch minibatch) {
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
				int t_ind = 0;
				for(String t : d.termTable.keySet()){
					
					for(int k=0; k<K; k++){
						gamma[t_ind][k] = ((nPhi[t_ind][k] + eta) * (nTheta[d.docId][k] + alpha) / (nz[k] + eta * minibatch.M));

					nTheta[d.docId][k] = ((Math.pow((1 - rhoTheta), d.termTable.get(t)) * nTheta[d.docId][k])
							+ ((1 - Math.pow((1 - rhoTheta), d.termTable.get(t))) * d.Cj * gamma[t_ind][k]));
					}
					t_ind++;
				}
			}
			rhoTheta = s / Math.pow((10 + rhoTheta_t), kappa);
			rhoTheta_t++;
			int t_ind = 0;
			for(String t : d.termTable.keySet()){
				
				for(int k=0; k<K; k++){
					gamma[t_ind][k] = ((nPhi[t_ind][k] + eta) * (nTheta[d.docId][k] + alpha) / (nz[k] + eta * minibatch.M));

				nTheta[d.docId][k] = ((Math.pow((1 - rhoTheta), d.termTable.get(t)) * nTheta[d.docId][k])
						+ ((1 - Math.pow((1 - rhoTheta), d.termTable.get(t))) * d.Cj * gamma[t_ind][k]));
				}
				t_ind++;
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
	}
}
