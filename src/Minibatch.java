package LDA;

import java.util.ArrayList;
import java.util.List;

public class Minibatch {
	int M;
	List<Document> Docs;

	public Minibatch(int m, ArrayList<Document> docList){
		M = m;
		Docs = docList;
	}
}