package mypackage;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * News Crawler
 * Will be started off with different threads
 *
 */

public class NewsCrawler extends WebCrawler{
    public static final Logger logger = LoggerFactory.getLogger( NewsCrawler.class );
    
    public static final Pattern FILTERS = Pattern.compile( ".*(\\.(html|pdf|doc|docx|txt|tif|tiff|gif|jpeg|jpg|jif|jfif|jp2|jpx|j2k|j2c|fpx|pcd|png))$");

    //private NewsController newsController;
    private Stat stat = new Stat();


    /*
     * When crawler instance finished, it got added to controller local data
     * Collect data there
     */
    public Stat getMyLocalData(){
	return stat;
    }
    
    //FILTER LINK
    //visit within the site
    @Override
    public boolean shouldVisit( Page referringPage, WebURL url ) {
	//boolean visit = FILTERS.matcher( url.getURL( ).toLowerCase( ) ).matches();
	boolean sv = Stat.isResidentInDomain( url.getDomain( ), ( (NewsController)myController ).getDomains( ) );
	// if ( sv ) {
	//     //attempt to fetch
	//     stat.AddFetch( new FetchAgg( url.getURL( ), -1 ) );
	// }
	return sv;
    }

    /*
     *  url fetches - status code
     *  fetch attempts
     *  Finish fetch, no exception
     */
    @Override
    public void handlePageStatusCode( WebURL webUrl, int statusCode, String statusDescription) {
	stat.AddFetch( new FetchAgg( webUrl.getURL( ), statusCode ) );
	/*if( statusCode == 200 ){
	    stat.AddFetchSuccess( new FetchAgg( webUrl.getURL( ), statusCode ) );
	}/*else{
	    //fail and abort is handled somewhere else
	    }*/
    }

    // @Override
    // public void onNotVisitRobot(WebURL webUrl, int statusCode, String statusDescription) {
    // 	stat.AddFetchAbort( new FetchAgg( webUrl.getURL( ), statusCode ) );
    // 	logger.info("imma stop!!!");
    // }

    

    //also aborted, happening durin fetchPage
    @Override
    public void onPageBiggerThanMaxSize(String urlStr, int statusCode, long pageSize) {
	super.onPageBiggerThanMaxSize( urlStr, statusCode, pageSize );
	stat.AddFetch( new FetchAgg( urlStr, statusCode ) );
	stat.AddFetchAbort( new FetchAgg( urlStr, statusCode ) );
	//stat.UpdateStatusCodeCount( statusCode );
	//reflect file size
	stat.UpdateSizeCount( pageSize );
	
    }

    //parse error. not even visited
    @Override
    public void onParseError(WebURL webUrl) {
	super.onParseError( webUrl );
	stat.AddFetchAbort( new FetchAgg( webUrl.getURL( ), -1 ) );
    }

    /*
     * Status code other than 3xx, fail
     */
    @Override
    public void onUnexpectedStatusCode( String urlStr, int statusCode, String contentType, String description ) {
	stat.AddFetchFail( new FetchAgg( urlStr, statusCode ) );
	//stat.UpdateStatusCodeCount( statusCode );
    }
    
    /*
     * fetch failed
     */
    @Override
    public void onContentFetchError( WebURL webUrl ) {
	super.onContentFetchError( webUrl );
	stat.AddFetchFail( new FetchAgg( webUrl.getURL( ), -1 ) );
	
    }

    //other exception..considered failed!!!
    //Which is the following
    //fetchpage : interruptedException, IOException ==> add fetch
    //
    @Override
    public void onUnhandledException(WebURL webUrl, Throwable e) {
	super.onUnhandledException(webUrl, e);
	stat.AddFetchFail( new FetchAgg( webUrl.getURL( ), -1 ) );
    }
    

    private static boolean shouldVisitContentType( String contentType ) {
	return contentType.contains( "html" )
	    || contentType.contains( "image" )
	    || contentType.contains( "pdf" )
	    || contentType.contains( "msword" )
	    || contentType.contains( "vnd.openxmlformats-officedocument" )
	    || contentType.contains( "vnd.ms-word" )
	;
    }

    private static String normalizeContentType( String contentType ) {
	String[] splitByColon = contentType.split( ";" );
	if ( splitByColon.length > 0 ){
	    return splitByColon[0];
	}
	return contentType;
    }
    
    /* Inspect page
     *  url - size - #outlinks - contenttype
     * only visit html/doc/pdf, all image type
     */
    @Override
    public void visit( Page page ) {
	String contentType = normalizeContentType( page.getContentType( ) );
	
	if ( !shouldVisitContentType( /*page.getContentType( )*/ contentType ) ) {
	    stat.AddFetchAbort( new FetchAgg( page.getWebURL( ).getURL( ), page.getStatusCode( ) ) );
	    return;
	}
	
	//it is html,doc,pdf,image
	stat.AddFetchSuccess( new FetchAgg( page.getWebURL( ).getURL( ), page.getStatusCode( ) ) );
	stat.AddVisited( new VisitAgg( page.getWebURL( ).getURL( ), page.getContentData( ).length,page.getParseData( ).getOutgoingUrls( ).size( ), /*page.getContentType( )*/ contentType ) );
	//links
	stat.AddLink( page.getWebURL(), ( (NewsController)myController ).getUrlSeeds( ) );
       
    }
    
}
