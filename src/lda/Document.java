package lda;


import java.util.HashMap;

public class Document{
	int docId, Cj;
	private HashMap<String,Integer> termTable;
	public Document(int id,int Cj, HashMap<String,Integer> termMap){
		this.docId = id;
		this.Cj = Cj;

		this.termTable = termMap;
	}
	public Document(){
		this.docId = 0;
		this.Cj = 0;
		
		this.termTable = new HashMap<String,Integer>();
	}
	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	public int getCj() {
		return Cj;
	}
	public void setCj(int cj) {
		Cj = cj;
	}
	public HashMap<String, Integer> getTermTable() {
		return termTable;
	}
	public void setTermTable(HashMap<String, Integer> termTable) {
		this.termTable = termTable;
	}
}
