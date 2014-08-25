package util;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Document{
	private int docId, Cj;
	private HashMap<String,Integer> termTable;
	
	public Document(int id,int Cj, HashMap<String,Integer> termMap){
		this.docId = id;
		this.Cj = Cj;

		this.termTable = termMap;
	}
	public Document(){
		
	}

	
	public HashMap<String,Integer> getTermTable() {
		return termTable;
	}

	public int getDocId() {
		return docId;
	}


	public int getCj() {
		return Cj;
	}
}
