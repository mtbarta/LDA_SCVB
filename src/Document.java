package LDA;

import java.util.HashMap;
import java.util.Map;

public class Document {
	int docId, Cj;
	HashMap<Integer,Integer> termTable;
	public Document(int id, HashMap<Integer,Integer> termMap){
		docId = id;
		Cj = 0;

		termTable = termMap;
	}
}
