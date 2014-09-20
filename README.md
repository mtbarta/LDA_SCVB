LDA_SCVB
========

*Latent Dirichlet Allocation using Stochastic Collapsed Variational Bayes (Foulds et al 2013).*  
[paper here](http://arxiv.org/pdf/1305.2452.pdf)

## Usage  

This model allows you to specify *batch* or *online* processing. Future updates will create an update function.

```Java
    //LDA(iterations, numDocs, numTopics)
    LDA lda = new LDA(30, 1500, 20);
    lda.processing("batch");

    lda.trainNIPS("./text/docword.nips.txt","./Vocab/vocab.nips.txt");
    
    ArrayList<HashMap<String,Double>> termTopics = lda.termTopicProbs(10);
    
    Print.printTopicWords(termTopics);
```

## Results

    Reading Files...
    LDA updates:
    Iteration: 1...... Done.
    Iteration: 2...... Done.
    Iteration: 3...... Done.
    .
    .
    .
    Topic: 0
    university : 0.040116595764092516
    mean : 0.03885503115118312
    error : 0.03702257269693845
