/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

/**
 * @author Micha
 *
 */

//public class NodeEditConfigCollection extends ArrayList<NodeEditConfig>
//Prepare usage of other list types
public class NodeEditConfigCollection implements Iterable<NodeEditConfig> {

    public static NodeEditConfigCollection EMPTY =  new NodeEditConfigCollection();

    private final List<NodeEditConfig> list = new ArrayList<>();

    public void setTextContent(String value) {
        for(NodeEditConfig e:list) {
            e.getElement().setTextContent(value);
        }
    }

    public int size() {
        return list.size();
    }

    public void add(NodeEditConfig editConfigNode) {
        if (editConfigNode == null) {
            throw new IllegalArgumentException("editConfigNode shouldn't be null");
        }
        list.add(editConfigNode);
    }

    public NodeEditConfig find(Element e) {
        for(NodeEditConfig c: list)
        {
            if(c.getElement().equals(e)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Iterator<NodeEditConfig> iterator() {
        return list.iterator();
    }

}
