/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 30, 2008
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.web.context.ContextLoaderListener;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public abstract class AbstractSpringJerseyRestTestCase {

    static String GET = "GET";
    static String POST = "POST";
    static String DELETE = "DELETE";
    static String PUT = "PUT";
    
    String contextPath = "/opennms/rest";
    
    private ServletContainer dispatcher;
    private MockServletConfig servletConfig;
    private MockServletContext servletContext;
    private ContextLoaderListener contextListener;
    private Filter filter;

    @Before
    public void setUp() throws Throwable {
        beforeServletStart();

        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();

        MockDatabase db = new MockDatabase(true);
        DataSourceFactory.setInstance(db);
                
        servletContext = new MockServletContext("file:src/main/webapp");

        servletContext.addInitParameter("contextConfigLocation", 
                "classpath:/org/opennms/web/rest/applicationContext-test.xml " +
                "classpath*:/META-INF/opennms/component-service.xml " +
                "classpath*:/META-INF/opennms/component-dao.xml " +
                "classpath:/META-INF/opennms/applicationContext-reportingCore.xml " +
                "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml " +
                "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml " +
                "classpath:/org/opennms/web/rest/applicationContext-mockEventProxy.xml " +
                "classpath:/META-INF/opennms/applicationContext-reporting.xml " +
                "/WEB-INF/applicationContext-spring-security.xml " +
                "/WEB-INF/applicationContext-jersey.xml");
        
        servletContext.addInitParameter("parentContextKey", "daoContext");
                
        ServletContextEvent e = new ServletContextEvent(servletContext);
        contextListener = new ContextLoaderListener();
        contextListener.contextInitialized(e);

        servletContext.setContextPath(contextPath);
        servletConfig = new MockServletConfig(servletContext, "dispatcher");    
        servletConfig.addInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        servletConfig.addInitParameter("com.sun.jersey.config.property.packages", "org.opennms.web.rest");
        
        try {

            MockFilterConfig filterConfig = new MockFilterConfig(servletContext, "openSessionInViewFilter");
            filter = new OpenSessionInViewFilter();        
            filter.init(filterConfig);

            dispatcher = new SpringServlet();
            dispatcher.init(servletConfig);

        } catch (ServletException se) {
            throw se.getRootCause();
        }
        
        afterServletStart();
        System.err.println("------------------------------------------------------------------------------");
    }

    protected MockServletContext getServletContext() {
    	return servletContext;
    }

    protected void beforeServletStart() throws Exception {
    }
    
    protected void afterServletStart() throws Exception {
        
    }

    @After
    public void tearDown() throws Exception {
        System.err.println("------------------------------------------------------------------------------");
        beforeServletDestroy();
        contextListener.contextDestroyed(new ServletContextEvent(servletContext));
        if (dispatcher != null) {
            dispatcher.destroy();
        }
        afterServletDestroy();
    }

    protected void beforeServletDestroy() throws Exception {
    }
    
    protected void afterServletDestroy() throws Exception {
        
    }

    protected void dispatch(final MockHttpServletRequest request, final MockHttpServletResponse response) throws Exception {
        final FilterChain filterChain = new FilterChain() {
            public void doFilter(final ServletRequest filterRequest, final ServletResponse filterResponse) throws IOException, ServletException {
                dispatcher.service(filterRequest, filterResponse);
            }
        };
        filter.doFilter(request, response, filterChain);
    }
    
    protected MockHttpServletResponse createResponse() {
        return new MockHttpServletResponse();
    }

    protected MockHttpServletRequest createRequest(final String requestType, final String urlPath) {
    	final MockHttpServletRequest request = new MockHttpServletRequest(servletContext, requestType, contextPath + urlPath) {

            @Override
            public void setContentType(final String contentType) {
                super.setContentType(contentType);
                super.addHeader("Content-Type", contentType);
            }
            
        };
        request.setContextPath(contextPath);
        return request;
    }

    protected void sendPost(String url, String xml) throws Exception {
        sendData(POST, MediaType.APPLICATION_XML, url, xml);
    }

    protected void sendPost(String url, String xml, int statusCode) throws Exception {
        sendData(POST, MediaType.APPLICATION_XML, url, xml, statusCode);
    }

    protected void sendPut(String url, String formData) throws Exception {
        sendData(PUT, MediaType.APPLICATION_FORM_URLENCODED, url, formData);
    }
    
    protected void sendPut(String url, String formData, int statusCode) throws Exception {
        sendData(PUT, MediaType.APPLICATION_FORM_URLENCODED, url, formData, statusCode);
    }
    
    protected void sendData(String requestType, String contentType, String url, String data) throws Exception {
    	sendData(requestType, contentType, url, data, 200);
    }
    
    protected void sendData(String requestType, String contentType, String url, String data, int statusCode) throws Exception {
        MockHttpServletRequest request = createRequest(requestType, url);
        request.setContentType(contentType);

        if(contentType.equals(MediaType.APPLICATION_FORM_URLENCODED)){
            request.setParameters(parseParamData(data));
            request.setContent(data.getBytes());
        }else{
            request.setContent(data.getBytes());
        }

        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);

        LogUtils.debugf(this, "Received response: %s", stringifyResponse(response));
        assertEquals(response.getErrorMessage(), statusCode, response.getStatus());
    }

    private String stringifyResponse(final MockHttpServletResponse response) {
    	final StringBuilder string = new StringBuilder();
    	try {
			string.append("HttpServletResponse[")
				.append("status=").append(response.getStatus())
				.append(",content=").append(response.getContentAsString())
				.append(",headers=[");
			boolean first = true;
			for (final Iterator<String> i = response.getHeaderNames().iterator(); i.hasNext(); first = false) {
				if (!first) {
					string.append(",");
				}
				final String name = i.next();
				string.append("name=").append(response.getHeader(name));
			}
			string.append("]").append("]");
		} catch (UnsupportedEncodingException e) {
			LogUtils.warnf(this, e, "Unable to get response content");
		}
    	return string.toString();
	}

	protected static Map<String, String> parseParamData(String data) throws UnsupportedEncodingException {
        Map<String, String> retVal = new HashMap<String, String>();
        for (String item : data.split("&")) {
            String[] kv = item.split("=");
            if(kv.length > 1){
                retVal.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1],"UTF-8"));
            }
        }
        return retVal;
    }

    protected String sendRequest(String requestType, String url, @SuppressWarnings("rawtypes") Map parameters, int expectedStatus) throws Exception {
    	final MockHttpServletRequest request = createRequest(requestType, url);
        request.setParameters(parameters);
        request.setQueryString(getQueryString(parameters));
        return sendRequest(request, expectedStatus);
    }
    
    protected String getQueryString(@SuppressWarnings("rawtypes") final Map parameters) {
    	final StringBuffer sb = new StringBuffer();

		try {
	    	for (final Object key : parameters.keySet()) {
	    		if (key instanceof String) {
	    			final Object value = parameters.get(key);
	    			String[] valueEntries = null;
	    			if (value instanceof String[]) {
	    				valueEntries = (String[])value;
	    			} else if (value instanceof String) {
	    				valueEntries = new String[] { (String)value };
	    			} else {
	    				LogUtils.warnf(this, "value was not a string or string array! (%s)", value);
	    				continue;
	    			}

	    			for (final String valueEntry : valueEntries) {
	    				sb.append(URLEncoder.encode((String)key, "UTF-8")).append("=").append(URLEncoder.encode((String)valueEntry, "UTF-8")).append("&");
	    			}
	    		} else {
	    			LogUtils.warnf(this, "key was not a string! (%s)", key);
	    		}
	    	}
		} catch (final UnsupportedEncodingException e) {
			LogUtils.warnf(this, e, "unsupported encoding UTF-8?!?  WTF??!");
		}
    	
    	return sb.toString();
    }

    protected String sendRequest(String requestType, String url, int expectedStatus) throws Exception {
    	final MockHttpServletRequest request = createRequest(requestType, url);
        return sendRequest(request, expectedStatus);
    }

    protected String sendRequest(MockHttpServletRequest request, int spectedStatus) throws Exception, UnsupportedEncodingException {
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        String xml = response.getContentAsString();
        if (xml != null) {
            System.err.println(xml);
        }
        assertEquals(spectedStatus, response.getStatus());
        return xml;
    }
    
    protected <T> T getXmlObject(JAXBContext context, String url, int expectedStatus, Class<T> expectedClass) throws Exception {
        MockHttpServletRequest request = createRequest(GET, url);
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(expectedStatus, response.getStatus());
        
        System.err.printf("xml: %s\n", response.getContentAsString());
        
        InputStream in = new ByteArrayInputStream(response.getContentAsByteArray());
        
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        T result = expectedClass.cast(unmarshaller.unmarshal(in));
        
        return result;

    }
    
    protected void putXmlObject(JAXBContext context, String url, int expectedStatus, Object object) throws Exception {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(object, out);
        byte[] content = out.toByteArray();
        

        MockHttpServletRequest request = createRequest(PUT, url);
        request.setContentType(MediaType.APPLICATION_XML);
        request.setContent(content);
        MockHttpServletResponse response = createResponse();        
        dispatch(request, response);
        assertEquals(expectedStatus, response.getStatus());
        
    }

	protected void createNode() throws Exception {
	    String node = "<node label=\"TestMachine\">" +            
	    "<labelSource>H</labelSource>" +
	    "<sysContact>The Owner</sysContact>" +
	    "<sysDescription>" +
	    "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
	    "</sysDescription>" +
	    "<sysLocation>DevJam</sysLocation>" +
	    "<sysName>TestMachine</sysName>" +
	    "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
	    "<type>A</type>" +
	    "</node>";
	    sendPost("/nodes", node);
	}

	protected void createIpInterface() throws Exception {
	    createNode();
	    String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
	    "<ipAddress>10.10.10.10</ipAddress>" +
	    "<hostName>TestMachine</hostName>" +
	    "<ipStatus>1</ipStatus>" +
	    "</ipInterface>";
	    sendPost("/nodes/1/ipinterfaces", ipInterface);
	}

	protected void createSnmpInterface() throws Exception {
	    createIpInterface();
	    String snmpInterface = "<snmpInterface ifIndex=\"6\">" +
	    "<ifAdminStatus>1</ifAdminStatus>" +
	    "<ifDescr>en1</ifDescr>" +
	    "<ifName>en1</ifName>" +
	    "<ifOperStatus>1</ifOperStatus>" +
	    "<ifSpeed>10000000</ifSpeed>" +
	    "<ifType>6</ifType>" +
	    "<ipAddress>10.10.10.10</ipAddress>" +
	    "<netMask>255.255.255.0</netMask>" +
	    "<physAddr>001e5271136d</physAddr>" +
	    "</snmpInterface>";
	    sendPost("/nodes/1/snmpinterfaces", snmpInterface);
	}

	protected void createService() throws Exception {
	    createIpInterface();
	    String service = "<service>" +
	    "<notify>Y</notify>" +
	    "<serviceType>" +
	    "<name>ICMP</name>" +
	    "</serviceType>" +
	    "<source>P</source>" +
	    "<status>N</status>" +
	    "</service>";
	    sendPost("/nodes/1/ipinterfaces/10.10.10.10/services", service);
	}

	protected void createCategory() throws Exception {
	    createNode();
	    String service = "<category name=\"Routers\">" +
	        "<description>Core Routers</description>" +
	        "</category>";
	    sendPost("/nodes/1/categories", service);
	}


}