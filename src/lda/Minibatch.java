package lda;

import java.util.ArrayList;
import java.util.List;

public class Minibatch {
	private static int MB_COUNT = 0;
	private int M = 0;
	public List<Document> docs;

	public Minibatch(int m, ArrayList<Document> docList){
		setM(m);
		docs = docList;
	}
	public Minibatch(){
		MB_COUNT++;
		setM(MB_COUNT);
		docs = new ArrayList<Document>();
	}
	public int getM() {
		return M;
	}
	public void setM(int m) {
		M = m;
	}
}