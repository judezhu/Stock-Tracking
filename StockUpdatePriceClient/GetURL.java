/**
 * CICS 525 Assignment 1
 * 
 * Huang Zhu 	15509094
 * Jiahua Chen 	87269122
 * Mu-Jen Wang	44371029
 * 
 * getURL.java: utility program to get stock information online
 * 
 * code example taken from
 * http://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
 */

import java.io.*;
import java.net.*;

/**
 * 
 * given an URL, get the HTML response in String
 *
 */
public class GetURL {

   public static String getHTML(String urlToRead) {
      URL url;
      HttpURLConnection conn;
      BufferedReader rd;
      String line;
      String result = "";
      try {
         url = new URL(urlToRead);
         conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         while ((line = rd.readLine()) != null) {
            result += line;
         }
         rd.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   /* for testing purpose, not used
   public static void main(String args[])
   {
    
     String result = GetURL.getHTML("http://download.finance.yahoo.com/d/quotes.csv?s=GOOG&f=l1=.csv)");
     int commaIndex = result.indexOf(',');
     String field0 = result.substring(0, commaIndex);
     System.out.println("field0: "+field0);
     double price = Double.parseDouble(field0);
     System.out.println("price: "+price);

 }
 */
   
}
