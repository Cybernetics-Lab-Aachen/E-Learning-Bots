package crawler;

public class alchemyResults {
	// source attributes
	String sourceURL = "";
	String language = "";
	String text = "";
	String url = "";
	// entities attributes
	String[] entities = new String[7];
	// keywords attributes
	String[] keywords = new String[8];
	// concepts attributes
	String[] concepts = new String[4];

	public String getSourceURL() {
		return sourceURL;
	}

	public void setSourceURL(String sourceURL) {
		this.sourceURL = sourceURL;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getEntity(int arrayField) {
		return entities[arrayField];
	}

	public void setEntity(String entity, int arrayField) {
		this.entities[arrayField] = entity;
	}

	public String getKeyword(int arrayField) {
		return keywords[arrayField];
	}

	public void setKeyword(String keywords, int arrayField) {
		this.keywords[arrayField] = keywords;
	}

	public String getConcept(int arrayField) {
		return concepts[arrayField];
	}

	public void setConcept(String concepts, int arrayField) {
		this.concepts[arrayField] = concepts;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String[] getEntities() {
		return entities;
	}
	public void setEntities(String[] entity) {
		this.entities = entity;
	}
	public String[] getKeywords() {
		return keywords;
	}
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
	public String[] getConcepts() {
		return concepts;
	}
	public void setConcepts(String[] concepts) {
		this.concepts = concepts;
	}
	
	
	public alchemyResults() {
		super();
		
		

		// TODO Auto-generated constructor stub
	}
}
