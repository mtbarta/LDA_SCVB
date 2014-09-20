package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import scvb.Minibatch;
import util.Document;
import util.Vocabulary;

/*
 * TODO: implement an iterator based on minibatch size.
 * TODO: implement a tokenizer class that can be adjusted.
 */
public class Reader {
	private int wordsInCorpus = 0;
	private HashMap<Integer,Integer> docSizes = new HashMap<Integer,Integer>();
	
	public ArrayList<Document> readAll(String fileDir, int numDocs, int numThreads) 
			throws InterruptedException, ExecutionException, IOException{
		//read each file. When each file is vectorized, put it in a minibatch.
		//producer-consumer threading structure.
		/*
		URL file2 = getClass().getResource(fileDir);
		if (file2 == null){
			throw new FileNotFoundException("Directory location not found");
		}
		String filePath = file2.getPath();
        filePath = filePath.replaceFirst("/","");
        */
		File directory = new File(fileDir);
		
		//error checking!
		if (numDocs != directory.listFiles().length){
			throw new IOException("Documents specified is not the same as amount in Directory.");
		}
				
        ExecutorService service = Executors.newFixedThreadPool(numThreads);
        CompletionService<Document> completionService = 
        	       new ExecutorCompletionService<Document>(service);
        System.out.println("Submitting files to threads...");
        // Wait for ReadFile to complete
        service.submit(new ReadFile(completionService, directory)).get();
        System.out.println("    Thread Pool shut down...");
        service.shutdown();  // interrupt CPUTasks
        
		//do things with processed docs.
        ArrayList<Document> documents = new ArrayList<Document>(numDocs);
        while(!service.isTerminated()){
        	try{
	        	Document doc = completionService.take()
	        			.get();
	        	documents.add(doc);
        	} catch(ExecutionException e) {
        		e.getCause();
        		e.printStackTrace();
        	}
        }
        service.awaitTermination(365, TimeUnit.DAYS);
        for (Document doc : documents){
        	this.docSizes.put(doc.getDocId(), doc.getCj());
        }
        return documents;
	}
	
	public ArrayList<Document> readAll(String fileDir,int numDocs, int numThreads, Vocabulary vocab) 
			throws InterruptedException, ExecutionException, IOException{
		//read each file. When each file is vectorized, put it in a minibatch.
		//producer-consumer threading structure.
		/*
		URL file2 = getClass().getResource(fileDir);
		if (file2 == null){
			throw new FileNotFoundException("Directory location not found");
		}
		String filePath = file2.getPath();
        filePath = filePath.replaceFirst("/","");
        */
		File directory = new File(fileDir);
		
		//error checking
		if (numDocs != directory.listFiles().length){
			throw new IOException("Documents specified is not the same as amount in Directory.");
		}
				
        ExecutorService service = Executors.newFixedThreadPool(numThreads);
        CompletionService<Document> completionService = 
        	       new ExecutorCompletionService<Document>(service);
        System.out.println("Submitting files to threads...");
        // Wait for ReadFile to complete
        service.submit(new ReadFile(completionService, directory, vocab)).get();
        System.out.println("    Thread Pool shut down...");
        service.shutdown();  // interrupt CPUTasks
        
		//do things with processed docs.
        ArrayList<Document> documents = new ArrayList<Document>(numDocs);
        while(!service.isTerminated()){
        	try{
	        	Document doc = completionService.take()
	        			.get();
	        	documents.add(doc);
        	} catch(ExecutionException e) {
        		e.getCause();
        		e.printStackTrace();
        	}
        }
        service.awaitTermination(365, TimeUnit.DAYS);
        for (Document doc : documents){
        	this.docSizes.put(doc.getDocId(), doc.getCj());
        }
        return documents;
	}
	public ArrayList<Document> readNIPS(String fileLoc, String vocabloc){
		ArrayList<Document> results = new ArrayList<Document>();
		/*
		 * reads the NIPS data set, which is a single file with tuples of
		 * document number, word, and word count.
		 * 
		 * @param fileLoc is the directory of the files.
		 * @param vocab is the Vocabulary object for the documents.
		 */
		ArrayList<String> vcb = new ArrayList<String>();
		
		File nipsData = new File(fileLoc);
		BufferedReader s;
		try {
			File vocab = new File(vocabloc);
			Scanner reader = new Scanner(vocab);
			while (reader.hasNext()){
				String line = reader.next();
				vcb.add(line);
			}
			reader.close();
			
			s = new BufferedReader(new FileReader(nipsData));
            String line;
            s.readLine();s.readLine();s.readLine();
            
            HashMap<String,Integer> map = new HashMap<String,Integer>();
            HashSet<String> wordSet = new HashSet<String>();
            int Cj = 0;
            int oldDocNum = 1;
            while(s.ready()){
            	line = s.readLine();
            	String[] lineArray = line.split(" ");
            	int docNum = Integer.parseInt(lineArray[0]) -1;
            	int wordID = Integer.parseInt(lineArray[1]) -1;
            	String word = vcb.get(wordID);
            	int wordCount = Integer.parseInt(lineArray[2]);
            	if (docNum == oldDocNum){
            		map.put(word, wordCount);
            		Cj += wordCount;
            	} else {
            		Document tempDoc = new Document(docNum,Cj,map);
            		wordSet.add(word);
            		map = new HashMap<String,Integer>();
            		Cj = 0;
            		
            		results.add(tempDoc);
            	}
            	this.wordsInCorpus = wordSet.size();
            	oldDocNum = docNum;
            }
          s.close();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
		for (Document doc : results){
        	this.docSizes.put(doc.getDocId(), doc.getCj());
        }
		return results;
	}
	/*
	 * GETTERS
	 */
	public int getWordsInCorpus() {
		return wordsInCorpus;
	}

	public HashMap<Integer, Integer> getDocSizes() {
		return docSizes;
	}

}
