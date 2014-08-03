package Refactored;

import java.util.HashMap;
import java.util.Map;

public class Document {
	int docId, Cj;
	HashMap<String,Integer> termTable;
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
}
