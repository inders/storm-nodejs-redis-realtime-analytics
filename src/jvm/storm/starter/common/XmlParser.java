package storm.starter.common;

import java.io.StringReader;
import java.util.UUID;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;

public class XmlParser {
  public static String[] parser(String xmlDoc, String timeMarker) throws Exception{


    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputSource inputStream = new InputSource();
    inputStream.setCharacterStream(new StringReader(xmlDoc));
    ArrayList<String> listJson = new ArrayList<String>();
    Document doc = db.parse(inputStream);
    NodeList nodes = doc.getElementsByTagName("entry");
    String time;

    for (int i = 0; i < nodes.getLength(); i++) {
      Entry entry = new Entry();
      Gson gson = new Gson();
      
      Element element = (Element) nodes.item(i);

      UUID uuid = UUID.randomUUID();
      entry.setId(uuid.toString());
      
      NodeList title = element.getElementsByTagName("title");
      Element line = (Element) title.item(0);
      
      entry.setTitle(getCharacterDataFromElement(line));


      NodeList content = element.getElementsByTagName("content");
      line = (Element) content.item(0);
      entry.setContent(getCharacterDataFromElement(line));
      
      NodeList link = element.getElementsByTagName("link");
      line = (Element) link.item(0);
      entry.setLink(line.getAttribute("href"));
      
      NodeList published = element.getElementsByTagName("published");
      line = (Element) published.item(0);
      time = getCharacterDataFromElement(line);
      if(time == timeMarker)
      {
    	  break;
      }
      else
      {
      entry.setPublishedAt(getCharacterDataFromElement(line));
      }
      
      NodeList authNode = element.getElementsByTagName("author");
      Element authele = (Element) authNode.item(0);
      NodeList name = authele.getElementsByTagName("name");
      line = (Element) name.item(0);
      entry.setAuthor(getCharacterDataFromElement(line));
      String entryJson = gson.toJson(entry);
      //System.out.println(entryJson);
      listJson.add(entryJson);
      
      
    }
    String[] jsonString = new String[listJson.size()];
    listJson.toArray( jsonString );
    return jsonString;
    
  }

  public static String getCharacterDataFromElement(Element e) {
    Node child = e.getFirstChild();
    if (child instanceof CharacterData) {
      CharacterData cd = (CharacterData) child;
      return cd.getData();
    }
    return "";
  }
}
