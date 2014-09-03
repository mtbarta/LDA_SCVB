package reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.TimeUnit;

import util.Document;
import util.Vocabulary;

public class ReadFile implements Runnable {

    //private final BlockingQueue<LinkedList<String>> queue;
	private CompletionService<Document> cs;
    private final File fileDir;
    private int count = 0;
    private Scanner s;
    private Vocabulary vocab;

    public ReadFile(CompletionService<Document> cs, File fileDir) {
        this.fileDir = fileDir;
        this.cs = cs;
    }
    public ReadFile(CompletionService<Document> cs, File fileDir, Vocabulary vocab) {
        this.fileDir = fileDir;
        this.cs = cs;
        this.vocab = vocab;
    }

    @Override
    public void run() {
        try {
            for (File file : this.fileDir.listFiles()) {
            	//if(file == null) break;
            	LinkedList<String> tempFile = new LinkedList<String>();
            	tempFile.add(Integer.toString(this.count));
            	this.count++;
            	//TODO: add processing features
                s = new Scanner(file);
                String line;
                while (s.hasNext()) {
                	line = s.next();
                	tempFile.add(line);
                }
                if (this.vocab != null){
                	cs.submit(new DocumentConsumer(tempFile,this.vocab));
                } else {
                	cs.submit(new DocumentConsumer(tempFile));
                }
            } 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
			s.close();
		}
    }
}