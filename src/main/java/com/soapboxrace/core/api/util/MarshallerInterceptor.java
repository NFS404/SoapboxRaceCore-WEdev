/*
 * This file is part of the Soapbox Race World (WorldUnited.GG) core source code.
 * Taken from https://github.com/SoapboxRaceWorld/soapbox-race-core/blob/develop/src/main/java/com/soapboxrace/core/api/util/MarshallerInterceptor.java
 */
package com.soapboxrace.core.api.util;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import com.soapboxrace.jaxb.util.JAXBUtility;

@Provider
@Produces(MediaType.APPLICATION_XML)
public class MarshallerInterceptor implements MessageBodyWriter<Object> {

	@Context
	protected Providers providers;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType
            , MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws WebApplicationException {
        if (object != null) {
            try {
                entityStream.write(JAXBUtility.marshal(object).getBytes());
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        }
    }

	@Override
	public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

}
