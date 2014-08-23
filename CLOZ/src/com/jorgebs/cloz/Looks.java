package com.jorgebs.cloz;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Looks")
public class Looks {
	
	@DatabaseField(generatedId = true)
	public int id;
	
	@DatabaseField
	public String fileName;
	
	@DatabaseField
	public String contacts;
	
	@DatabaseField
	public String date;
	
	@DatabaseField
	public String tags;
}
