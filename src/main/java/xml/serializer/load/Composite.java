/*
 * Composite.java July 2006
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xml.serializer.load.exceptions.AttributeException;
import xml.serializer.load.exceptions.ElementException;
import xml.serializer.load.exceptions.FieldRequiredException;

import java.lang.reflect.Field;

/**
 * The <code>Composite</code> object is used to perform serialization
 * of objects that contain XML annotation. Composite objects are objects
 * that are not primitive and contain references to serializable fields.
 * This <code>Converter</code> will visit each field within the object
 * and deserialize or serialize that field depending on the requested
 * action. If a required field is not present when deserializing from
 * a DOM element this terminates the deserialization reports the error.
 * <pre>
 * 
 *    &lt;element name="test" class="some.package.Type"&gt;
 *       &lt;text&gt;string value&lt;/text&gt;
 *       &lt;integer&gt;1234&lt;/integer&gt;
 *    &lt;/element&gt;
 * 
 * </pre>
 * To deserialize the above XML source this will attempt to match the
 * attribute name with an <code>Attribute</code> annotation from the
 * XML schema class, which is specified as "some.package.Type". This
 * type must also contain <code>Element</code> annotations for the
 * "text" and "integer" elements.
 * <p>
 * Serialization requires that all fields marked as required must have
 * values that are not null. This ensures that the serialized object
 * can be deserialized at a later stage using the same class schema.
 * If a required field is null the serialization terminates an an
 * exception is thrown.
 * 
 */
final class Composite implements Converter {
  
   /**
    * This factory creates instances of the deserialized object.
    */
   private ObjectFactory factory;

   /**
    * This is the source object for the instance of serialization.
    */
   private Source root;
        
   /**
    * Constructor for the <code>Composite</code> object. This creates 
    * a converter object capable of serializing and deserializing root
    * objects labeled with XML annotations. The XML schema class must 
    * be given to the instance in order to perform deserialization.
    *  
    * @param root the source object used to perform serialization
    * @param type this is the XML schema class to use
    */
   public Composite(Source root, Class type) {
      this.factory = new ObjectFactory(type);           
      this.root = root;
   }

   /**
    * This <code>read</code> method performs deserialization of the XML
    * schema class type by traversing the fields and instantiating them
    * using details from the provided DOM element. Because this will
    * convert a non-primitive value it delegates to other converters to
    * perform deserialization of lists and primitives.
    * <p>
    * If any of the required fields are not present within the provided
    * DOM element this will terminate deserialization and throw an
    * exception. The annotation missing is reported in the exception.
    * 
    * @param node the DOM element field values are deserialized from
    * 
    * @return this returns the fully deserialized object graph
    */
   public Object read(Node node) throws Exception {
      Object source = factory.getInstance(node);      
      read(node, source);
      return source;
   }
   
   /**
    * This <code>read</code> method performs deserialization of the XML
    * schema class type by traversing the fields and instantiating them
    * using details from the provided DOM element. Because this will
    * convert a non-primitive value it delegates to other converters to
    * perform deserialization of lists and primitives.
    * <p>
    * If any of the required fields are not present within the provided
    * DOM element this will terminate deserialization and throw an
    * exception. The annotation missing is reported in the exception.
    * 
    * @param node the DOM element field values are deserialized from
    * @param source the object whose fields are to be deserialized
    */
   private void read(Node node, Object source) throws Exception {
      Visitor visitor = root.getVisitor(source);           
      read(node, source, visitor);
   }
   
   /**
    * This <code>read</code> method performs deserialization of the XML
    * schema class type by traversing the fields and instantiating them
    * using details from the provided DOM element. Because this will
    * convert a non-primitive value it delegates to other converters to
    * perform deserialization of lists and primitives.
    * <p>
    * If any of the required fields are not present within the provided
    * DOM element this will terminate deserialization and throw an
    * exception. The annotation missing is reported in the exception.
    * 
    * @param node the DOM element field values are deserialized from
    * @param source ths object whose fields are to be deserialized
    * @param visitor this object visits the objects fields
    */
   private void read(Node node, Object source, Visitor visitor) throws Exception {
      readAttributes(node, source, visitor);
      readElements(node, source, visitor);
   }   

