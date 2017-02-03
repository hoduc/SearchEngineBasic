package mypackage;

import java.net.URL;
import java.lang.String;
import java.lang.System;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.Locale;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Apache
import org.apache.http.impl.EnglishReasonPhraseCatalog;


//Craw4j specific
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


import static mypackage.Stat.FetchAgg;
import static mypackage.Stat.VisitAgg;
import static mypackage.Stat.UrlAgg;


/**
 * NewsController < CrawlController, to get access to the list<domains> in each instance program runs
 */
public class NewsController extends CrawlController{
    public static final Logger logger = LoggerFactory.getLogger( NewsController.class );

    //just delegate to super
    public NewsController( CrawlConfig config, PageFetcher pageFetcher, RobotstxtServer robotServer ) throws Exception{
	super( config, pageFetcher, robotServer );
    }
    
     //program commands
    public static final String prog = "NewsCrawler";
    public static final String options = "prog [1 link]";

    //Crawler config
    public static final int MAX_PAGE_FETCH = 10000;
    public static final int MAX_DEPTH_CRAWL = 16;
    public static final int NUM_CRAWLER = 7;

    
    //Report
    public static final String USER_AGENT = "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405";
    public static final String CRAWL_REPORT = "CrawlReport_";
    public static final String FETCH_PREFIX = "fetch_";
    public static final String VISIT_PREFIX = "visit_";
    public static final String URLS_PREFIX = "urls_";

    
    
    private Collection<String> DOMAINS = new HashSet<>();
    private Collection<String> SEED_URLS = new HashSet<>();

    
    public Collection<String> getDomains(){
	return DOMAINS;
    }

    public boolean addDomain( String s ) {
	logger.info( "adding domain:" + s );
	return DOMAINS.add( s );
    }

    public boolean addDomains( Collection<String> cs ) {
	return DOMAINS.addAll( cs );
    }

    public Collection<String> getUrlSeeds(){
	return SEED_URLS;
    }

    public boolean addSeedUrl( String s ) throws Exception{
	/*logger.info( "seedurl passed in:" + s );
	String toadd = s.substring( 0, s.indexOf( "//" ) + 2);
	logger.info( "after //// :", toadd );
	int firstSlash = toadd.indexOf( "/" );
	if ( firstSlash != -1 ) {
	    toadd = toadd.substring( 0, firstSlash );
	    //return SEED_URLS.add( toadd );
	}
	logger.info("adding seed_url:" + toadd );
	//return SEED_URLS.add( toadd );
	SEED_URLS.add( toadd );
	return true;*/
	URL sURL = new URL( s );
	return SEED_URLS.add( sURL.getHost( ) );
    }

    public boolean addSeedUrls( Collection<String> cs ) throws Exception{
	for( String s : cs ) {
	    addSeedUrl( s );
	}
	return true;
    }
    
    //public String CrawlSubject = "dummy";

    /*
    private static void collect( Map<Object, ? extends Integer> in, Map< Object, ? super Integer> out ){
	for( Map.Entry<Object, ? extends Integer> entry : in.entrySet( ) ) {
	        Object k = entry.getKey( );
		if( out.containsKey( k ) ) {
		    //Integer i = (Integer)out.get( k );
		    out.put( entry.getKey( ), entry.get( k ) + entry.getValue() );
		}else{
		    out.put( entry.getKey( ), entry.getValue( ) );
		}
	    }
    }
    */
    
