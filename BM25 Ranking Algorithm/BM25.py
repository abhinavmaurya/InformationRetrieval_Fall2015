__author__ = 'abhinavmaurya'
#####################################################
# Author: Abhinav Maurya                            #
# Course: Information Retrieval CS6200              #
# Assignment#: 3                                    #
# Description: BM25 Algorithm                       #
# Date: 11/07/2015                                  #
#####################################################

import sys
import math
from collections import defaultdict
import json
import operator

# Constants
k1 = 1.2
b = 0.75
k2 = 100.0
R = 0.0
r = 0.0
bm25_op_file = "results.eval"

# the main bm25 function for computing score of each doc
def bm25(index_file, queries_file, n_docs):
    index = open(index_file, 'r')
    inv_index = json.load(index)
    docs_length_dict = createDocsLength(inv_index)
    avg_doc_length = calculateDocsAvgLength(docs_length_dict)

    queries = open(queries_file, 'r')
    query = queries.readline()
    q_id = 1
    op = open(bm25_op_file, "w+")
    op.write("query_id" + "\tQ0\t" + "doc_id" + "\t" + "rank" + "\t" + "BM25_score" + "\tsystem-name\n")
    # iterate over all queries and write 100 doc with score in o/p file.
    while query != "":
        q = query.split()
        bm25_score = calculateBM25Score(q, inv_index, docs_length_dict, avg_doc_length)
        write_score(q_id, bm25_score, op, n_docs)
        q_id += 1
        query = queries.readline()

# BM25 Score calculation for all documents
def calculateBM25Score(q_wrds, inv_index, docs_length_dict, avg_doc_length):
    N = len(docs_length_dict)
    bm25_score = defaultdict()
    q_wrd_freq = defaultdict()
    for w in q_wrds:
        if w not in q_wrd_freq:
            q_wrd_freq[w] = 1
        else:
            q_wrd_freq[w] += 1

    #iterate over each doc to compute its BM25 score
    for doc, ln in docs_length_dict.items():
        score = 0.0
        # iterate over query words to compute score of doc
        # score of doc = sum of score of doc for individual query word
        for q_wrd, qf in q_wrd_freq.items():
            word_index = inv_index[q_wrd]
            f = 0
            # compute f -> freq of word in doc
            if doc in word_index:
                f = word_index[doc]

            # n -> total docs containing query word
            n = len(word_index)

            # computation of normalization factor K
            K = k1 * ((1 - b) + (b * (float(ln) / avg_doc_length)))

            # BM_25 scoring function is broken into 3 parts for ease of understanding
            # BM_25 score for given word = t1*t2*t3
            t1 = math.log((((r + 0.5)/(R - r + 0.5)) / ((n - r + 0.5) / (N - n - R + r + 0.5))))
            t2 = (((k1 + 1) * f) / (K + f))
            t3 = (((k2 + 1) * qf) / (k2 + qf))

            # the sum of scores for all query words will give us BM25 score of doc
            score += (t1 * t2 * t3)

        bm25_score[doc] = score

    return bm25_score


# writing the BM25 score of top 100 docs for each query
def write_score(q_id, bm25_score, op, n_docs):
    sorted_bm_25_score_list = sorted(bm25_score.items(), key=operator.itemgetter(1), reverse=True)
    i = 1
    for doc, score in sorted_bm_25_score_list:
        op.write(str(q_id) + "\t\tQ0\t" + doc + "\t" + str(i) + "\t" + "%.10f" % score + "\tabhinav-system\n")
        if(i == n_docs):
            break
        i += 1

# createDocsLength creates the dictioary with doc_id(key) and respective doc_length(value)
def createDocsLength(inv_index):
    docs_length_dict = defaultdict()
    for wrd, doc_freq in inv_index.items():
        for doc, freq in doc_freq.items():
            if(doc not in docs_length_dict.keys()):
                docs_length_dict[doc] = doc_freq[doc]
            else:
                docs_length_dict[doc] += doc_freq[doc]

    return docs_length_dict

# calculateDocsAvgLength calculates the average document length
def calculateDocsAvgLength(docs_length):
    sum = 0
    for doc, ln in docs_length.items():
        sum += ln
    avg = sum/len(docs_length)
    return avg

# reading inputs as command line arg and calling bm25 function
if __name__ == '__main__':
    index_file = sys.argv[1]
    queries_file = sys.argv[2]
    n_docs = int(sys.argv[3])
    bm25(index_file, queries_file, n_docs)
