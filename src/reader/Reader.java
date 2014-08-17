package reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import util.Document;

public class Reader{
	private int wordsInCorpus = 0;
	private HashMap<Integer,Integer> docSizes;
	public ArrayList<Document> readFilesFromDirectory(String fileDir) 
			throws FileNotFoundException{
		File directory = new File(fileDir);
		ArrayList<Document> results = new ArrayList<Document>(directory.listFiles().length);
		
		for(File f : directory.listFiles()){
			Document doc = new Document.Builder()
											.readFile(f)
											.tokenize()
											.build();
			this.wordsInCorpus += doc.getCj();
			this.docSizes.put(doc.getDocId(), doc.getCj());
			results.add(doc);
		}
		return results;
	}
	
	public static Document readFile(String fileLoc) 
			throws FileNotFoundException{
		File rFile = new File(fileLoc);		

		Document doc = new Document.Builder()
										.readFile(rFile)
										.tokenize()
										.build();


		return doc;
	}

	public int getWordsInCorpus() {
		return wordsInCorpus;
	}

	public HashMap<Integer, Integer> getDocSizes() {
		return docSizes;
	}

}
