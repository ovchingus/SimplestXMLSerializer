/*
 * Root.java July 2006
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

package xml.serializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This <code>Root</code> annotation is used to annotate classes that
 * need to be serialized. Also, elements within an element list, as
 * represented by the <code>ElementList</code> annotation need this
 * annotation so that the element names can be determined. All other
 * field names can be determined using the field annotation and so
 * the <code>Root</code> annotation is not needed for those. 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Root {
   
   /**
    * This represents the name of the XML element. This must be 
    * used for objects within element lists and root elements
    * so that they can be serialized with a known name. If a
    * root object does not contain this annotation it cannot be
    * serialized as the root element name cannot be determined.
    * 
    * @return the name of the XML element this represents
    */
   public String name();
}
