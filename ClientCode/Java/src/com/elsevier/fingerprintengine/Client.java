package com.elsevier.fingerprintengine;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * General client class for one-off indexing with the fingerprint engine.
 * 
 * @author DoornenbalM
 */
public class Client {
    private final CloseableHttpClient client;
    private final HttpPost httpPost;
    
    private static final ResponseHandler<String> handler = new ResponseHandler<String>() {
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            } else {
                return null;
            }
        }
    };

    /**
     * Creates a connection to a TACO workflow
     * 
     * @param url - URL of the TACO instance
     * @param workflow - name of the workflow
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public Client(String url) throws ClientProtocolException, IOException {        
        client = HttpClients.createDefault();        
        httpPost = new HttpPost(url + "/Categorize");        
    }
    
    /**
     * Creates a https connection to a TACO workflow
     * 
     * @param url - URL of the TACO instance
     * @param username - name of the username
  	 * @param password - password of the user
     * @param workflow - name of the workflow
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public Client(String url, String username, String password) throws ClientProtocolException, IOException {
		String authString = username + ":" + password;   
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());   
		String authStringEnc = new String(authEncBytes);
        
        client = HttpClients.createDefault();        
        httpPost = new HttpPost(url + "/Categorize");        
        httpPost.setHeader("Authorization", "Basic " + authStringEnc);
    }

    /**
     * Sends a request to the TACO workflow, returning the returned XML in a string.
     * 
     * @param title
     * @param text
     * @return returned XML in a string
     * @throws HttpException
     * @throws IOException
     */
    public String sendTitleAndAbstract(String workflow, String title, String text) throws HttpException, IOException {
        String combined = combine(workflow, title, text);
        String result = request(combined);
        return result;
    }

    /**
     * Sends a request to the TACO workflow, sending the XML result to a SAX handler.
     * 
     * @param title
     * @param text
     * @param xmlHandler
     * @throws HttpException
     * @throws IOException
     * @throws SAXException
     */
    public void sendTitleAndAbstract(String workflow, String title, String text, DefaultHandler xmlHandler) throws HttpException,
            IOException, SAXException {
        String combined = combine(workflow, title, text);
        request(combined, xmlHandler);
    }

    /**
     * Sends a request to TACO, returning the returned XML in a string.
     * 
     * @param data
     * @return returned XML in a string.
     * @throws IOException
     */
    public String request(String data) throws IOException {
        StringEntity entity = new StringEntity(data, "UTF-8");
        httpPost.setEntity(entity);
        String result = client.execute(httpPost, handler);
        return result;
    }

    /**
     * Sends a request to TACO, sending the XML result to a SAX handler.
     * 
     * @param data
     * @param xmlHandler
     * @throws HttpException
     * @throws IOException
     * @throws SAXException
     */
    public void request(String data, DefaultHandler xmlHandler) throws IOException, SAXException {
        httpPost.setEntity(new StringEntity(data));
        try {
            HttpResponse response = client.execute(httpPost);
            // System.out.println("HTTP response: " + responseCode);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity == null) {
                throw new IOException("HTTPPost did not return data");
            }
            try {
                InputStream in = responseEntity.getContent();
                try {
                    XMLReader xr = XMLReaderFactory.createXMLReader();
                    xr.setContentHandler(xmlHandler);
                    xr.setDTDHandler(xmlHandler);
                    xr.setEntityResolver(xmlHandler);
                    xr.setErrorHandler(xmlHandler);
                    xr.parse(new InputSource(in));
                } finally {
                    in.close();
                }
            } finally {
                EntityUtils.consume(responseEntity);
            }
        } catch (ClientProtocolException e) {
            httpPost.abort();
            throw e;
        } catch (IOException e) {
            httpPost.abort();
            throw e;
        }
    }

    /**
     * Combines title and text into a XML document
     * 
     * @param title
     * @param text
     * @return
     */
    private static String combine(String workflow, String title, String text) {
        Document doc = new Document();
        Element root = new Element("Xml");
        Element workflowNode = new Element("Workflow");
        Element textNode = new Element("Text");
        Element titleNode = new Element("Title");
        Element absNode = new Element("Abstract");
        
        workflowNode.setText(workflow);
        titleNode.setText(title);
        absNode.setText(text);
        
        root.addContent(workflowNode);
        textNode.addContent(titleNode);
        textNode.addContent(absNode);
        root.addContent(textNode);
        doc.setContent(root);
        
        XMLOutputter outputter = new XMLOutputter();
        String result = outputter.outputString(doc).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n", "");
        result = result.replaceAll("\r\n", "");
        return result;
    }
}

