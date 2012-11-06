/**
 * 
 */
package storm.starter.common;

import com.google.gson.Gson;

import storm.starter.bolt.classification.SentimentClass;

/**
 * @author jaydeep.vishwakarma
 * 
 */
public class Entry {

  private String id;
  private String channel;
  private String title;
  private String content;
  private String publishedAt;
  private String link;
  private String author;
  private SentimentClass sentiment;

  public SentimentClass getSentiment() {
    return sentiment;
  }

  public void setSentiment(SentimentClass sentiment) {
    this.sentiment = sentiment;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getChannel() {
	    return channel;
	  }

	  public void setChannel(String channel) {
	    this.channel = channel;
	  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(String publishedAt) {
    this.publishedAt = publishedAt;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Override
  public String toString() {
    return "Entry{id='" + id + "', title='" + title + "', content='" + content + "', publishedat='" + publishedAt + "', link='" + link
        + "', author='" + author + "'}";
  }
  
  public static Entry fromJson(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, Entry.class);
  }

}
