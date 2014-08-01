package LDA;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class LDA{
	int numDocs, numTerms, numTermsInCorpus, numTopics, iter;
	List<Term> termList;

	public LDA(int iter, int topics, int docs,
				int terms, int termsCorpus){
		numDocs = docs;
		numTerms = terms;
		numTermsInCorpus = termsCorpus;
		numTopics = topics;
		iter = iter;
		termList = new ArrayList<Term>();
	}
	/* normalize probabilities to 1. used on each inference
	* iteration.
	* 
	*@returns: void
	*/
	public void normalize(SCVB sampling){
		for(int k=0; k<sampling.K; k++){
			double k_tot = 0;
			for(Term term : termList){
				k_tot = sampling.nPhi[term.wordID][k];
			}
			for(Term term : termList){
				double temp = sampling.nPhi[term.wordID][k] / k_tot;
				sampling.nPhi[term.wordID][k] = temp;
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
	public void parseFile(String file, int skiplines, int minibatchsize){
		//BufferedReader reader = new BufferedReader(new FileReader(file));

		File file = File(file);
		SCVB scvb0 = new SCVB(iter, numTopics, numTerms,
					numDocs,0);
		//find number of batches to run...
		/* for each batch, find out how many minibatches to run,
		* create minibatches, store to SCVB, and parallelize run.
		*/
		int nProcs = Runtime.getRuntime().availableProcessors();

		int macroBatchSize = numDocs / nProcs;
		FileProcess processor = new FileProcess(file, numDocs);
		processor.processAll(nProcs, macroBathSize);
	}
}

