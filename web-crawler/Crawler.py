#################################################
# Author: Abhinav Maurya                        #
# Course: Information Retrieval                 #
# Assignment#: 1                                #
# Description: Implementation of web-crawler    #
# Date: 09/23/2015                              #
#################################################

from bs4 import BeautifulSoup
from urllib.error import HTTPError, URLError
import urllib.request
import time
import sys

BASE_URL = "http://en.wikipedia.org/"

#Function for Focused crawling
def focused_crawling(seedPage, keyphrase):

    finalList = [seedPage]
    focList = []
    discardedList = []
    totalpages = 0
    depth = 1

    # Flag if 1000 Unique URLs crawled
    stopCrawling = False
    while depth <= 5 and not stopCrawling:
        nextList = finalList
        finalList = []

        for item in nextList:

            if(totalpages == 1000):
                stopCrawling = True
                break

            if item not in focList and item not in discardedList:
                totalpages += 1
                try:
                    soup = BeautifulSoup(urllib.request.urlopen(item).read(), 'html.parser')
                except HTTPError as e:
                    print("Connection Error. Will reconnect in 10 seconds")
                    time.sleep(10)
                    continue
                except URLError as e:
                    print("Unreachable URL: ", item)
                    continue
                except:
                    continue

                text = soup.get_text()
                if(text.lower().find(keyphrase.lower(), 0, len(text)) > -1):
                    focList.append(item)
                    if(len(focList) == 1000):
                        stopCrawling = True
                        break

                    for link in soup.find_all('a'):
                        l = str(link.get('href'))
                        if ":" not in l and "#" not in l and "Main_Page" not in l:
                            newUrl = str(urllib.request.urljoin(BASE_URL, l))
                            if newUrl.startswith("http://en.wikipedia.org/wiki/") and newUrl != BASE_URL and newUrl not in finalList and newUrl not in focList and newUrl not in discardedList:
                                finalList.append(newUrl)

                else:
                    discardedList.append(item)
            #time.sleep(1)

        depth += 1
    print("Total Pages Crawled = ",totalpages)
    print("Total Relevant Pages = ", len(focList))
    print("Proportion = ",(len(focList)/totalpages))
    return focList

#Function For Unfocused Crawling
def unfocused_crawling(seedPage):
    finalList = [seedPage]
    crawledList = []
    depth = 1
    # Flag if 1000 Unique URLs crawled
    stopCrawling = False
    while depth <= 5 and not stopCrawling:
        nextList = finalList
        finalList = []

        for item in nextList:

            if item not in crawledList:
                try:
                    soup = BeautifulSoup(urllib.request.urlopen(item).read(), 'html.parser')
                except HTTPError as e:
                    print("Connection Error. Will reconnect in 10 seconds")
                    time.sleep(10)
                    continue
                except URLError as e:
                    print("Unreachable URL: ", item)
                    time.sleep(30)
                    continue
                except:
                    continue

                crawledList.append(item)
                if(len(crawledList) == 1000):
                    stopCrawling = True
                    break

                for link in soup.find_all('a'):
                    l = str(link.get('href'))
                    if ":" not in l and "#" not in l and "Main_Page" not in l:
                        newUrl = str(urllib.request.urljoin(BASE_URL, l))
                        if newUrl.startswith("http://en.wikipedia.org/wiki/") and newUrl != BASE_URL and newUrl not in finalList and newUrl not in crawledList:
                            finalList.append(newUrl)

            #time.sleep(1)
        depth += 1
    return crawledList

def write_to_file(file, list_to_write):
    for i in list_to_write:
        file.write(i + "\n")

def main(seedPage, keyphrase):
    if(keyphrase == None or keyphrase == ""):
        list = unfocused_crawling(seedPage)
        unfocussed_crawling_list = open("unfocused_crawling_URL_list", "w+")
        write_to_file(unfocussed_crawling_list, list)
    else:
        list = focused_crawling(seedPage, keyphrase)
        unfocussed_crawling_list = open("focused_crawling_URL_list", "w+")
        write_to_file(unfocussed_crawling_list, list)

if __name__ == '__main__':
    if(len(sys.argv) <= 3):
        if(len(sys.argv) == 2):
            seed = sys.argv[1]
            key = None
        else:
            seed = sys.argv[1]
            key = sys.argv[2]
    main(seed,key)