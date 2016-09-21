package com.elsevier.fingerprintengine;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Client class for one-off indexing with the fingerprint engine.
 * Thread safe
 *
 * Construct with the base url of the server as well as the login credentials.
 */
public class Client implements IClient {
	private static final String APPLICATION_XML = "application/xml";
	private static final String CATEGORIZE = "Categorize";

	// published workflows
	public static final String MESH_PLAINTEXT = "MeSH";
	public static final String MESH_XML = "MeSHXml";

	// makes connections for us
	private WebClientFactory factory;

	/**
	 * Creates a https connection to a TACO workflow
	 *
	 * @param url      - URL of the TACO instance - for example https://fingerprintengine.scivalcontent.com/TACO7600/TacoService.svc
	 * @param username username
	 * @param password password of the user
	 */
	public Client(String url, String username, String password) {
		factory = new WebClientFactory(url, username, password);
	}

	@Override
	public String categorizeTitleAndAbstract(String workflow, String title, String text) throws HttpException {
		String combined = combine(workflow, title, text);
		return categorize(combined);
	}

	@Override
	public void categorizeTitleAndAbstract(String workflow, String title, String text, DefaultHandler xmlHandler)
			throws HttpException, IOException, SAXException {
		String combined = combine(workflow, title, text);
		request(combined, xmlHandler);
	}

	@Override
	public String categorize(String data) throws HttpException {
		return executeCategorizePost(data).readEntity(String.class);
	}

	@Override
	public String executeWorkflow(String workflowId, String document) throws HttpException {
		return executePost(workflowId, document).readEntity(String.class);
	}

	private Response executeCategorizePost(String data) throws HttpException {
		return executePost(CATEGORIZE, data);
	}

	private Response executePost(String path, String data) throws HttpException {
		WebClient client = makeClient(path);
		Response response = client.post(data);

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new HttpStatusException(response.getStatus(), "End point returned status code " + response.getStatus());
		}
		return response;
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
	public void request(String data, DefaultHandler xmlHandler) throws IOException, SAXException, HttpException {
		Response response = executeCategorizePost(data);

		if (!response.hasEntity()) {
			throw new IOException("HTTPPost did not return data");
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
		return factory.getWebClient(APPLICATION_XML, path, APPLICATION_XML);
	}
}

