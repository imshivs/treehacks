import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Code sample for accessing the Yelp API V2.
 * 
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 * 
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 * 
 */
public class YelpAPI {

  private static final String API_HOST = "api.yelp.com";
  private static final String DEFAULT_TERM = "dinner";
  //private static final String DEFAULT_LOCATION = "San Francisco, CA";
  private static final String DEFAULT_LOCATION = "Atlanta, GA";
  private static final double DEFAULT_LATITUDE = 33.8145922;
  private static final double DEFAULT_LONGITUDE = -84.415253;
  private static final double DEFAULT_LONGITUDE_SW = -84.415253;
  private static final double DEFAULT_LATITUDE_SW = -84.415253;
  private static final double DEFAULT_LONGITUDE_NE = -84.411352 ;
  private static final double DEFAULT_LATITUDE_NE = 33.787218;
  private static final int SEARCH_LIMIT = 20;
  private static final String SEARCH_PATH = "/v2/search";
  private static final String BUSINESS_PATH = "/v2/business";

  /*
   * Update OAuth credentials below from the Yelp Developers API site:
   * http://www.yelp.com/developers/getting_started/api_access
   */
  private static final String CONSUMER_KEY = "zlf4pJN763y3udkSaBzk_w";
  private static final String CONSUMER_SECRET = "KK0DbuhoQSIAQOD8Y2fXpBhMuh0";
  private static final String TOKEN = "Q-70pEkQcI2P38WBf6YaqcgpNWTLnueG";
  private static final String TOKEN_SECRET = "LwiBZvGni9uhfjIIEOWGHlA8sgM";

  OAuthService service;
  Token accessToken;

  /**
   * Setup the Yelp API OAuth credentials.
   * 
   * @param consumerKey Consumer key
   * @param consumerSecret Consumer secret
   * @param token Token
   * @param tokenSecret Token secret
   */
  public YelpAPI(String consumerKey, String consumerSecret, String token, String tokenSecret) {
    this.service =
        new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
            .apiSecret(consumerSecret).build();
    this.accessToken = new Token(token, tokenSecret);
  }

  /**
   * Creates and sends a request to the Search API by term, and latitude and longitude.
   * <p>
   * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
   * for more info.
   * 
   * @param term <tt>String</tt> of the search term to be queried
   * @param location <tt>String</tt> of the location
   * @return <tt>String</tt> JSON Response
   */
  public String searchForBusinessesByLocation(String term, String location) {
    OAuthRequest request = createOAuthRequest(SEARCH_PATH);
    request.addQuerystringParameter("term", term);
    //request.addQuerystringParameter("location", location);
    request.addQuerystringParameter("bounds", "33.787218,-84.415139|33.781605,-84.411352");
    System.out.println("request");
    //request.addQuerystringParameter("cll", String.valueOf(DEFAULT_LATITUDE) + ", " + String.valueOf(DEFAULT_LONGITUDE));
    request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
    return sendRequestAndGetResponse(request);
  }

  /**
   * Creates and sends a request to the Search API by term, and a bounding box of latitude and longitude.
   * <p>
   * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
   * for more info.
   * 
   * @param term <tt>String</tt> of the search term to be queried
   * @param longitude <tt>double</tt> of the location
   * @param latitude <tt>double</tt> of the location
   * @return <tt>String</tt> JSON Response
   */
  public String searchForBusinessesByDirection(String term, double longitude, double latitude) {
    OAuthRequest request = createOAuthRequest(SEARCH_PATH);
    //Create bounding box
    double longitude_NE = longitude + .1;
    double longitude_SW = longitude - .1;
    double latitude_NE = latitude + .1;
    double latitude_SW  = latitude - .1;
    String coords = String.valueOf(latitude_SW) + ","
      + String.valueOf(longitude_SW) + "|" + String.valueOf(latitude_NE)
      + "," + String.valueOf(longitude_NE);
    request.addQuerystringParameter("term", term);
    request.addQuerystringParameter("bounds", coords);
    System.out.println("request");
    return sendRequestAndGetResponse(request);
  }

  public double distanceByRatio(double baseLongitude, double baseLatitude,
    double baseDegree, double longitude, double latitude) {
    double longitudeDiff = longitude - baseLongitude;
    double latitudeDiff = latitude - baseLatitude;
    double hypoteneus = Math.pow(longitudeDiff, 2) + Math.pow(latitudeDiff, 2);
    double degree = Math.asin(latitudeDiff / hypoteneus);
    double realDegree;
    if (longitudeDiff > 0 && latitudeDiff > 0) {
      realDegree = 90 - degree;
    } else if (longitudeDiff < 0 && latitudeDiff > 0) {
      realDegree = 270 + degree;
    } else if (longitudeDiff > 0 && latitudeDiff < 0) {
      realDegree = 90 + degree;
    } else {
      realDegree = 270 - degree;
    }
    double degreeDiff = Math.abs(realDegree - baseDegree);
    return hypoteneus * .75 + degreeDiff * .25;
  }

