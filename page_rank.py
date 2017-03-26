from bs4 import BeautifulSoup
import os
import urlparse
from os.path import expanduser
import networkx as nx

CWD = os.getcwd()
CONFIG_SRC = CWD + "/config/"
DATA_SRC = CWD + "/CrawledData/"
SOLR_HOME = "http://localhost:8983/solr/"
SOLR_VER = "solr-6.2.1"
PATCH_SRC = "patch/"
PATCH_JSON = PATCH_SRC + "patch_link.json"
PAGERANK_FILE = PATCH_SRC + "pagerank.txt"


############## STEP 1 ################
###########function##################
def quot(s, ob="\"", cb="\""):
    return ob + s + cb

def quot_s( s ):
    return quot( s, "'", "'" )

def quot_b(s):
    return quot(s,"{", "}")

def quot_sb(s):
    return quot(s, "[", "]")

def get_field_value( field, field_val ):
    return quot( field ) + ":" + quot( field_val )

def set_field_value( field, field_val ):
    return quot( field ) + ":" + quot_b( quot("set") + ":" + quot(field_val) )

def write_json( filename, json ):
    with open(filename,'w') as f:
        f.write(json)

def add_fieldj( id, field, field_val ,json ):
    prop = quot_b(get_field_value("id", id) + "," + set_field_value( field, field_val ))
    if json == "":
        return prop
    return json + "," + prop

#####################################

#note: folder should end with "/"
def html_url(folder, id_prefix):
    G = nx.DiGraph()
    hu_mapping = {}
    uh_mapping = {}
    for csv in os.listdir( folder ):
        if not csv.endswith(".csv"):
            continue
        with open(folder + csv,'r') as f:
            for line in f:
                ls = line.strip()
                #print(ls)
                c = ls.index(",")
                id, link = ls[:c],ls[c+1:]
                if id not in hu_mapping:
                    hu_mapping[id_prefix + id] = link
                    G.add_node(id_prefix + id)
                if link not in uh_mapping:
                    uh_mapping[link] = id_prefix + id
        #print("hu_mapping:", len(hu_mapping.keys()))
        #print("uh_mapping:", len(uh_mapping.keys()))
   
    return hu_mapping, uh_mapping, G

def get_link( html, hu_mapping ):
    return "" if html not in hu_mapping else hu_mapping[html]

def get_html( link, uh_mapping ):
    return "" if link not in uh_mapping else uh_mapping[link]

#sometimes, people like to cut the full url
def normalize_link( page, link ):
    to_break = False
    ret_link = link
    if ret_link:
        ret_link = link.encode("utf-8")
        if not page:
            print("domain wrong!!!")
            exit(-1)
        if not ret_link.startswith("http://") and not ret_link.startswith("https://"):
            ret_link = page + ret_link
   # print(link,"=>",ret_link)
    return ret_link

def domain(url):
    return urlparse.urlparse(url).hostname or ''

def ulinkpr(folder, hu_mapping, uh_mapping, G, attributes, anchors, json):
    for html in os.listdir(folder):
        if not os.path.isfile(html) and not html.endswith(".html"):
            continue
        current_html = folder + html
        current_link = get_link( current_html, hu_mapping ) #url
        #print("current_html:", current_html)
        #print("current_link:", current_link)
        #html_fp = open( current_html )
        #update json
        json = add_fieldj( current_html, "link", current_link, json )
        #parse outgoing link
        docSoup = BeautifulSoup(open(current_html), "html.parser")
        for (att,anchor) in zip(attributes,anchors):
            for link in docSoup.findAll(anchor):
               #print("link:?", link.get(att))
               o_link = normalize_link( domain(current_link), link.get( att ))
               if o_link and o_link in uh_mapping:
                   #print("in here?")
                   o_html = get_html( o_link, uh_mapping )
                   print("edge:", current_html, "=>", o_html )
                   G.add_edge( current_html, o_html )
    return nx.pagerank(G,0.85, None, 30),json

def write_pr(filename, pr_dict):
    with open(filename,'w') as f:
        for k,v in pr_dict.items():
            f.write( str(k) + "=" + str(v) +"\n")

def patch( json_loc, pr_loc ):
    humap, uhmap, G = html_url(CONFIG_SRC, DATA_SRC)
    #print("number_of_nodes:", G.number_of_nodes())
    #write json to update link
    #page rank
    pr_dict, json = ulinkpr(DATA_SRC, humap, uhmap, G, ['href','src'], ['a', 'img'], "")
    write_json( json_loc, quot_sb( json ) )
    write_pr( pr_loc, pr_dict ) 

patch(PATCH_JSON, PAGERANK_FILE)
