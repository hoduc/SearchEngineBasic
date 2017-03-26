from flask import Flask
from flask import request
from flask import render_template
import urllib
import urllib2

app = Flask(__name__)

SOLR_HOME="http://localhost:8983/solr/"

def add_k_v(d1,k1,d2,k2):
    if k1 in d1 and k1 not in d2:
        d2[k2] = d1[k1]

def on_type( val ):
    print(type(val), val) 
    if isinstance( val, list ) and len( val ) == 1:
        return val[0]
    return val

    
def add_ks_vs(d1,k1s,d2,k2s):
    return { k2s[i] : on_type( d1[k1s[i]] )for i in range(len(k1s)) if k1s[i] in d1 }

def pagify(total_items, item_per_page=10, entry_before_dot = 3 ):
    pages = []
    page_factor = 1 if total_items%item_per_page > 0 else 0
    for i in range( 2, total_items/item_per_page + page_factor + 1):
        if i > entry_before_dot:
            pages.append( "..." )
            pages.append( total_items/item_per_page + page_factor )
            break
        pages.append( str( i ) )
    return pages

def parse_entries( docs ):
    numFound = int(docs['response']['numFound'])
    print (numFound, " documents found. ")
    entries = []
    if numFound:
        for bulk_info in docs['response']['docs']:
            d = add_ks_vs(bulk_info,["link", "title", "description", "og_image", "id"], {}, ["link", "title", "description", "img", "id"])            
            entries.append(d)
    return pagify(numFound), entries

#param=(k,v)
def append_param(param, prefix="&"):
    return prefix + urllib.quote( param[0] ) + "=" + urllib.quote( param[1] ) if param else ""

def append_params(params):
    p = ""
    #print("param=", params)
    for param in params:
        p  += append_param( param )
    print("param=", p)
    return p

#Ref
#https://cwiki.apache.org/confluence/display/solr/Using+Python
def get_indexed_docs( core, query, params = [] ):
    r_url = SOLR_HOME + core + "/select" + append_param(("indent","on"),"?") + append_param(("wt", "python")) + append_param(("q", query )) + append_params(params)
    print("url:", r_url)
    return eval(urllib2.urlopen(r_url).read())

@app.route('/')
def hello_world():
    return 'hello world'

@app.route('/hello/<string:name>')
def hello(name=None):
    #hello
    return render_template( 'hello.html', name=name )

def search_frame(title, src, request, core, params=[]):
    error = None
    entries = []
    npages = None
    q = ""
    if request.method == 'POST':
        q = str(request.form["q"])
        print("searchword:", q)
        if not q:
            error = "Please type something"
        #send request to solr and parse the response into a list
        else:
            npages, entries = parse_entries( get_indexed_docs( core, q, params ) )
        if not entries:
            error = "Sorry, cannot find anything!!!"
    return render_template( 'search.html', title=title, src=src, error=error, entries=entries, pages=npages, q=q)    
    
@app.route('/search', methods=['POST', 'GET'])
def search():
    return search_frame( "normal", "search", request, "srcore" )

@app.route('/search_pr', methods=['POST', 'GET'])
def search_pr():
    return search_frame( "pagerank", "search_pr", request, "srcore", [("sort", "pageRankFile desc")])
    
@app.route('/not_implemented')
def not_implemented():
    return "Not implemented"
