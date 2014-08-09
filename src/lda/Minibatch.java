package lda;

import java.util.ArrayList;
import java.util.List;

public class Minibatch {
	int M;
	List<Document> docs;

	public Minibatch(int m, ArrayList<Document> docList){
		M = m;
		docs = docList;
	}
	public Minibatch(){
		M = 0;
		docs = new ArrayList<Document>();
	}
}