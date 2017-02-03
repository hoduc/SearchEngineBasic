package mypackage;

import java.net.URL;
import java.net.MalformedURLException;
import java.lang.String;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.impl.EnglishReasonPhraseCatalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;


public class Stat{
    public static final Logger logger = LoggerFactory.getLogger( Stat.class );
    public static class FetchAgg{
	String Url;
	int StatusCode;

	public FetchAgg( String Url,  int StatusCode ) {
	    this.Url = Url;
	    this.StatusCode = StatusCode;
	}

	//for dumping csv
	@Override
	public String toString(){
	    return Url + "," + StatusCode;
	}
     };

     public static class VisitAgg{
	String Url;
	long Size;
	int OutLinks;
	String ContentType;

	public VisitAgg( String Url, long Size, int OutLinks, String ContentType ) {
	    this.Url = Url;
	    this.Size = Size;
	    this.OutLinks = OutLinks;
	    this.ContentType = ContentType;
	}

	@Override
	public String toString() {
	    return Url + "," + Size + "," + OutLinks + "," + ContentType;
	}
     };

     public static class UrlAgg{
	String Url;
	boolean OK;

	public UrlAgg( String Url, boolean OK ){
	    this.Url = Url;
	    this.OK = OK;
	}

	@Override
	public String toString(){
	    if( OK ){
		return Url + "," + "OK";
	    }
	    return Url + "," + "N_OK";
	}
     };
     //dummy class to just dump csv
     // public static class Agg{
     // 	private String[] aggs;
     // 	public Agg( String[] aggs ){
     // 	    this.aggs = aggs;
     // 	}

     // 	@Override
     // 	public String toString(){
     // 	    String s = "";
     // 	    for( String agg : aggs )
     // 		s += agg + ",";
     // 	    s = s.substring( 0, s.length( ) - 1 );
     // 	    return s;
     // 	}
     // };

    public static final int KB = 1024;
    public static final int _10KB = 10 * KB;
    public static final int _100KB = 10 * _10KB;
    public static final int MB = 1024 * KB;
    
    private Collection<FetchAgg> fetches = new ArrayList<>();
    private Collection<FetchAgg> fetchSuccs = new ArrayList<>();
    private Collection<FetchAgg> fetchAbrts = new ArrayList<>();
    private Collection<FetchAgg> fetchFails = new ArrayList<>();
    private Collection<VisitAgg> visiteds = new ArrayList<>();
    private Collection<UrlAgg>   links    = new ArrayList<>();
    private Map<String, Integer>    uniqueLinks         = new HashMap<>();
    private Map<String, Integer>    uniqueResidentLinks = new HashMap<>();
    private Map<String, Integer>    uniqueNonResidentLinks = new HashMap<>();
    private Map<Integer, Integer> statusCodeFreqs = new HashMap<>();
    
    
    private final int[] fileSizesCount = new int[]{
	0, //< 1KB - 1000B       [0]
	0, //< 10KB - 10,000B    [1]
	0, //< 100KB - 100,000B  [2]
	0, //< 1MB - 1,000,000B  [3]
	0, //>= 1MB - 1,000,000B [4]
    };
    
    /*Map<Integer, Integer> fileSizesCountMap = new HashMap<>(){
    	    {
    		put( KB, 0 );
    		put( _10KB, 0 );
    		put( _100KB, 0 );
    		put( MB, 0 );
    		put( -1, 0 ); //>=1MB
    	    }
    };
    */

    private final Map<String, Integer> contentTypeFreqs = new HashMap<>();

    public void UpdateSizeCount( long fileSize ) {
	int index = 4;
	//Hah, mathy
	////get coresponding index : log( n / 1000 ) + 1
	// if ( fileSize < 1000 ) {
	//     index = 0;
	// }else if ( fileSize < MB ) {
	//     index = (int)(Math.log10( fileSize/ 10000 ) + 1 );
	// }
	if ( fileSize < KB ) {
	    index = 0;
	}else if ( fileSize < _10KB ) {
	    index = 1;
	}else if ( fileSize < _100KB ) {
	    index = 2;
	}else if ( fileSize < MB ) {
	    index = 3;
	}else{
	    index = 4;
	}
	fileSizesCount[ index ]++;
    }

    /**
     * update valid statusCode count
     */
    public void UpdateStatusCodeCount( int statusCode ) {
	if( EnglishReasonPhraseCatalog.INSTANCE.getReason( statusCode, Locale.ENGLISH ) == null ) {
	    return;
	}
	Integer statusCodeCount = statusCodeFreqs.get( statusCode );
	if ( statusCodeCount == null ) {
	    statusCodeFreqs.put( statusCode, 1 );
	}else {
	    statusCodeFreqs.put( statusCode, statusCodeCount + 1 );
	}
    }
    
