package de.fernunihagen.d2l2.types;

public class CoreferenceEntity {
	
	int id;
	int begin;
	int end;
	String name;
	String firstMention;
	public CoreferenceEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CoreferenceEntity(int id, int begin, int end, String name, String firstMention) {
		super();
		this.id = id;
		this.begin = begin;
		this.end = end;
		this.name = name;
		this.firstMention = firstMention;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFirstMention() {
		return firstMention;
	}
	public void setFirstMention(String firstMention) {
		this.firstMention = firstMention;
	}
	
	
}
