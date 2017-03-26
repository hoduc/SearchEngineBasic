import urllib
import urllib2
import json 

from os.path import expanduser
SOLR_HOME = "http://localhost:8983/solr/"
SOLR_VER = "solr-6.2.1"
SOLR_NAME = "solr"
SOLR_UPDATE_QUERY_SUFFIX ="update? -H Content-Type: application/json --data-binary "
DATA_FOLDER = "CrawledData"
ID_PREFIX = expanduser("~") + "/Desktop/SearchEngine/HW3/" + SOLR_VER + "/" + DATA_FOLDER + "/"


#print("home=", ID_PREFIX)


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


def add_field( core, id, field, field_val ):
    json_update = quot_s(quot_sb(quot_b(get_field_value("id", id) + "," + set_field_value( field, field_val ))))
    q = SOLR_HOME + core + "/" + SOLR_UPDATE_QUERY_SUFFIX + json_update
    print(q)
    doc = eval(urllib2.urlopen(q).read())
    print (doc)
    return True if doc['responseHeader']['status'] == 0 else False

def write( filename, json ):
    with open(filename,'w') as f:
        f.write(json)

def add_fieldj( id, field, field_val ,json ):
    prop = quot_b(get_field_value("id", id) + "," + set_field_value( field, field_val ))
    if json == "":
        return prop
    return json + "," + prop
    
#TEST_ID= ID_PREFIX + "ff0770ca-d355-4089-8e66-51f1087bde99.html"
#print(add_fieldj("test",TEST_ID, "link", "ducho2",add_fieldj("test", TEST_ID, "link", "ducho", "")))
#write_json("patch_link.json", quot_sb(add_fieldj("test", TEST_ID, "link", "ducho", "")))

def read_id_link(filename, json):
    with open(filename, 'r') as f:
        for line in f:
            print(line)
            ls = line.strip().split(',')
            #print(ls)
            id, link = ls[0],ls[1]
            #print(id,link)
            json = add_fieldj( ID_PREFIX + id, "link", link, json )
    return json
write( "patch_link.json", quot_sb( read_id_link("mapLATimesDataFile.csv", read_id_link("mapHuffingtonPostDataFile.csv", ""))))