    private void UpdateContentTypeCount ( String contentType ) {
	Integer contentTypeCount = contentTypeFreqs.get( contentType );
	if ( contentTypeCount == null ) {
	    contentTypeFreqs.put( contentType, 1 );
	}else{
	    contentTypeFreqs.put( contentType, contentTypeCount + 1 );
	}
    }

    private void UpdateUniqueLink( UrlAgg urlAgg ) {
	if ( !uniqueLinks.containsKey( urlAgg.Url ) ) {
	    uniqueLinks.put( urlAgg.Url, 1 );
	    if ( urlAgg.OK && !uniqueResidentLinks.containsKey( urlAgg.Url ) ) {
		uniqueResidentLinks.put( urlAgg.Url,  1 );
	    }else if ( !urlAgg.OK && !uniqueNonResidentLinks.containsKey( urlAgg.Url ) ) {
		uniqueNonResidentLinks.put( urlAgg.Url,  1 );
	    }
	}
    }

    private void UpdateUniqueNonResidentLink( UrlAgg urlAgg ) {
	if ( !urlAgg.OK && !uniqueNonResidentLinks.containsKey( urlAgg.Url ) ) {
	    uniqueNonResidentLinks.put( urlAgg.Url,  1 );
	}
    }

    private void UpdateUniqueResidentLink( UrlAgg urlAgg ) {
	if ( urlAgg.OK && !uniqueResidentLinks.containsKey( urlAgg.Url ) ) {
	    uniqueResidentLinks.put( urlAgg.Url,  1 );
	}
    }
    
    public void AddFetch( FetchAgg fetchAgg ) {
	fetches.add( fetchAgg );
	//UpdateStatusCodeCount( fetchAgg.StatusCode );
    }

    public void AddFetchSuccess( FetchAgg fetchAgg ) {
	fetchSuccs.add( fetchAgg );
	UpdateStatusCodeCount( fetchAgg.StatusCode );
    }

    public void AddFetchAbort( FetchAgg fetchAgg ) {
	fetchAbrts.add( fetchAgg );
	UpdateStatusCodeCount( fetchAgg.StatusCode );
    }

    public void AddFetchFail( FetchAgg fetchAgg ) {
	fetchFails.add( fetchAgg );
	UpdateStatusCodeCount( fetchAgg.StatusCode );
    }

    public void AddVisited( VisitAgg visitAgg ) {
	visiteds.add( visitAgg );
	UpdateSizeCount( visitAgg.Size );
	UpdateContentTypeCount( visitAgg.ContentType );
    }

    public static boolean isResidentInDomain( String testDomain, Collection<String> domains ) {
	for( String d : domains ) {
	    if ( testDomain.startsWith( d ) ) {
		return true;
	    }
	}
	return false;
	//return domains.contains( testDomain );
    }
    
    public void AddLink( WebURL url, Collection<String> domains ) {
	boolean inDomain = false;
	try{
	    URL pUrl = new URL( url.getURL( ) );
	    inDomain = isResidentInDomain( pUrl.getHost( ), domains );
	}catch(MalformedURLException e){
	    inDomain = false;
	}
	logger.info("url:" + url.getURL( ) );
	logger.info("in domain:" + inDomain);
	UrlAgg urlAgg = new UrlAgg( url.getURL( ), inDomain );
	links.add( urlAgg );
	UpdateUniqueLink( urlAgg );
	
    }

    public Collection<FetchAgg> getFetches( ) {
	return fetches;
    }

    public Collection<FetchAgg> getFetchSuccs( ) {
	return fetchSuccs;
    }

    public Collection<FetchAgg> getFetchAbrts( ) {
	return fetchAbrts;
    }

    public Collection<FetchAgg> getFetchFails( ) {
	return fetchFails;
    }

    public Collection<VisitAgg> getVisiteds( ) {
	return visiteds;
    }

    public Collection<UrlAgg> getLinks( ) {
	return links;
    }

    public Set<String> getUniqueLinks( ){
	return uniqueLinks.keySet( );
    }

    public Set<String> getUniqueResidentLinks( ){
	return uniqueResidentLinks.keySet( );
    }

    public Set<String> getUniqueNonResidentLinks( ){
	return uniqueNonResidentLinks.keySet( );
    }

    public Map<Integer, Integer> getStatusCodes( ) {
	return statusCodeFreqs;
    }

    public Map<String, Integer> getContentTypes( ) {
	return contentTypeFreqs;
    }

    public int[] getFileSizesCount( ) {
	return fileSizesCount;
    }
    
}
