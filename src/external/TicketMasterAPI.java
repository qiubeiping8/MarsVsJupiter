package external;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

import java.io.InputStreamReader;
import java.util.*;


public class TicketMasterAPI {
    private static final String URL = "http://app.ticketmaster.com/discovery/v2/events.json";
    private static final String DEFAULT_KEYWORD = "";  //no restriction
    private static final String API_KEY ="W2GiGiBbsTU0J6JdhPY0EYFOJUPRwiYN";
    
    private static final String EMBEDDED = "_embedded";
    private static final String EVENTS = "events";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String URL_STR = "url";
	private static final String RATING = "rating";
	private static final String DISTANCE = "distance";
	private static final String VENUES = "venues";
	private static final String ADDRESS = "address";
	private static final String LINE1 = "line1";
	private static final String LINE2 = "line2";
	private static final String LINE3 = "line3";
	private static final String CITY = "city";
	private static final String IMAGES = "images";
	private static final String CLASSIFICATIONS = "classifications";
	private static final String SEGMENT = "segment";

    public List<Item> search(double lat, double lon, String keyword) {
    	//Encode keyword inURL since it may contain special characters
    	if (keyword == null) {
    		keyword = DEFAULT_KEYWORD;
    	}
    	try {
    		keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	//convert lat/lon to geohash
    	String geoHash = GeoHash.encodeGeohash(lat, lon, 8); //GeoHash API
    	
    	//Make URL "apikey=12345&geoPoint=abcd&keyword=music&radius=50"
    	String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s",
    			                    API_KEY, geoHash, keyword, 50);
    	
    	try {
    		//open a HTTP connection between your java application and ticketMaster based URL
    		HttpURLConnection connection = 
    				(HttpURLConnection) new URL(URL + "?" + query).openConnection();
    	    
    		//set request method to GET
    		connection.setRequestMethod("GET");
    	    //Send request to TicketMaster and get response, response code could be returned directly
    	    //response body is saved in inputStream of connection
    	    int responseCode = connection.getResponseCode();
    	    
    	    System.out.println("\nSending 'GET' request to URL: " + URL + "?" + query);
    	    System.out.println("Response code: " + responseCode);
    	    
    	    //test responseCode
    	    //now read response body to get events data
    	    
    	    StringBuilder response = new StringBuilder();
    	    
    	    try (BufferedReader in = new BufferedReader(
    	    		new InputStreamReader(connection.getInputStream()))){
    	    	String inputLine;
    	    	while ((inputLine = in.readLine()) != null) {
    	    		response.append(inputLine);
    	    	}
    	    }
    	    		
    	    
    	    JSONObject obj = new JSONObject(response.toString());
    	    if (obj.isNull("_embedded")) {
    	    	return new ArrayList<Item>(); //return empty array
    	    }
    	    
    	    JSONObject embedded = obj.getJSONObject("_embedded");
    	    JSONArray events = embedded.getJSONArray("events");
    	    
    	    return getItemList(events);
    	    
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return new ArrayList<Item>();
    }
    
    private String getAddress(JSONObject event) throws JSONException {
    	if (!event.isNull(EMBEDDED)) {
    		JSONObject embedded = event.getJSONObject(EMBEDDED);
    		
    		if (!embedded.isNull(VENUES)) {
    			JSONArray venues = embedded.getJSONArray(VENUES);
    			
    			
    			for (int i = 0; i < venues.length(); i++) {
    				JSONObject venue = venues.getJSONObject(i);
    				StringBuilder sBuilder = new StringBuilder();
    				
    				if (!venue.isNull(ADDRESS)) {
    					JSONObject address = venue.getJSONObject(ADDRESS);
    					if (!address.isNull(LINE1)) {
    						sBuilder.append(address.getString(LINE1));
    					}
    					if (!address.isNull(LINE2)) {
    						sBuilder.append('\n');
    						sBuilder.append(address.getString(LINE2));
    					}
    					if (!address.isNull(LINE3)) {
    						sBuilder.append('\n');
    						sBuilder.append(address.getString(LINE3));
    					}
    				}
    				
    				if(!venue.isNull(CITY)) {
    					JSONObject city = venue.getJSONObject(CITY);
    					if (!city.isNull(NAME)) {
    						sBuilder.append('\n');
    						sBuilder.append(city.getString(NAME));
    					}
    				}
    				
    				String addr = sBuilder.toString();
    				if (addr.length() > 0) {
    					return addr;
    				}
    			}
    		}
    	}
    	return " ";
    	
    }
    
 // {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
    private String getImageUrl (JSONObject event) throws JSONException {
    	if (!event.isNull(IMAGES)) {
			JSONArray array = event.getJSONArray(IMAGES);
			
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				
				if (!image.isNull(URL_STR)) {
					return image.getString(URL_STR);
				}
			}
		}
		return "";

    }
    
 // {"classifications" : [{"segment": {"name": "music"}}, ...]}
    private Set<String> getCategories(JSONObject event) throws JSONException {
    	Set<String> categories = new HashSet<>();

        if (!event.isNull(CLASSIFICATIONS)) {
            JSONArray classifications = event.getJSONArray(CLASSIFICATIONS);
            
            for (int i = 0; i < classifications.length(); ++i) {
                JSONObject classification = classifications.getJSONObject(i);
                
                if (!classification.isNull(SEGMENT)) {
                    JSONObject segment = classification.getJSONObject(SEGMENT);
                    
                    if (!segment.isNull(NAME)) {
                        categories.add(segment.getString(NAME));
                    }
                }
            }
        }

        return categories;
    }
    
 // Convert JSONArray to a list of item objects.
    private List<Item> getItemList(JSONArray events) throws JSONException {
    	List<Item> itemList = new ArrayList<Item>();
    	
    	for (int i = 0; i < events.length(); i++) {
    		JSONObject event = events.getJSONObject(i);
    		
    		ItemBuilder builder = new ItemBuilder();
    		if (!event.isNull(NAME)) {
    			builder.setName(event.getString(NAME));
    		}
    		
    		if (!event.isNull(ID)) {
    			builder.setItemId(event.getString(ID));
    		}
    		
    		if(!event.isNull(RATING)) {
    			builder.setRating(event.getDouble(RATING));
    		}
    		
    		if (!event.isNull(URL_STR)) {
    			builder.setUrl(event.getString(URL_STR));
    		}
    		
    		if(!event.isNull(DISTANCE)) {
    			builder.setDistance(event.getDouble(DISTANCE));
    		}
    		
    		builder.setImageUrl(getImageUrl(event));
    		builder.setCategories(getCategories(event));
    		builder.setAddress(getAddress(event));
    		
    		itemList.add(builder.build());
    	}
    	
    	return itemList;
    } 
    
    private void queryAPI(double lat, double lon) {
    	List<Item> itemList = search(lat, lon, null);
    	try {
    		for (Item item : itemList) {
    			JSONObject object = item.toJSONObject();
    			System.out.println(object);

    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
    /**
	 * Main entry for sample TicketMaster API requests.
	 */
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}


}
