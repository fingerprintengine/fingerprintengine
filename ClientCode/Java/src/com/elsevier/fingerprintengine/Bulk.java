package com.elsevier.fingerprintengine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 * Bulk indexing client
 * @author DoornenbalM
 */
public class Bulk {
    private final URL endpoint;
    private final String username;
    private final String password;
    private final String rootMarker;
    private final String recordMarker;
    private final String idMarker;
    private final boolean formatAsXml;
    
    /**
     * Creates a connection to a TACO workflow
     * 
     * @param baseURL
     *            URL of the TACO instance
     * @param workflow
     *            name of the workflow
     * @param rootMarker
     * @param recordMarker
     * @param idMarker
     * @param formatAsXml
     * @throws MalformedURLException 
     */
    public Bulk(String baseURL,
                String username,
                String password,
                String workflow, 
                String rootMarker, 
                String recordMarker, 
                String idMarker, 
                boolean formatAsXml) throws MalformedURLException 
    {    
        if (!baseURL.endsWith("/")){
            baseURL += "/";
        }
        
        String address = baseURL + "Bulk.svc" + "/" + workflow + "/" + recordMarker + "/" + idMarker;
        endpoint = new URL(address);
        
        this.username = username;
        this.password = password;
        this.rootMarker = rootMarker;
        this.recordMarker = recordMarker;
        this.idMarker = idMarker;
        this.formatAsXml = formatAsXml;
    }
    
    /**
     * Main method.
     * Ex arguments: 
     * -h https://fingerprintengine.ptgels.com/TACO7500
     * -u user
     * -p password
     * -f txt
     * -o out
     * -x
     * Send a directory content to some TACO client.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        CommandLineParser clp = new BasicParser();
        Options options = new Options();
        options.addOption("r", "rootmarker", true, "marker for the root");
        options.addOption("i", "idmarker", true, "marker for the id");
        options.addOption("w", "workflow", true, "workflow");
        options.addOption("x", "xml", false, "format each document as xml [plain text by default]");
        options.addOption("d", "documentmarker", true, "marker for the document");
        options.addOption("h", "host", true, "host URL (without .svc path)");
        options.addOption("u", "username", true, "the user name");
        options.addOption("p", "password", true, "the password");
        options.addOption("f", "inputfolder", true, "input folder");
        options.addOption("o", "outputfolder", true, "output folder");
        CommandLine cl = clp.parse(options, args);
        Bulk bulk = new Bulk(
                    cl.getOptionValue("h", null),
                    cl.getOptionValue("u", null),
                    cl.getOptionValue("p", null),
                    cl.getOptionValue("w", "MeSHXml"), 
                    cl.getOptionValue("r", "Documents"), 
                    cl.getOptionValue("d", "Document"), 
                    cl.getOptionValue("i", "ID"),
                    cl.hasOption("x"));
        
        String bulkRequest = bulk.directoryToBulk(cl.getOptionValue("f", null));
        String result = bulk.postData(bulkRequest);
       // InputStream result = bulk.execute(bulkRequest);

        Document document = new SAXBuilder().build(new StringReader(result));
        bulk.storeBulkResponse(document, cl.getOptionValue("o", null));
    }
    
    /**
    * Reads data from the data reader and posts it to a server via POST request.
    * data - The data you want to send
    * @param bulkRequest
    * @return - writes the server's response to output
     * @throws IOException 
    */
    public String postData(String bulkRequest) throws IOException
    {
        HttpURLConnection urlc = null;
        try
        {
            urlc = (HttpURLConnection) endpoint.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");
             
			String authString = username + ":" + password;   
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());   
			String authStringEnc = new String(authEncBytes);
			urlc.setRequestProperty("Authorization", "Basic "     + authStringEnc);
			
            OutputStream out = urlc.getOutputStream();
             
            try
            {
                Writer writer = new OutputStreamWriter(out, "UTF-8");
                pipe(new StringReader(bulkRequest), writer);
                writer.close();
            } catch (IOException e)
            {
                throw new IOException("IOException while posting data", e);
            } finally
            {
                if (out != null)
                    out.close();
            }
             
            InputStream in = urlc.getInputStream();
            Writer output = new StringWriter();
            try
            {
                Reader reader = new InputStreamReader(in);
                pipe(reader, output);
                reader.close();
            } catch (IOException e)
            {
                throw new IOException("IOException while reading response", e);
            } finally
            {
                if (in != null)
                    in.close();
            }
     

            output.flush();
            return output.toString();
        } catch (IOException e)
        {
            throw new IOException("Connection error (is server running at " + endpoint + " ?): " + e);
        } finally
        {
            if (urlc != null)
                urlc.disconnect();
        }
    }     
    
    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private static void pipe(Reader reader, Writer writer) throws IOException
    {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0)
        {
            writer.write(buf, 0, read);
        }
        writer.flush();
    }

    /**
     * Store the response - parse the xml and store in separate files.
     * 
     * @param result
     * @param outputFolder
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws JDOMException 
     */
    private void storeBulkResponse(Document document, String outputFolder) 
            throws SAXException, IOException, ParserConfigurationException, JDOMException {
            
        XMLOutputter outputter = new XMLOutputter();
        
        File theDir = new File(outputFolder);

        // if the directory does not exist, create it
        if (!theDir.exists())
        {
            System.out.println("creating directory: " + outputFolder);
            boolean createResult = theDir.mkdir();  
            if (createResult) {    
                System.out.println("DIR created");  
            }
        }
        
        for (Element node : document.getDescendants(new ElementFilter(recordMarker))) {
                        
            // get the ID
            Attribute attr = node.getAttribute(idMarker);
            String id = null;
            if (attr != null) {
                id = attr.getValue();
            }
            else {
                id = node.getChildText(idMarker);
            }
            
            // get the document content
            String content = outputter.outputString(node);
            
            File output = new File(theDir, id + ".xml");

            BufferedWriter writer = new BufferedWriter(new FileWriter(output, false));
            writer.write(content);
            writer.close();
        }        
    }

    /**
     * Process all files in a folder.
     * @param directory
     * search path 
     * @param rootMarker
     * @param idMarker
     * @param recordMarker
     * @throws IOException 
     */
    public String directoryToBulk(String directory) throws IOException {
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles(); 

        Element root = new Element(rootMarker);
        
        for (int i = 0; i < listOfFiles.length; i++) 
        {
            if (listOfFiles[i].isFile()) 
            {
                Element documentNode = new Element(recordMarker);
                
                // get the id and text
                String id = FilenameUtils.removeExtension(listOfFiles[i].getName());
                String text = FileUtils.readFileToString(listOfFiles[i], "utf-8");
                
                if (formatAsXml) {
                    // content and id as separate nodes.
                    Element idNode = new Element(idMarker);
                    idNode.setText(id);
                    
                    // set the content
                    Element contentNode = new Element("text");
                    contentNode.setText(text);
                    
                    documentNode.addContent(idNode);
                    documentNode.addContent(contentNode);
                }
                else {
                    documentNode.setAttribute(idMarker, id);
                    documentNode.setText(text);
                }
                
                root.addContent(documentNode);
            }
        }

        Document doc = new Document();
        doc.setContent(root);
        XMLOutputter outputter = new XMLOutputter();
        String result = outputter.outputString(doc).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n", "");
        result = result.replaceAll("\r\n", "");
        return result;        
    }
}
