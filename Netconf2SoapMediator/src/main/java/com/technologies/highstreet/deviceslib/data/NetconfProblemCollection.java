package com.technologies.highstreet.deviceslib.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class NetconfProblemCollection extends ArrayList<NetconfProblemListItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3422394216944296825L;
	private boolean mSeqNoGenerationRandom = false;

	private int generateSequenceNumber() {
		int s;
		if (mSeqNoGenerationRandom) {
			Random rnd=new Random();
			s=rnd.nextInt();
			
		} 
		else {
			s = 1;
			int i = 0;
			boolean found = true;
			while (found) {
				found = false;
				for (i = 0; i < this.size(); i++) {
					if (this.get(i).SequenceNumber == s)

					{
						found = true;
						s++;
						break;
					}
				}
			}
		}
		return s;
	}

	public NetconfProblemListItem Add(String desc, NetconfSeverity s, Date ts) {

		NetconfProblemListItem item = new NetconfProblemListItem(this.generateSequenceNumber(), desc, s, ts);
		this.add(item);
		return item;
	}

}
