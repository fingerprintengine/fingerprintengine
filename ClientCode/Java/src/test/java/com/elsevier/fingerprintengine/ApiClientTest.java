package com.elsevier.fingerprintengine;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;

import junit.framework.Test;
import junit.framework.TestCase;

public class ApiClientTest
    extends TestCase
{
    private static final String url = "https://fingerprintengine.scivalcontent.com/taco7800/TacoService.svc/";
    private static final String username = "geertzenj@scivalcontent.local";
    private static final String password = "7%HOmbCdb}lH1";

    /**
     * Testing Indexing with MeSH
     */
    public void test_indexing_MeSH() 
        throws ClientProtocolException, IOException, HttpException
    {
        Client c = new Client(url, username, password);
        String t = "Photosynthesis is a process used by plants and other organisms to convert light energy into chemical energy that can later be released to fuel the organisms' activities (energy transformation).";
        String r = c.analyze("MeSH", t);
        System.out.println("-[MeSH indexing]----------------------------------------------");
        System.out.println(r);
        System.out.println("--------------------------------------------------------------");
        assertTrue( r.toLowerCase().contains("<annotations>") );
    }

    /**
     * Testing Domain Classification
     */
    public void test_classification_domain() 
        throws ClientProtocolException, IOException, HttpException
    {
        Client c = new Client(url, username, password);
        String t = "Photosynthesis is a process used by plants and other organisms to convert light energy into chemical energy that can later be released to fuel the organisms' activities (energy transformation).";
        String r = c.analyze("DomainClassifier", t);
        System.out.println("-[Domain classification]--------------------------------------");
        System.out.println(r);
        System.out.println("--------------------------------------------------------------");
        assertTrue( r.toLowerCase().contains("<annotations>") );
    }

}
