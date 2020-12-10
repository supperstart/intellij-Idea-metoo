package com.metoo.core.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.record.formula.functions.T;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonUtil {
	
	// 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
	 public static String loadJSON (String method,String paramJson) {
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
		 StringBuffer url = new StringBuffer();
		 url.append("https://router.jd.com/api?");
		 url.append("method=").append(method);
		 url.append("&app_key=").append(SysPropertiesUtil.getProperty("APP_KEY").trim());
		 url.append("&access_token=").append(SysPropertiesUtil.getProperty("ACCESS_TOKEN").trim());
		 url.append("&timestamp=").append(sdf.format(new Date()));
		 url.append("&v=1.0&format=json&param_json=").append(paramJson);
		 
	        StringBuilder json = new StringBuilder();
	        try {
	        	System.out.println(".......................");
	        	System.out.println(url.toString());
	            URL oracle = new URL(url.toString());
	            URLConnection yc = oracle.openConnection();
	            BufferedReader in = new BufferedReader(new InputStreamReader(
	                                        yc.getInputStream(),"utf-8"));
	            String inputLine = null;
	            while ( (inputLine = in.readLine()) != null) {
	                json.append(inputLine);
	            }
	            in.close();
	        } catch (MalformedURLException e) {
	        	System.out.println("////////////");
	        	System.out.println(e.getMessage());
	        } catch (IOException e) {
	        	System.out.println("\\\\\\\\\\\\\\");
	        	System.out.println(e.getMessage());
	        }
	        
	        return json.toString();
	    }
	 
	 public static Map<String,Object> parseJson(String jsonString){
			JSONObject jsonObject = JSONObject.fromObject(jsonString);
			Map<String,Object> map = new HashMap<String,Object>();   
	        for (Iterator iter = jsonObject.keys(); iter.hasNext();) {   
	            String key = (String) iter.next();   
	            map.put(key, jsonObject.get(key));   

	            Map<String,Object> childMap = JSONObject.fromObject(map.get(key));
	            for(Entry<String, Object> entry1:childMap.entrySet()){
	            	if("result".equals(entry1.getKey())){
	            		if(!"".equals(entry1.getValue())){
	            			Map<String,Object> secondMap = JSONObject.fromObject(childMap.get(entry1.getKey()));
		            		
		            		return secondMap;
	            		}
	            		return null;
	                	
	            	}
	            }

	        }
	        return null;
		}

	 public static String loadJSON2 (String method,String paramJson) {
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
		 StringBuffer url = new StringBuffer();
		 url.append("https://router.jd.com/api?");
		 url.append("method=").append(method);
		 url.append("&app_key=e2fac8e532e9423ab75fd0ba5d32922d");
		 url.append("&access_token=c9301cb80c314cb79ea45bfeed0383768");
		 url.append("&timestamp=").append(sdf.format(new Date()));
		 url.append("&v=1.0&format=json&param_json=").append(paramJson); 
		 
		 URLConnection connection = null;
         try {
             connection = new URL(url.toString()).openConnection();
             connection.connect();
 

            InputStream fin = connection.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(fin,"utf-8"));
             StringBuffer buffer = new StringBuffer();
             String temp = null;
             while ((temp = br.readLine()) != null) {
                 buffer.append(temp);
             }
             
             System.out.println(buffer.toString());
             
             return buffer.toString();
 
        } catch (IOException e) {
             e.printStackTrace();
         } 
         
         return null;
	 }
	 
	 
	 /**
	  * 适用于[{"name":1,"num":1},{"name":2,"num":2}]
	  * @param jsonMessage
	  * @return
	  */
	public static  List<Map<String,Object>> parseJsonArray(String jsonMessage){
		List<Map<String,Object>> listMap = new ArrayList<Map<String,Object>>();
		JSONObject jsonObject = JSONObject.fromObject(jsonMessage);
		Map<String,Object> map = new HashMap<String,Object>();   
        for (Iterator iter = jsonObject.keys(); iter.hasNext();) {   
            String key = (String) iter.next();   
            map.put(key, jsonObject.get(key));   

            Map<String,Object> childMap = JSONObject.fromObject(map.get(key));
            for(Entry<String, Object> entry1:childMap.entrySet()){
            	if("result".equals(entry1.getKey())){
            		if(!"".equals(entry1.getValue())){
            			JSONArray jsonArray = JSONArray.fromObject(entry1.getValue());
            			for (Iterator it = jsonArray.iterator(); it
								.hasNext();) {
            				Map<String,Object> mm = JSONObject.fromObject( it.next());
							listMap.add(mm);
						}
            		}
                	
            	}
            }

        }
		
		return listMap;
	}
	
	
	 
	 /**
	  * 适用于{"id":[{"name":1,"num":1},{"name":2,"num":2}]}
	  * @param jsonMessage
	  * @return
	  */
	public static  Map<String,List<Map<String,Object>>> parseJsonMap(String jsonMessage){
		Map<String,List<Map<String,Object>>> resultMap = new HashMap<String,List<Map<String,Object>>>();
		List<Map<String,Object>> listMap = new ArrayList<Map<String,Object>>();
		JSONObject jsonObject = JSONObject.fromObject(jsonMessage);
		Map<String,Object> map = new HashMap<String,Object>();   
       for (Iterator iter = jsonObject.keys(); iter.hasNext();) {   
           String key = (String) iter.next();   
           map.put(key, jsonObject.get(key));   

           Map<String,Object> childMap = JSONObject.fromObject(map.get(key));
           for(Entry<String, Object> entry1:childMap.entrySet()){
           	if("result".equals(entry1.getKey())){
           		if(!"null".equals(entry1.getValue()) || !"".equals(entry1.getValue())){
           			Map<String,Object> reMap = JSONObject.fromObject(childMap.get(entry1.getKey()));
           			for(Entry<String, Object> en1:reMap.entrySet()){
               			JSONArray jsonArray = JSONArray.fromObject(en1.getValue());
               			for (Iterator it = jsonArray.iterator(); it
   								.hasNext();) {
               				Map<String,Object> mm = JSONObject.fromObject( it.next());
   							listMap.add(mm);
   						}
               			resultMap.put(en1.getKey(), listMap);
           			}
           		}
               	
           	}
           }

       }
		
		return resultMap;
	}

	/**
	 * 适用于{"1","2","3 "}
	 * @param jsonMessage
	 * @return
	 */
	 public static List<String> parseString(String jsonMessage){
		 List<String> resultList = new ArrayList<String>();
		 JSONObject jsonObject = JSONObject.fromObject(jsonMessage);
			Map<String,Object> map = new HashMap<String,Object>();   
	        for (Iterator iter = jsonObject.keys(); iter.hasNext();) {   
	            String key = (String) iter.next();   
	            map.put(key, jsonObject.get(key));   

	            Map<String,Object> childMap = JSONObject.fromObject(map.get(key));
	            for(Entry<String, Object> entry1:childMap.entrySet()){
	            	if("result".equals(entry1.getKey())){
	            		if(!"".equals(entry1.getValue())){
	            			String str = (String) entry1.getValue();
	            			String[] strList = str.split(",");
	            			if(strList.length > 0){
	            				resultList = Arrays.asList(strList);
	            			}
	            		}
	            	}
	            }

	        }
	        return resultList ;
	 }
	 
	 
	/**
	 * @param map
	 * @return
	 */
	 public static String mapToJson(Map<String, Object> map){
		 
		 
	        //Gson gson = new Gson();
	       // return gson.toJson(map, Map.class);
		 return  JSONObject.fromObject(map).toString();
	    }

	 //2
	 public static String mapToJson2(List<T> childs){
		 
		 
	        //Gson gson = new Gson();
	       // return gson.toJson(map, Map.class);
		 return  JSONArray.fromObject(childs).toString();
	    }
	 
	 /**
	  * 对象转json
	  * @param data
	  * @return
	  * @throws IOException
	  */
	 public static String objectToJson(Object data) throws IOException {
	        try {
	            String string = MAPPER.writeValueAsString(data);
	            return string;
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	
	 public static String toJSONString(Object object)
	    {
	        JSONArray jsonArray = JSONArray.fromObject(object);
	        
	        return jsonArray.toString();
	    }
	

}
