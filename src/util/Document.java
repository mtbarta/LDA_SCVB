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

	
	public HashMap<String,Integer> getTermTable() {
		return termTable;
	}

	/*
	 * BUILDER CLASS BELOW
	 */
	public static class Builder {
		private static int docId;
		private int Cj = 0;
		private HashMap<String, Integer> termTable = new HashMap<String,Integer>();
		
		//TEMP VARS for building.
		private LinkedList<String> tempFile;

		public Builder readFile(File filePath) throws FileNotFoundException{
			//File file = new File(filePath);
			this.tempFile = new LinkedList<String>();
			
			Scanner s = new Scanner(filePath);
			String line = s.next();
	        while (s.hasNext()) {
	        	tempFile.add(line);
	        }
	        s.close();
			return this;
		}
		
		public Builder tokenize() {
			while(this.tempFile.iterator().hasNext()){
				String line = this.tempFile.iterator().next();
		    	for(String word : line.split(" ")){
		    		if (this.termTable.containsKey(word)) {
			            this.termTable.put(word, termTable.get(word) + 1);
			            this.Cj++;
			        } else {
			            this.termTable.put(word, 1);
			            this.Cj++;
			        }
		    	}
			}
			return this;
		}
		public Builder assignId(){
			Builder.docId = 0;
			Builder.docId++;
			
			return this;
		}
		public Document build(){
			return new Document(docId, Cj, termTable);
			
		}
	}

	public int getDocId() {
		return docId;
	}


	public int getCj() {
		return Cj;
	}
}
