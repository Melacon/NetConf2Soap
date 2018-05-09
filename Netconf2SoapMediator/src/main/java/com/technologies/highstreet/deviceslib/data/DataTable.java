package com.technologies.highstreet.deviceslib.data;

import java.util.ArrayList;
import java.util.List;

public class DataTable {

	private final List<String> columns;
	private final List<DataRow> rows;
	private final String baseOID;
	public String getBase() {return this.getBase();}
	
	public DataTable() {
		this("",new String[] {},0);
	}

	public DataTable(String base,String[] cols,int numRows) {
		this.baseOID=base;
		this.columns = new ArrayList<String>();
		if (cols != null) {
			for (String c : cols)
				this.columns.add(c);
		}
		this.rows=new ArrayList<DataRow>();
		while(numRows-->0)
			this.rows.add(null);
	}

	public DataTable(String base, String[] cols, String[] rows) {
		this.baseOID=base;
		this.columns = new ArrayList<String>();
		if (cols != null) {
			for (String c : cols)
				this.columns.add(c);
		}
		this.rows=new ArrayList<DataRow>();
		for(int i=0;i<rows.length;i++)
			this.rows.add(new DataRow(rows[i]));
	}

	public void AddColumn(String colName) {
		this.columns.add(colName);
		for(DataRow row:this.rows)
			row.setSize(this.columns.size());
	}
	public void AddRow(Object[] values)
	{
		DataRow row=new DataRow(values);
		row.setSize(this.columns.size());
		this.rows.add(row);
	}
	public Object getValueAt(String col,int row)
	{
		return getValueAt(this.columns.indexOf(col),row);
	}
	public Object getValueAt(int col,int row)
	{
		if(row<rows.size())
			return rows.get(row).getValueAt(col);
		return null;
	}
	public void setValueAt(String col,int row,Object value)
	{
		this.setValueAt(this.columns.indexOf(col), row, value);
	}
	public boolean setValueAt(int col,int row,Object value)
	{
		if(col<this.columns.size() && row<this.rows.size())
		{
			DataRow r = this.rows.get(row);
			return r.setValueAt(col,value);
		}
		return false;
	}
	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		for(String col:this.columns)
			sb.append(col+"\t");
		sb.append("\n");
		for(DataRow row:this.rows)
			sb.append(row.toString("\t")+"\n");
		return sb.toString();
	}
}