   /**
    * This <code>read</code> method is used to read the attributes from
    * the provided DOM element. This will iterate over all attributes
    * within the element and convert those attributes as primitives to
    * field values within the source object.
    * <p>
    * Once all attributes within the DOM element have been evaluated
    * the <code>Visitor</code> is checked to ensure that there are no
    * required fields annotated with the <code>Attribute</code> that
    * remain. If any required attribute remains an exception is thrown. 
    * 
    * @param node this is the DOM element to be evaluated
    * @param source the source object which will be deserialized
    * @param visitor this is used to visit the attribute fields
    * 
    * @throws Exception thrown if any required attributes remain
    */
   private void readAttributes(Node node, Object source, Visitor visitor) throws Exception {
      NamedNodeMap list = node.getAttributes();
      LabelMap map = visitor.getAttributes();

      for(int i = 0; i < list.getLength(); i++) {
         readAttribute(list.item(i), source, map);
      }  
      readCheck(map, source);
   }

   /**
    * This <code>read</code> method is used to read the elements from
    * the provided DOM element. This will iterate over all elements
    * within the element and convert those elements to primitives or
    * composite objects depending on the field annotation.
    * <p>
    * Once all elements within the DOM element have been evaluated
    * the <code>Visitor</code> is checked to ensure that there are no
    * required fields annotated with the <code>Element</code> that
    * remain. If any required element remains an exception is thrown. 
    * 
    * @param node this is the DOM element to be evaluated
    * @param source the source object which will be deserialized
    * @param visitor this is used to visit the element fields
    * 
    * @throws Exception thrown if any required elements remain
    */
   private void readElements(Node node, Object source, Visitor visitor) throws Exception {
      NodeList list = node.getChildNodes();
      LabelMap map = visitor.getElements();
      
      for(int i = 0; i < list.getLength(); i++) {
         Node next = list.item(i);
         
         if(next instanceof Element)
            readElement(next, source, map);
      } 
      readCheck(map, source);
   }
   
   /**
    * This <code>read</code> method is used to perform deserialization
    * of the provided node object using a delegate converter. This is
    * typically another <code>Composite</code> converter, or if the
    * node is an attribute a <code>Primitive</code> converter. When
    * the delegate converter has completed the deserialized value is
    * assigned to the field.
    * 
    * @param node this is the node that contains the field value
    * @param source the source object to assign the field value to
    * @param map this is the map that contains the label objects
    * 
    * @throws Exception thrown if the the label object does not exist
    */
   private void readAttribute(Node node, Object source, LabelMap map) throws Exception {
      String name = node.getNodeName();
      Label label = map.remove(name);
      
      if(label == null) {
         throw new AttributeException("Attribute '%s' does not exist", name);
      }
      read(node, source, label);
   }

   /**
    * This <code>read</code> method is used to perform deserialization
    * of the provided node object using a delegate converter. This is
    * typically another <code>Composite</code> converter, or if the
    * node is an attribute a <code>Primitive</code> converter. When
    * the delegate converter has completed the deserialized value is
    * assigned to the field.
    * 
    * @param node this is the node that contains the field value
    * @param source the source object to assign the field value to
    * @param map this is the map that contains the label objects
    * 
    * @throws Exception thrown if the the label object does not exist
    */
   private void readElement(Node node, Object source, LabelMap map) throws Exception {
      String name = node.getNodeName();
      Label label = map.remove(name);
      
      if(label == null) {
         throw new ElementException("Element '%s' does not exist", name);
      }
      read(node, source, label);
   }
   
   
   /**
    * This <code>read</code> method is used to perform deserialization
    * of the provided node object using a delegate converter. This is
    * typically another <code>Composite</code> converter, or if the
    * node is an attribute a <code>Primitive</code> converter. When
    * the delegate converter has completed the deserialized value is
    * assigned to the field.
    * 
    * @param node this is the node that contains the field value
    * @param source the source object to assign the field value to
    * @param label this is the label used to create the converter
    * 
    * @throws Exception thrown if the field could not be deserialized
    */
   private void read(Node node, Object source, Label label) throws Exception {      
      Converter reader = label.getConverter(root);
      Field field = label.getField();      
      Object object = reader.read(node);
     
      field.set(source, object);
   }
   
   /**
    * This method checks to see if there are any <code>Label</code>
    * objects remaining in the provided map that are required. This is
    * used when deserialization is performed to ensure the the DOM
    * element deserialized contains sufficient details to satisfy the
    * XML schema class annotations. If there is a required label that
    * remains it is reported within the exception thrown.
    * 
    * @param map this is the map to check for remaining labels
    * @param source this is the object that has been deserialized 
    * 
    * @throws Exception thrown if an XML property was not declared
    */
   private void readCheck(LabelMap map, Object source) throws Exception {
      Class type = source.getClass();
      String name = type.getName();
      
      for(Label label : map) {
         if(label.isRequired()) {
            throw new FieldRequiredException("Unable to satisfy %s for %s", label,  name);
         }
      }      
   }
   
