/*
 * CompositeList.java July 2006
 *
 * Copyright (C) 2006, Niall Gallagher <niallg@users.sf.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General 
 * Public License along with this library; if not, write to the 
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 * Boston, MA  02111-1307  USA
 */

package xml.serializer.load;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xml.serializer.ElementList;

import java.util.Collection;

/**
 * The <code>CompositeList</code> object is used to convert an element
 * list to a collection of element entries. This in effect performs a 
 * root serialization and deserialization of entry elements for the
 * collection object. On serialization each objects type must be 
 * checked against the XML annotation entry so that it is serialized
 * in a form that can be deserialized. 
 * <pre>
 *
 *    &lt;list&gt;
 *       &lt;entry attribute="value"&gt;
 *          &lt;text&gt;example text value&lt;/text&gt;
 *       &lt;/entry&gt;
 *       &lt;entry attribute="demo"&gt;
 *          &lt;text&gt;some other example&lt;/text/&gt;
 *       &lt;/entry&gt;
 *    &lt;/list&gt;
 * 
 * </pre>
 * For the above XML element list the element <code>entry</code> is
 * contained within the list. Each entry element is thus deserialized
 * as a root element and then inserted into the list. This enables
 * lists to be composed from XML documents. For serialization the
 * reverse is done, each element taken from the collection is written
 * as a root element to the owning element to create the list. 
 * Entry objects do not need to be of the same type.
 * 
 *
 * @see xml.serializer.load.Traverser
 * @see ElementList
 */ 
final class CompositeList implements Converter {

   /**
    * This factory is used to create a suitable collection list.
    */         
   private CollectionFactory factory;

   /**
    * This performs the traversal used for object serialization.
    */ 
   private Traverser root;
      
   /**
    * This is the entry type for elements within the list.
    */   
   private Class entry;

   /**
    * Constructor for the <code>CompositeList</code> object. This is
    * given the list type and entry type to be used. The list type is
    * the <code>Collection</code> implementation that deserialized
    * entry objects are inserted into. 
    *
    * @param root this is the source object used for serialization
    * @param type this is the collection type for the list used
    * @param entry the entry type to be stored within the list
    */    
   public CompositeList(Source root, Class type, Class entry) {
      this.factory = new CollectionFactory(type); 
      this.root = new Traverser(root);      
      this.entry = entry;
   }

   /**
    * This <code>read</code> method wll read the XML element list from
    * the provided node and deserialize its children as entry types.
    * This will each entry type is deserialized as a root type, that 
    * is, its <code>Root</code> annotation must be present and the
    * name of the entry element must match that root element name.
    * 
    * @param node this is the DOM element that is to be deserialized
    * 
    * @return this returns the item to attach to the object field
    */ 
   public Object read(Node node) throws Exception{
      Collection list = factory.getInstance(node);
      NodeList nodes = node.getChildNodes();
      
      for(int i = 0; i < nodes.getLength(); i++) {
         Node next = nodes.item(i);
         
         if(next instanceof Element) {
            list.add(root.read(next, entry));
         }        
      }
      return list;
   }      

   /**
    * This <code>write</code> method will write the specified object
    * to the given DOM element as as list entries. Each entry within
    * the given collection must be assignable from the annotated 
    * type specified within the <code>ElementList</code> annotation.
    * Each entry is serialized as a root element, that is, its
    * <code>Root</code> annotation is used to extract the name. 
    * 
    * @param source this is the source collection to be serialized 
    * @param node this is the DOM element container to be populated
    */ 
   public void write(Object source, Element node) throws Exception {
      Collection list = (Collection) source;                
      
      for(Object item : list) {
         Class type = item.getClass();

         if(!type.isAssignableFrom(entry)) {
            throw new RuntimeException("Type does not match list entry");                     
         }
         Element next = root.write(item);
         node.appendChild(next);
      }
   }
}
