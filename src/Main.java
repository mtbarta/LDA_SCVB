import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import util.Print;
import lda.LDA;


public class Main {

	public static void main(String[] args) {
		/*
		 * TODO:
		 * implement document/batch iteration to not read in unused docs too early.
		 * find a way to implement tokenization methods easily.
		 * implement settings functions in LDA.
		 * 
		 */
		LDA lda = new LDA(30, 1500, 20);
		lda.processing("batch");
		//almost ready for NIPS data set.
		lda.trainNIPS("./text/docword.nips.txt","./Vocab/vocab.nips.txt");

		ArrayList<HashMap<String,Double>> termTopics = lda.termTopicProbs(10);
		
		Print.printTopicWords(termTopics);
	}

}