   /**
    * This <code>write</code> method is used to perform serialization of
    * the given source object. Serialization is performed by appending
    * elements and attributes from the source object to the provided DOM
    * element object. How the objects fields are serialized is 
    * determined by the XML schema class that the source object is an
    * instance of. If a required field is null an exception is thrown.
    * 
    * @param source this is the source object to be serialized
    * @param node the DOM element the object is to be serialized to 
    * 
    * @throws Exception thrown if there is a serialization problem
    */
   public void write(Object source, Element node) throws Exception {
      Visitor visitor = root.getVisitor(source);           
      write(source, node, visitor);
   }
   
   /**
    * This <code>write</code> method is used to perform serialization of
    * the given source object. Serialization is performed by appending
    * elements and attributes from the source object to the provided DOM
    * element object. How the objects fields are serialized is 
    * determined by the XML schema class that the source object is an
    * instance of. If a required field is null an exception is thrown.
    * 
    * @param source this is the source object to be serialized
    * @param node the DOM element the object is to be serialized to
    * @param visitor this is used to track the referenced fields 
    * 
    * @throws Exception thrown if there is a serialization problem
    */
   private void write(Object source, Element node, Visitor visitor) throws Exception {
      writeAttributes(source, node, visitor);
      writeElements(source, node, visitor);      
   }

   /**
    * This write method is used to write all the attribute fields from
    * the provided source object to the DOM element. This visits all
    * the fields marked with the <code>Attribute</code> annotation in
    * the source object. All annotated fields are written as attributes
    * to the DOM element. This will throw an exception if a required
    * field within the source object is null. 
    * 
    * @param source this is the source object to be serialized
    * @param node this is the DOM element to write attributes to
    * @param visitor this is used to track the referenced attributes
    * 
    * @throws Exception thrown if there is a serialization problem
    */
   private void writeAttributes(Object source, Element node, Visitor visitor) throws Exception {
      LabelMap attributes = visitor.getAttributes();

      for(Label label : attributes) {
         Field field = label.getField();
         Object value = field.get(source);
         
         if(label.isRequired() && value == null) {
            throw new AttributeException("Value for %s is null", label);
         }
         writeAttribute(value, node, label);              
      }      
   }

   /**
    * This write method is used to write all the element fields from
    * the provided source object to the DOM element. This visits all
    * the fields marked with the <code>Element</code> annotation in
    * the source object. All annotated fields are written as elements
    * to the DOM element. This will throw an exception if a required
    * field within the source object is null. 
    * 
    * @param source this is the source object to be serialized
    * @param node this is the DOM element to write elements to
    * @param visitor this is used to track the referenced elements
    * 
    * @throws Exception thrown if there is a serialization problem
    */
   private void writeElements(Object source, Element node, Visitor visitor) throws Exception {
      LabelMap elements = visitor.getElements();
      
      for(Label label : elements) {
         Field field = label.getField();
         Object value = field.get(source);
                 
         if(label.isRequired() && value == null) {
            throw new ElementException("Value for %s is null", label);
         }
         writeElement(value, node, label);
      }         
   }
   
   /**
    * This write method is used to set the value of the provided object
    * as an attribute to the DOM element. This will acquire the string
    * value of the object using <code>toString</code> only if the
    * object provided is not an enumerated type. If the object is an
    * enumerated type then the <code>Enum.name</code> method is used.
    * 
    * @param value this is the value to be set as an attribute
    * @param node this is the DOM element to write the attribute to
    * @param label the label that contains the field details
    * 
    * @throws Exception thrown if there is a serialization problem
    */
   private void writeAttribute(Object value, Element node, Label label) throws Exception {
      if(value != null) {
         String name = label.getName();
         String text = value.toString();
         
         if(value instanceof Enum) {
            Enum type = (Enum) value;
            text = type.name();
         }
         node.setAttribute(name, text);
      }
   }
   
   /**
    * This write method is used to append the provided object as an
    * element to the given DOM element object. This will recursively
    * write the fields from the provided object as elements. This is
    * done using the <code>Converter</code> acquired from the field
    * label. If the type of the field value is not of the same
    * type as the XML schema class a "class" attribute is appended.
    * 
    * @param value this is the value to be set as an element
    * @param node this is the DOM element to write the element to
    * @param label the label that contains the field details
    * 
    * @throws Exception thrown if there is a serialization problem
    */
   private void writeElement(Object value, Element node, Label label) throws Exception {
      if(value != null) {
         String name = label.getName();
         Element next = root.getElement(name);
         Class type = value.getClass();
        
         if(type != label.getType()) {
            factory.setOverride(type, next);
         }
         label.getConverter(root).write(value, next);
         node.appendChild(next);
      }
   }
}

