package scvb;

import java.util.ArrayList;
import java.util.List;

import util.Document;

public class Minibatch {
	private static int MB_COUNT = 0;
	private int index = 0;
	private int Cj;
	public List<Document> docs;

	public Minibatch(ArrayList<Document> docList){
		this.Cj = 0;
		for(Document doc : docList){
			this.Cj += doc.getCj();
		}
		MB_COUNT++;
		setIndex(MB_COUNT);
		this.docs = docList;
	}
	public Minibatch(){
		Cj = 0;
		MB_COUNT++;
		setIndex(MB_COUNT);
		docs = new ArrayList<Document>();
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int m) {
		this.index = m;
	}
	public int getCj() {
		return Cj;
	}
}