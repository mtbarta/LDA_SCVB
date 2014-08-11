import json
import numpy as np
import pandas as pd
import itertools
import logging
from scipy import sparse
import cPickle as Pickle
import gensim

from nltk import SnowballStemmer

from sklearn.pipeline import Pipeline
from sklearn.feature_selection  import chi2
import sklearn.linear_model as lm
from sklearn import naive_bayes as nb
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn import cross_validation as cv
from sklearn.decomposition import TruncatedSVD
from sklearn.feature_selection import SelectPercentile, chi2

#from preprocessing import *


logging.basicConfig(format = '%(asctime)s : %(levelname)s : %(message)s', level = logging.INFO)
#####
# Get Data
#####

X = open("./text/Alice", "r")
#X = pd.read_csv('./text/Alice', sep=" ", na_values=['?'], )
#X_test = pd.read_csv('../data/test.tsv', sep="\t", na_values=['?'], index_col=1)
#y = X['label']
#data.cpickle from data_wrangler.py... combination of all text features
#f = open('data.cpickle', 'rb')
#print "Loading Pickle"
#data = Pickle.load(f)
#f.close()

#####
# Segment Data into train/test
#####

#train=data[len(X_test.index):]
#test = data[:len(X_test.index)]

#####
# Tokenize
#####


token = [line.lower().split() for line in X]
#train_token = [line.lower().split() for line in train]
#test_token = [line.lower().split() for line in test]

#stemmer = SnowballStemmer('english')
stemmed = []

for line in token:
    stemmed.append([word for word in line])

#####
# LDA Dictionary
####
dictionary = gensim.corpora.Dictionary(token)
# remove stop words and words that appear only once
#stoplist = set('a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your'.split(','))
#stop_ids = [dictionary.token2id[stopword] for stopword in stoplist
#            if stopword in dictionary.token2id]
#once_ids = [tokenid for tokenid, docfreq in dictionary.dfs.iteritems() if docfreq < 3]
#dictionary.filter_tokens(stop_ids + once_ids) # remove stop words and words that appear only once
#dictionary.compactify()
#####
#filter dictionary
#####
#dictionary.filter_extremes(3,.5)



preproc_train = stemmed
#preproc_test = stemmed[:len(X_test.index)]


#####
# LDA Corpus
####
class MyCorpus(object):
     def __iter__(self):
         for line in preproc_train:
             # assume there's one document per line, tokens separated by whitespace
             yield dictionary.doc2bow(line)

corpus_memory_friendly = MyCorpus()

# Use below to persist dictionary and corpus.
##########
#dictionary.save('mydictionary.dict')
#gensim.corpora.MmCorpus.serialize('corpus.mm', corpus_memory_friendly)

#####
# LDA model
#####
lda = gensim.models.ldamodel.LdaModel(corpus= corpus_memory_friendly, 
                                      id2word = dictionary,
                                      num_topics=2,
                                       update_every=0,
                                        chunksize=10000,
                                         passes=10)



lda.print_topics(2)
lda.save("lda_model.pkl")