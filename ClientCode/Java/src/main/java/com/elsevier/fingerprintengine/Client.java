package com.elsevier.fingerprintengine;

import static javax.ws.rs.core.Response.Status.OK;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Client class for indexing and classification with the fingerprint engine.
 *
 * Construct with the base url of the server as well as the login credentials.
 */
public class Client {
    // mime type output
    private static final String MIME_XML = "application/xml";

    // web client
    private WebClientFactory factory;

    // default timeout
    private int receiveTimeoutInSeconds = 20;

    /**
     * Creates a https connection to a TACO workflow
     *
     * @param url URL of the TACO instance
     * @param username username
     * @param password password of the user
     * @param receiveTimeoutInSeconds receive timeout in seconds
     */
    public Client(String url, String username, String password) {
        this.factory = new WebClientFactory(url, username, password, this.receiveTimeoutInSeconds);
    }

    public Client(String url, String username, String password, int receiveTimeoutInSeconds) {
        this.factory = new WebClientFactory(url, username, password, receiveTimeoutInSeconds);
    }

    public String analyzeTitleAndAbstract(String workflow, String title, String text) throws HttpException {
        String combined = combine(workflow, title, text);
        System.out.println(combined);
        return analyze(workflow, combined);
    }

    public void analyzeTitleAndAbstract(String workflow, String title, String text, DefaultHandler xmlHandler)
            throws HttpException, IOException, SAXException {
        String combined = combine(workflow, title, text);
        request(workflow, combined, xmlHandler);
    }

    public String analyze(String workflow, String text) throws HttpException {
        return executeAnalyzePost(workflow, text).readEntity(String.class);
    }

    public String executeWorkflow(String workflowId, String document) throws HttpException {
        return executePost(workflowId, document).readEntity(String.class);
    }

    /**
     * {@inheritDoc}
     */
    public String getResource(String path) throws HttpException {
        return executeGet(path).readEntity(String.class);
    }

    private Response executeGet(String path) throws HttpException {
        WebClient client = makeClient(path);
        Response response = client.get();
        raiseExceptionWhenNotOkResponse(response);
        return response;
    }

    private Response executeAnalyzePost(String workflow, String data) throws HttpException {
        return executePost(workflow, data);
    }

    private Response executePost(String path, String data) throws HttpException {
        WebClient client = makeClient(path);
        Response response = client.post(data);
        raiseExceptionWhenNotOkResponse(response);
        return response;
    }

    private void raiseExceptionWhenNotOkResponse(Response response) throws HttpStatusException {
        if (response.getStatus() != OK.getStatusCode()) {
            throw new HttpStatusException(response.getStatus(), response.readEntity(String.class));
        }
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
    public void request(String workflow, String data, DefaultHandler xmlHandler) throws IOException, SAXException, HttpException {
        Response response = executeAnalyzePost(workflow, data);

        if (!response.hasEntity()) {
            throw new IOException("HTTP Post did not return data");
        }
        try (InputStream in = response.readEntity(InputStream.class)) {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(xmlHandler);
            xr.setDTDHandler(xmlHandler);
            xr.setEntityResolver(xmlHandler);
            xr.setErrorHandler(xmlHandler);
            xr.parse(new InputSource(in));
        }
    }

    /**
     * Combines title and text into a XML document so the Fingerprint Engine can classify it correctly
     *
     * @param title    title of the document
     * @param text     text of the document
     * @param workflow process to use for enrichment
     * @return XML document to use as request to fingerprint engine
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

    private WebClient makeClient(String path) {
        return factory.getWebClient(MIME_XML, path, MIME_XML);
    }
}