    private static Writer getWriter( String filename ) throws Exception {
	Writer writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( filename ) ) );
	return writer;
    }
    

    private static void writeList( String filename, Collection<? extends Object> contents ) throws Exception {
	Writer writer = getWriter( filename );
	for( Object o : contents ) {
	    writer.write( o + "\n" );
	}
	writer.close();
    }

    private static void writemdTitle( Writer writer, String title ) throws Exception {
	writer.write( title + "\n" );
	for( int i = 0 ; i < title.length(); i++ )
	    writer.write( "=" );
	writer.write( "\n" );
    }

    private static void writeStatusCode( Writer writer, Map<Integer, Integer> sCount ) throws Exception {
	for( Map.Entry<Integer, Integer> entry : sCount.entrySet( ) ) {
	    writer.write( entry.getKey( ) +  " - " + EnglishReasonPhraseCatalog.INSTANCE.getReason( entry.getKey( ) , Locale.ENGLISH ) + " : " + entry.getValue( ) + "\n" );
	}
    }

    private static void writeContentTypes( Writer writer, Map<String, Integer> ctCount ) throws Exception {
	for( Map.Entry<String, Integer> entry : ctCount.entrySet( ) ) {
	    writer.write( entry.getKey( ) + " : " + entry.getValue( ) + "\n" );
	}
	
    }

    private static void writeHeader( Writer writer, String crawlSubject ) throws Exception{
	writer.write( "Name : Duc Ho\n" );
	writer.write( "USC ID : 8944025003\n" );
	writer.write( "News site crawled : " + crawlSubject + "\n\n" );
    }

    private static void writeFetchStat( Writer writer, int fetchAttempted, int fetchSuccess, int fetchAbort, int fetchFail ) throws Exception {
	writemdTitle( writer, "Fetch Statistics" );
	writer.write( "# fetches attempted :" + fetchAttempted + "\n" );
	writer.write( "# fetches succeeded :" + fetchSuccess + "\n" );
	writer.write( "# fetches aborted :"   + fetchAbort + "\n" );
	writer.write( "# fetches failed :"    + fetchFail + "\n" );
	writer.write( "\n\n" );
    }

    private static void writeOutUrlStat( Writer writer, String crawlSubject, int totalLink, int uniqueLink, int uniqueResidentLink, int uniqueNonResidentLink ) throws Exception {
	writemdTitle( writer, "Outgoing URLs : " );
	writer.write( "Total URLs extracted : " + totalLink + "\n" );
	writer.write( "# unique URLs extracted :" + uniqueLink + "\n" );
	writer.write( "# unique URLS within [" + crawlSubject + "] : " + uniqueResidentLink + "\n" );
	writer.write( "# unique URLs outside [" + crawlSubject + "] : " + uniqueNonResidentLink + "\n" );
	writer.write( "\n\n");
    }

    private static void writeStatusCodeStat( Writer writer, Map<Integer, Integer> statusCodes ) throws Exception {
	writemdTitle( writer, "Status Codes : " );
	writeStatusCode( writer, statusCodes );
	writer.write( "\n\n" );
	
    }

    private static void writeFileSizeStat( Writer writer, int _KB, int _10KB, int _100KB, int _MB, int MB ) throws Exception {
	
	writemdTitle( writer, "File Sizes : " );
	writer.write( "< 1KB : " + _KB + "\n" );
	writer.write( "1KB ~ < 10KB : " + _10KB + "\n" );
	writer.write( "10KB ~ < 100KB : " + _100KB  + "\n" );
	writer.write( "100KB ~ < 1MB : " + _MB + "\n" );
	writer.write( ">= 1MB : " + MB + "\n" );
	writer.write( "\n\n" );
	
    }

    private static void writeContentTypeStat( Writer writer, Map<String, Integer> contentTypes ) throws Exception {
	writemdTitle( writer, "Content Types : " );
	writeContentTypes( writer, contentTypes );
	writer.write( "\n\n" );
    }

    private static void startReport( String crawlSubject, NewsController controller ) throws Exception{
	//csv
	Collection<FetchAgg> fetches = new ArrayList<>();
	Collection<VisitAgg> visiteds = new ArrayList<>();
	Collection<UrlAgg> links = new ArrayList<>();
	//4 report
	Set<String> uniqueLinks = new HashSet<>();
	Set<String> uniqueResidentLinks = new HashSet<>();
	Set<String> uniqueNonResidentLinks = new HashSet<>();
	Map<Integer, Integer> statusCodes = new HashMap<>();
	Map<String, Integer> contentTypes = new HashMap<>();
	
	int fetchAttempted = 0;
	int fetchSuccess = 0;
	int fetchAbort = 0;
	int fetchFail = 0;
	int totalLink = 0;
	int uniqueLink = 0;
	int uniqueResidentLink = 0;
	int uniqueNonResidentLink = 0;
	int _KB = 0;
	int _10KB = 0;
	int _100KB = 0;
	int _MB = 0;
	int MB = 0;
	
	for( Object statObj : controller.getCrawlersLocalData( ) ) {
	    Stat stat = (Stat) statObj;
	    fetches.addAll( stat.getFetches( ) );
	    visiteds.addAll( stat.getVisiteds( ) );
	    links.addAll( stat.getLinks( ) );
	    
	    fetchAttempted += stat.getFetches( ).size( );
	    fetchSuccess += stat.getFetchSuccs( ).size( );
	    fetchAbort += stat.getFetchAbrts( ).size( );
	    fetchFail += stat.getFetchFails( ).size( );
	    totalLink += stat.getLinks( ).size( );
	    uniqueLinks.addAll( stat.getUniqueLinks( ) );
	    uniqueResidentLinks.addAll( stat.getUniqueResidentLinks( ) );
	    uniqueNonResidentLinks.addAll( stat.getUniqueNonResidentLinks( ) );
	    
	    Map<Integer, Integer> sc = stat.getStatusCodes( );
	    for( Map.Entry<Integer, Integer> entry : sc.entrySet( ) ) {
		Integer k = entry.getKey( );
		if( statusCodes.containsKey( k ) ) {
		    statusCodes.put( k, statusCodes.get( k ) + entry.getValue() );
		}else{
		    statusCodes.put( k, entry.getValue( ) );
		}
	    }
	    

	    int[] fileSizesCount = stat.getFileSizesCount( );
	    _KB += fileSizesCount[0];
	    _10KB += fileSizesCount[1];
	    _100KB += fileSizesCount[2];
	    _MB += fileSizesCount[3];
	    MB += fileSizesCount[4];

	    
	    Map<String, Integer> ctc = stat.getContentTypes( );
	    for( Map.Entry<String, Integer> entry : ctc.entrySet( ) ) {
		String k = entry.getKey( );
		if( contentTypes.containsKey( k ) ) {
		    contentTypes.put( k, contentTypes.get( k ) + entry.getValue() );
		}else{
		    contentTypes.put( k, entry.getValue( ) );
		}
	    }
	}

	uniqueLink = uniqueLinks.size( );
	uniqueResidentLink = uniqueResidentLinks.size( );
	uniqueNonResidentLink = uniqueNonResidentLinks.size( );
	
	//write the csv and stuff
	writeList( crawlSubject + "/" + FETCH_PREFIX + crawlSubject + ".csv", fetches );
	writeList( crawlSubject + "/" + VISIT_PREFIX + crawlSubject + ".csv", visiteds );
	writeList( crawlSubject + "/" + URLS_PREFIX + crawlSubject + ".csv", links );
	//calculate the statistics
	//start report
	Writer writer = getWriter( crawlSubject + "/" + CRAWL_REPORT + crawlSubject + ".txt" );
	writeHeader( writer, crawlSubject );
	writeFetchStat( writer, fetchAttempted, fetchSuccess, fetchAbort, fetchFail );
	writeOutUrlStat( writer, crawlSubject, totalLink, uniqueLink, uniqueResidentLink, uniqueNonResidentLink );
	writeStatusCodeStat( writer, statusCodes );
	writeFileSizeStat( writer, _KB, _10KB, _100KB, _MB, MB );
	writeContentTypeStat( writer, contentTypes );
	writer.close( );
    }
    
    //Configuration : controller to crawl passed in links
    public static void ConfigAndRun( String...seeds ) throws Exception {
	Set<String> domains = new HashSet<String>();
	String crawlSubject = "";
	for( String seed : seeds ) {
	    WebURL wurl = new WebURL();
	    wurl.setURL( seed );
	    domains.add( wurl.getDomain( ) );
	    crawlSubject += wurl.getDomain( ) + "_";
	}

	if ( !crawlSubject.equals( "" ) )
	    crawlSubject = crawlSubject.replace(".", "_");
	
	//crawl config
	CrawlConfig config = new CrawlConfig();
	config.setMaxPagesToFetch( MAX_PAGE_FETCH );
	config.setMaxDepthOfCrawling( MAX_DEPTH_CRAWL );
	config.setUserAgentString( USER_AGENT );
	config.setIncludeBinaryContentInCrawling( true ); //png, pdf,etc
	config.setPolitenessDelay( 2500 ); //internet slow
	//extra
	config.setCrawlStorageFolder( crawlSubject );
	
	
	PageFetcher pageFetcher = new PageFetcher( config );
	RobotstxtConfig robotstxtConfig = new RobotstxtConfig( );
	//robotstxtConfig.setEnabled( false ); //dismiss robot protocol
	RobotstxtServer robotstxtServer = new RobotstxtServer( robotstxtConfig, pageFetcher );

	
	NewsController controller = new NewsController( config, pageFetcher, robotstxtServer );
	controller.addDomains( domains );
	for( String seed : seeds ) {
	    logger.info( "seed:" + seed );
	    controller.addSeed( seed );
	    controller.addSeedUrl( seed );
	}
	
	for( String s : domains ) {
	    logger.info( "domain:" + s );
	}

	for( String s : controller.getUrlSeeds( ) ) {
	    logger.info( "seedurl:" + s );
	}
	//return;
	// logger.info("test csv output:" , new UrlAgg( "123", false ) );
	// Object o = new UrlAgg( "456", true );
	// logger.info("test2:" + o );
	// o = new UrlAgg( "789", false );
	// logger.info("test3:" + o );
	//if it gets here, it is done!!!
	controller.start( NewsCrawler.class, NUM_CRAWLER );
	startReport(crawlSubject, controller);
	
    }

    public static String DisplayOption(){
	return options;
    }
    
    public static void main( String[] args ) throws Exception {
	if ( args.length  != 1 ) {
	    System.out.println( DisplayOption() );
	}else{
	    System.setProperty( "current.date", String.valueOf( System.currentTimeMillis( ) ) );
	    ConfigAndRun( args );
	}
    }
}
