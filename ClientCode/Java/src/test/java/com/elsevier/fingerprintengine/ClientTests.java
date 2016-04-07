package com.elsevier.fingerprintengine;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClientTests {

	private static final String httpUrl = "http://localhost/Taco7600/TacoService.svc";
	private static final String httpsUrl = "https://fingerprintengine.scivalcontent.com/TACO7600/TacoService.svc";
	private static final String username = "";			// replace with your own
	private static final String password = "";			// replace with your own
	private static final String workflow = "MeSH";
	
	private static final String BEAR_FULLTEXT = "Knowledge of placentation in bears was limited to analysis of a single shed placenta of the European brown bear. It was shown that the mature allantoic placenta is discoidal, endotheliochorial, provided with a circular marginal hematoma, and that the allantoic sac is large and permanent. The present study, based on a small series of conceptuses younger than any previously examined, provides new details concerning implantation and the morphogenesis of the fetal membranes, placenta and paraplacental organs. Implantation in the black bear is central and superficial; the orientation of the embryonic disc is antimesometrial. Amniogenesis and chorion formation are almost certainly accomplished by folding. The decidual reaction is minimal. As in other carnivores the yolk sac is prominent early, but progressively declines. A well developed choriovitelline placenta is formed early, but is eliminated before the limb bud stage. The bilaminar omphalopleure persists only slightly beyond the limb bud stage. The allantois, however, is extensive and permanent. Initially, the allantoic placenta is cup shaped but eventually flattens out to form the discoidal placenta. It is lobuliform and endotheliochorial, and probably a marked attenuation of the interhemal membrane occurs in later stages. The marginal hematoma, which is completed before the limb bud stage, is formed by enlargement and coalescence of separate extravasations, and apparently remains fully functional to term. Overall, morphogenetic sequences and relationships in bears more nearly resemble those of the dog than of other carnivores. (35 references.)";
	
    private Client client = new Client(httpsUrl, username, password); 
	
	@Test
	public void testCategorizeOverHttps() throws Exception {
    	String response = client.categorizeTitleAndAbstract(
    			workflow,
        		"Morphogenesis of the fetal membranes and placenta of the black bear, Ursus americanus (Pallas)",
        		BEAR_FULLTEXT);

         System.out.println("----------------------------------------");
         System.out.println(response);   
	}
	
	@Test
	public void usingAWorkflowEndPointToAnalyzePlaintext() throws HttpException {
		String response = client.executeWorkflow(Client.MESH_PLAINTEXT, BEAR_FULLTEXT);
		System.out.println("----------------------------------------");
        System.out.println(response);
	}
	
	@Test @Ignore("Demonstrates that the solution can be used in a multi-threaded environment")
	public void multiThreadedTest() throws Exception {
		final int TOTAL = 100;
		final int PARALLEL = 10;
		
		long startTime = System.currentTimeMillis();
		
		ExecutorService executor = Executors.newFixedThreadPool(PARALLEL);
		for(int i=0; i<TOTAL; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						testCategorizeOverHttps();
					} catch (Exception e) {
						System.out.println("Exception: " + e.getMessage());
					}
				}
			});
		}
		
		executor.shutdown();
		assertTrue(executor.awaitTermination(10000, TimeUnit.SECONDS));
		
		System.out.println("Concurrently ran " + TOTAL + " requests with " + PARALLEL +
				" in parallel, taking " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	@Test @Ignore("Only for when there's a local server")
	public void testHttpLocalhost() throws ClientProtocolException, IOException, HttpException {
        Client client = new Client(httpUrl, username, password); 

    	String response = client.categorizeTitleAndAbstract(
    			workflow,
        		"Morphogenesis of the fetal membranes and placenta of the black bear, Ursus americanus (Pallas)",
        		"Knowledge of placentation in bears was limited to analysis of a single shed placenta of the European brown bear. It was shown that the mature allantoic placenta is discoidal, endotheliochorial, provided with a circular marginal hematoma, and that the allantoic sac is large and permanent. The present study, based on a small series of conceptuses younger than any previously examined, provides new details concerning implantation and the morphogenesis of the fetal membranes, placenta and paraplacental organs. Implantation in the black bear is central and superficial; the orientation of the embryonic disc is antimesometrial. Amniogenesis and chorion formation are almost certainly accomplished by folding. The decidual reaction is minimal. As in other carnivores the yolk sac is prominent early, but progressively declines. A well developed choriovitelline placenta is formed early, but is eliminated before the limb bud stage. The bilaminar omphalopleure persists only slightly beyond the limb bud stage. The allantois, however, is extensive and permanent. Initially, the allantoic placenta is cup shaped but eventually flattens out to form the discoidal placenta. It is lobuliform and endotheliochorial, and probably a marked attenuation of the interhemal membrane occurs in later stages. The marginal hematoma, which is completed before the limb bud stage, is formed by enlargement and coalescence of separate extravasations, and apparently remains fully functional to term. Overall, morphogenetic sequences and relationships in bears more nearly resemble those of the dog than of other carnivores. (35 references.)");

         System.out.println("----------------------------------------");
         System.out.println(response);   
	}	
}