  /**
   * Creates and sends a request to the Business API by business ID.
   * <p>
   * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
   * for more info.
   * 
   * @param businessID <tt>String</tt> business ID of the requested business
   * @return <tt>String</tt> JSON Response
   */
  public String searchByBusinessId(String businessID) {
    OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
    return sendRequestAndGetResponse(request);
  }

  /**
   * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
   * 
   * @param path API endpoint to be queried
   * @return <tt>OAuthRequest</tt>
   */
  private OAuthRequest createOAuthRequest(String path) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
    return request;
  }

  /**
   * Sends an {@link OAuthRequest} and returns the {@link Response} body.
   * 
   * @param request {@link OAuthRequest} corresponding to the API request
   * @return <tt>String</tt> body of API response
   */
  private String sendRequestAndGetResponse(OAuthRequest request) {
    System.out.println("Querying " + request.getCompleteUrl() + " ...");
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }

  /**
   * Queries the Search API based on the command line arguments and takes the first result to query
   * the Business API.
   * 
   * @param yelpApi <tt>YelpAPI</tt> service instance
   * @param yelpApiCli <tt>YelpAPICLI</tt> command line arguments
   */
  private static String queryAPI(YelpAPI yelpApi, YelpAPICLI yelpApiCli, double longitude, double latitude, double degree) {

    String searchResponseJSON =
        yelpApi.searchForBusinessesByDirection(yelpApiCli.term, longitude, latitude);

    JSONParser parser = new JSONParser();
    JSONObject response = null;
    try {
      response = (JSONObject) parser.parse(searchResponseJSON);
    } catch (ParseException pe) {
      System.out.println("Error: could not parse JSON response:");
      System.out.println(searchResponseJSON);
      System.exit(1);
    }

    JSONArray businesses = (JSONArray) response.get("businesses");
    // JSONObject firstBusiness = (JSONObject) businesses.get(0);
    // String firstBusinessID = firstBusiness.get("id").toString();
    // System.out.println(String.format(
    //     "%s businesses found, querying business info for the top result \"%s\" ...",
    //     businesses.size(), firstBusinessID));

    double smallestRatio = 100000;

    // Select the nearest business and display business details
    String smallestBusinessID = "";
    for (Object business: businesses) { 
      JSONObject currentBusiness = (JSONObject) business;
      String currBusinessID = currentBusiness.get("id").toString();
      JSONObject currBusinessLocation = (JSONObject)currentBusiness.get("location");
      JSONObject currBusinessCoord = (JSONObject)currBusinessLocation.get("coordinate");
      String currBusinessLatitude = currBusinessCoord.get("latitude").toString();
      double currBusinessLat = Double.parseDouble(currBusinessLatitude);
      String currBusinessLongitude = currBusinessCoord.get("longitude").toString();
      double currBusinessLong = Double.parseDouble(currBusinessLongitude);
      System.out.println("\n" + currBusinessLat + ", " +  currBusinessLong);

      double temp = yelpApi.distanceByRatio(longitude, latitude, degree, currBusinessLong, currBusinessLong);
      if (smallestRatio > temp) {
        smallestBusinessID = currentBusiness.get("id").toString();
        smallestRatio = temp;
      }
      System.out.println(String.format("Result for business \"%s\" found:", currBusinessID));
      //String businessResponseJSON = yelpApi.searchByBusinessId(currBusinessID.toString());
      //System.out.println(businessResponseJSON);
    }

    String businessResponseJSON = yelpApi.searchByBusinessId(smallestBusinessID.toString());
    System.out.println("\n\n\n" + businessResponseJSON);

    return businessResponseJSON;
  }

  /**
   * Command-line interface for the sample Yelp API runner.
   */
  private static class YelpAPICLI {
    @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
    public String term = DEFAULT_TERM;

    @Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
    public String location = DEFAULT_LOCATION;
  }

  /**
   * Main entry for sample Yelp API requests.
   * <p>
   * After entering your OAuth credentials, execute <tt><b>run.sh</b></tt> to run this example.
   */
  public static void main(String[] args) {
    YelpAPICLI yelpApiCli = new YelpAPICLI();
    new JCommander(yelpApiCli, args);

    YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
    queryAPI(yelpApi, yelpApiCli, -122.16562540, 37.42855860, 90);
  }
}