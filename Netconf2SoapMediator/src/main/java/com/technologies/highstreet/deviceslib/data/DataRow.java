package com.technologies.highstreet.deviceslib.data;

import java.util.ArrayList;

public class DataRow extends ArrayList<Object>{
	/**
	 *
	 */
	private static final long serialVersionUID = -4503230493466281384L;
	private static final Object EMPTY = "";
	private static int keyHelper=1;
	private final String key;
	public String getKey() {return this.key;}
	public DataRow()
	{
		this((String)null);
	}
	public DataRow(Object[] values)
	{
		this(String.format("%d",keyHelper++),values);
	}
	public DataRow(String k,Object[] values) {
		this.key=k;
		if(values!=null)
		{
			for(Object o:values)
				this.add(o);
		}
	}
	public DataRow(String k) {
		this(k,null);
	}
	public void setSize(int size) {
		if (size > this.size()) {
			while (size > this.size())
				this.add(EMPTY);
		} else if (size < this.size())// reduce rows
		{
			while (size < this.size())
				this.remove(this.size() - 1);
		}
	}

	public Object getValueAt(int col) {
		if (col < this.size())
			return this.get(col);
		return null;
	}

	public boolean setValueAt(int col, Object value) {
		if(col<this.size())
		{
			this.set(col, value);
			return true;
		}
		return false;
	}
	public String toString(String seperator) {
		String s="";
		if(this.size()>0)
			s=this.getString(0);
		for(int i=1;i<this.size();i++)
			s+=seperator+this.getString(i);
		return s;
	}
	private String getString(int i) {
		if(i<this.size() && this.get(i)!=null)
			return this.get(i).toString();
		return "null";
	}
}
