#####################################################
# Author: Abhinav Maurya                            #
# Course: Information Retrieval CS6200              #
# Assignment#: 3                                    #
# Description: Creation of Inverted index for BM25  #
# Date: 11/07/2015                                  #
#####################################################


import sys
import re
from collections import defaultdict
import json

#Constants
inv_index_op_file = "index.out"

# Inverted index creation
def invertedIndex(corpus_file):

    doc_count = 0
    doc_id = 0
    inv_index = defaultdict(list)
    # regex to match if line is document number
    doc_regex = '(#)\s(\d*)'

    #read corpus file line by line
    with open(corpus_file) as f:
        lines = f.read().splitlines()
    for line in lines:
        matchObj = re.match(doc_regex, line)
        if matchObj:
            doc_id = matchObj.group(2)
            doc_count += 1
        else:
            for word in line.split():
                #ignore the digit
                if(not word.isdigit()):
                    if(not word in inv_index):
                        inv_index[word] = {}
                        inv_index[word][doc_id] = 1
                    else:
                        if(doc_id in inv_index[word]):
                            inv_index[word][doc_id] += 1
                        else:
                            inv_index[word][doc_id] = 1

    write_to_file(inv_index_op_file, inv_index)

# Write the inverted index to a file
# Making inv_index dictionary serializable using json module of python
# and writing it to a file using json dump
def write_to_file(file_name, inv_index):
    file = open(file_name, "w")
    json.dump(inv_index, file)
    file.close()


# reading command line arg and calling index function
if __name__ == '__main__':
    corpus_file = sys.argv[1]
    invertedIndex(corpus_file)