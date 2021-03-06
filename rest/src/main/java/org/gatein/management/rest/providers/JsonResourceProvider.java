/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.management.rest.providers;

import org.gatein.management.rest.content.Child;
import org.gatein.management.rest.content.Link;
import org.gatein.management.rest.content.Operation;
import org.gatein.management.rest.content.Resource;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON Provider to control marshalling of a managed resource.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonResourceProvider implements MessageBodyWriter<Resource>
{
   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return (Resource.class.isAssignableFrom(type));
   }

   @Override
   public long getSize(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      PrintWriter printWriter = new PrintWriter(entityStream);
      try
      {
         JSONWriter writer  =  new JSONWriter(printWriter);
         writer.object().key("description").value(resource.getDescription());
         writer.key("children").array();
         for (Child child : resource.getChildren())
         {
            writeChild(child, writer);
         }
         writer.endArray();
         if (resource.getOperations() != null)
         {
            writer.key("operations").array();
            for (Operation operation : resource.getOperations())
            {
               writeOperation(operation, writer);
            }
            writer.endArray();
         }
         writer.endObject();

         printWriter.flush();
      }
      catch (JSONException e)
      {
         throw new IOException("Exception writing json result.", e);
      }
      finally
      {
         printWriter.close();
      }
   }

   private void writeOperation(Operation operation, JSONWriter writer) throws IOException, JSONException
   {
      writer.object().key("operation-name").value(operation.getOperationName());
      writer.key("operation-description").value(operation.getOperationDescription());
      writeLink("link", operation.getOperationLink(), writer);
      writer.endObject();
   }

   private void writeChild(Child child, JSONWriter writer) throws IOException, JSONException
   {
      writer.object().key("name").value(child.getName());
      writer.key("description").value(child.getDescription());
      writeLink("link", child.getLink(), writer);
      writer.endObject();
   }

   private void writeLink(String name, Link link, JSONWriter writer) throws IOException, JSONException
   {
      writer.key(name).object();
      if (link.getRel() != null)
      {
         writer.key("rel").value(link.getRel());
      }
      writer.key("href").value(link.getHref());
      
      if (link.getType() != null)
      {
         writer.key("type").value(link.getType());
      }
      if (link.getMethod() != null)
      {
         writer.key("method").value(link.getMethod());
      }
      writer.endObject();
   }
}
