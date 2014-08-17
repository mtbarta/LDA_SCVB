import lda.LDA;


public class Main {

	public static void main(String[] args) {
		/*
		 * initialize LDA.
		 * set variables in LDA.
		 * set tokenization parameters.
		 * run LDA.
		 */
		
		/*
		 * implement document/batch iteration to not read in unused docs too early.
		 * find a way to implement tokenization methods easily.
		 * implement settings functions in LDA.
		 * 
		 */
		LDA lda = new LDA(5, 5, 5);
		lda.processing("batch");
		
		lda.train("/text");

	}

}
