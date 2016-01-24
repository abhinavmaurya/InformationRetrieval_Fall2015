#########################################################
# Author: Abhinav Maurya                                #
# Course: Information Retrieval CS6200                  #
# Assignment#: 5                                        #
# Description: Evaluation of retrieval effectiveness    #
# Date: 12/03/2015                                      #
#########################################################

import sys
from collections import defaultdict
import math
import operator

def retrieval_effectiveness(rank_file, queries_file, relevance_judgement):

    #read corpus file line by line
    with open(queries_file) as qf:
        queries = qf.read().splitlines()

    rel_doc_list = retrieveRelevantDocList(relevance_judgement)

    result_for_query = open(rank_file).readlines()

    avg_precision = defaultdict()
    op_file_2 = open('effectiveness_measure_op', 'w')
    op_file_2.write("Effectiveness Measure:\n\n")
    for query in queries:
        qid = query.split(' ', 1)[0]
        q = query.split(' ', 1)[1]
        rel_docs = rel_doc_list[qid]
        get_converted_qid = retrieveQidForResult(qid)

        op_file_name = 'result_for_q' + qid
        op_file = open(op_file_name, 'w')
        op_file.write('Result Table for Query#'+ qid + ": " + q +"\n\n")

        header = ('RANK', 'DOC_ID', 'DOC_SCORE', 'REL_LEVEL', 'PRECISION', 'RECALL', 'NDCG')
        formatted_header = '{:4}\t{:10}\t{:15}\t{:^10}\t{:>20}\t{:>10}\t{:>20}'.format(*header)
        op_file.write(formatted_header + '\n')

        i = (get_converted_qid - 1) * 100
        idcg = calculateIDCGForQuery(rel_doc_list[qid])
        total_ap = 0
        counter = 1
        rel_ret = 0
        dcg = 0
        for num in range (i, i+100):
            doc = 'CACM-' + str(int(result_for_query[num].split()[2]))
            score = result_for_query[num].split()[4]
            if doc in rel_doc_list[qid]:
                R = rel_doc_list[qid][doc]
                rel_ret += 1
                total_ap += rel_ret/counter
            else:
                R = 0

            precision = rel_ret/counter
            recall = rel_ret/len(rel_doc_list[qid])

            # DCG calculation
            if(counter == 1):
                dcg = R
            else:
                dcg += R/math.log(counter, 2)

            ndcg = dcg/idcg[counter-1]

            # p@20
            if(counter == 20):
                print('P@20 for query \"' + q + "\" = " + str(precision))
                op_file_2.write('P@20 for query \"' + q + "\" = " + str(precision) + "\n")

            to_write = (counter, doc, score, R, precision, recall, ndcg)
            op_file.write('{:4}\t{:10}\t{:15}\t{:^10}\t{:20}\t{:10}\t{:20}'.format(*to_write))
            op_file.write("\n")

            counter += 1

        avg_precision[qid] = total_ap/len(rel_doc_list[qid])

    sum = 0
    for key, val in avg_precision.items():
        sum += val

    print('MAP: ' + str(sum/len(avg_precision)))
    op_file_2.write('\nMAP = '+ str(sum/len(avg_precision)))


# To calculate ideal DCG for a query
def calculateIDCGForQuery(rel_doc_with_R):
    idcg = []
    counter = 1
    idcg_sum = 0
    sorted_rel_docs_list = sorted(rel_doc_with_R.items(), key=operator.itemgetter(1), reverse=True)
    for doc, R in sorted_rel_docs_list:
        if(counter == 1):
            idcg_sum = R
        else:
            idcg_sum += R/math.log(counter, 2)
        idcg.append(idcg_sum)
        counter += 1

    while counter <= 100:
        idcg.append(idcg_sum)
        counter += 1

    return idcg


# To retrieve Relevant Doc list for queries from given relevance file
def retrieveRelevantDocList(relevance_judgement):

    rel_doc_list = defaultdict()
    rel_docs = open(relevance_judgement, 'r')
    line = rel_docs.readline()
    while line != '':
        qid = line.split()[0]
        if not qid in rel_doc_list:
            rel_doc_list[qid] = {}

        doc_id = line.split()[2]
        doc_rel = int(line.split()[3])
        rel_doc_list[qid][doc_id] = doc_rel
        line = rel_docs.readline()

    return rel_doc_list

# To retrieve corresponding query ids from eval file
def retrieveQidForResult(qid):

    if(qid == '12'):
        return 1
    elif(qid == '13'):
        return 2
    elif(qid == '19'):
        return 3
    else:
        return 0


# reading inputs as command line arg and calling bm25 function
if __name__ == '__main__':
    rank_file = sys.argv[1]
    queries_file = sys.argv[2]
    relevance_judgement = sys.argv[3]
    retrieval_effectiveness(rank_file, queries_file, relevance_judgement)