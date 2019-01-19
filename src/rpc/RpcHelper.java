package rpc;
import entity.Item;
import entity.Item.ItemBuilder;

import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse; 
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
public class RpcHelper {
	
		// Writes a JSONArray to http response.
		public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
			PrintWriter out = response.getWriter();	
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			out.print(array);
			out.close();
		}
		// Writes a JSONObject to http response.
		public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {
			PrintWriter out = response.getWriter();	
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			out.print(obj);
			out.close();
		}
		
		//remote procedure call
		public static JSONObject readJSONObject(HttpServletRequest request) {
			StringBuilder sBuilder = new StringBuilder();
			try(BufferedReader reader = request.getReader()) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					sBuilder.append(line);
				}
				return new JSONObject(sBuilder.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return new JSONObject();
		}
		
		// Converts a list of Item objects to JSONArray.
		  public static JSONArray getJSONArray(List<Item> items) {
		    JSONArray result = new JSONArray();
		    try {
		      for (Item item : items) {
		        result.put(item.toJSONObject());
		      }
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    return result;
		  }

}
