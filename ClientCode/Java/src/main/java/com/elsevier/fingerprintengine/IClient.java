package com.elsevier.fingerprintengine;

import java.io.IOException;

import org.apache.http.HttpException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Interface to the client to allow for users to unit test against the interface
 */
public interface IClient {
    /**
     * Sends a request to the TACO workflow, returning the returned XML in a string.
     * 
     * @param title
     * @param text
     * @return returned XML in a string
     * @throws HttpException
     * @throws IOException
     */
    String categorizeTitleAndAbstract(String workflow, String title, String text) throws HttpException;

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
    void categorizeTitleAndAbstract(String workflow, String title, String text, DefaultHandler xmlHandler) throws HttpException,
            IOException, SAXException;

    /**
     * Sends a categorization request to TACO, returning the returned XML in a string.
     * 
     * @param data
     * @return returned XML in a string.
     * @throws HttpException 
     */
    String categorize(String data) throws HttpException;
    
    /**
     * Execute the workflow on the server with the given ID, posting it the given document for analysis
     * @param workflowId workflow to run
     * @param document text of the document
     * @return response from the server, or exception if the call was unsuccessful
     * @throws HttpException on error
     */
	String executeWorkflow(String workflowId, String document) throws HttpException;
}
