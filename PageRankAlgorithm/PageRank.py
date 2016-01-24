#####################################################
# Author: Abhinav Maurya                            #
# Course: Information Retrieval CS6200              #
# Assignment#: 2                                    #
# Description: Implementation of PageRank algorithm #
# Date: 10/11/2015                                  #
#####################################################

# P = pages
# N = totalPages
# S = sink_pages
# M(p) is the set (without duplicates) of pages that link to page p
# L(q) is the number of out-links (without duplicates) from page q
# d is the PageRank damping/teleportation factor; use d = 0.85 as a fairly typical value

import sys
from collections import defaultdict
import math
import operator


## PageRank function to initiate page rank program
def pageRank(in_links_file):

    initial_page_rank = {}
    page_rank = {}
    in_links_dict = makeInLinksDict(in_links_file)
    out_links_dict = makeOutLinksDict(in_links_dict)
    pages = in_links_dict.keys() | out_links_dict.keys()
    totalPages = len(pages)
    sink_nodes = findSinkNodes(in_links_dict, out_links_dict)
    d = 0.85
    perplexity = []

    ## logic start
    for p in pages:
        initial_page_rank[p] = 1/totalPages
        page_rank[p] = 1/totalPages

    #page_rank = initial_page_rank
    iteration = 0
    while (not isPRConverging(perplexity)):

        sinkPR = 0
        new_PR = {}
        for p in sink_nodes:
            sinkPR += page_rank[p]

        for p in pages:
            new_PR[p] = (1-d)/totalPages
            new_PR[p] += d*sinkPR/totalPages

            for q in in_links_dict[p]:
                new_PR[p] += d* page_rank[q]/len(out_links_dict[q])

        for p in pages:
            page_rank[p] = new_PR[p]

        perplexity.append(calculatePerplexity(page_rank))
        print("Perplexity for iteration", iteration+1," is:", perplexity[iteration])

        iteration += 1
    ##logic end

    ## Write outputs required in files
    write_page_rank_to_file("unsorted_page_rank_values", page_rank.items(), "Page rank of given pages:")

    sortedPRList = sorted(page_rank.items(), key=operator.itemgetter(1), reverse=True)
    write_page_rank_to_file("sorted_page_rank_values", sortedPRList, "Sorted Page rank of given pages (in descending order):")

    write_perplexity_to_file("perplexity_values", perplexity)

    write_results_to_file("pr_results", in_links_dict, sortedPRList, pages, sink_nodes, initial_page_rank, page_rank)



## function to check if page rank values are converging based on perplexity
def isPRConverging(perplexity):
    if len(perplexity) >= 4:
        i = len(perplexity) - 1
        counter = 0
        while counter < 4:
            if (abs(perplexity[i] - perplexity[i-1]) < 1):
                counter += 1
                i -= 1
            else:
                break

        if(counter == 4):
            return True
        else:
            return False
    else:
        False


## Function to calculate the perplexity value for each iteration
def calculatePerplexity(page_rank):
    entropy=0
    for key, val in page_rank.items():
        entropy += val * math.log(val, 2)
    perplexity = math.pow(2, (entropy * -1))
    return perplexity


## Converts the given in_link file to in_link_dictionary
def makeInLinksDict (in_links_file):
    in_link = open(in_links_file, "r")
    in_links_dict = defaultdict(set)
    text = in_link.readline()
    while text != "":
        node_links = text.split()
        for l in node_links[1:]:
            in_links_dict[node_links[0]].add(l)

        text = in_link.readline()
    return in_links_dict


## make in_links_count_dict based on given in_link dict
def makeInLinksCountDict (in_links_dict):
    in_links_count_dict = {}
    for key, value in in_links_dict.items():
        in_links_count_dict[key] = len(value)

    return in_links_count_dict


## make out-link-dictionary based on given in-link dictionary
def makeOutLinksDict(in_links_dict):
    out_links_dict = defaultdict(set)
    for i in in_links_dict.keys():
        for j in in_links_dict[i]:
            out_links_dict[j].add(i)

    return out_links_dict

## make a list of sink nodes
def findSinkNodes (in_links_dict, out_links_dict):
    sink_nodes = set()
    for a in in_links_dict.keys():
        if a not in out_links_dict.keys():
            sink_nodes.add(a)

    return sink_nodes


## Function to write the perplexity values of each iteration in a given file_name
def write_perplexity_to_file(file_name, perplexity):
    file = open(file_name, "w")
    file.write("Perplexity values after each iteration: \n")
    i=1
    for p in perplexity:
        file.write("Perplexity value after iteration "+ str(i) + "\t: "+ str(p) + "\n")
        i+=1
    file.close()


## Function to write the page rank values in sequence as given in page_rank_list
def write_page_rank_to_file(file_name, page_rank_list, msg):
    file = open(file_name, "w")
    file.write(msg + "\n")
    for p, pr in page_rank_list:
        file.write(p + "\t: " + "%.16f" % pr + "\n")
    file.close()



## Write the page rank results to a given file_name
def write_results_to_file(file_name, in_links_dict, sortedPRList, pages, sink_pages, initial_page_rank, page_rank):
    file = open(file_name, "w")

    ## print top 50 pages based on PR value
    file.write("Document IDs of the top 50 pages based on PageRank: \n")
    i = 0
    for p, pr in sortedPRList:
        file.write(p + "\t: " + "%.16f" % pr + "\n")
        i+=1
        if(i == 50):
            break

    ## print top 50  pages based on in_link_count
    file.write("\n\nDocument IDs of the top 50 pages based on in_link count: \n")
    sorted_in_links_count_list = sorted(makeInLinksCountDict(in_links_dict).items(), key=operator.itemgetter(1), reverse=True)
    i = 0
    for p, count in sorted_in_links_count_list:
        file.write(p + "\t: " + str(count) + "\n")
        i+=1
        if(i == 50):
            break

    #the proportion of pages with no in-links (sources);
    pages_with_no_in_link = []
    for p, count in sorted_in_links_count_list:
        if count == 0:
            pages_with_no_in_link.append(p)
    proportion = len(pages_with_no_in_link)/len(pages)
    file.write("\n\nProportion of pages with no inlink = "+str(len(pages_with_no_in_link))+"/"+str(len(pages))+" = "+str(proportion))
    print("\n\nProportion of pages with no inlink = "+str(len(pages_with_no_in_link))+"/"+str(len(pages))+" = "+str(proportion))

    #the proportion of pages with no out-links (sinks);
    proportion = len(sink_pages)/len(pages)
    file.write("\n\nProportion of pages with no outlink (sink) = "+str(len(sink_pages))+"/"+str(len(pages))+" = "+str(proportion))
    print("\n\nProportion of pages with no outlink (sink) = "+str(len(sink_pages))+"/"+str(len(pages))+" = "+str(proportion))

    #the proportion of pages whose PageRank is less than their initial, uniform values.
    lesser_page_rank_list = []
    for p in pages:
        if page_rank[p] < initial_page_rank[p]:
            lesser_page_rank_list.append(p)
    proportion = len(lesser_page_rank_list)/len(pages)
    file.write("\n\nProportion of pages whose PageRank is less than their initial, uniform values = "+str(len(lesser_page_rank_list))+"/"+str(len(pages))+" = "+ str(proportion))
    print("\n\nProportion of pages whose PageRank is less than their initial, uniform values = "+str(len(lesser_page_rank_list))+"/"+str(len(pages))+" = "+ str(proportion))

    file.close()

# reading command line arg and calling pageRank function
if __name__ == '__main__':
    in_links_file = sys.argv[1]
    pageRank(in_links_file)
