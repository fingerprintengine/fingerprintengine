package com.elsevier.fingerprintengine;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class ClientTests {
	private final String httpUrl = "http://localhost/taco7800/TacoService.svc";
	private final String httpsUrl = "https://fingerprintengine.scivalcontent.com/taco7800/TacoService.svc/";
	private final String username = "user";
	private final String password = "password";
	private final String workflow = "MeSHXml";
	
	@Test
	public void testHttps() throws ClientProtocolException, IOException, HttpException {
        Client client = new Client(httpsUrl, username, password); 

    	String response = client.sendTitleAndAbstract(
    			workflow,
        		"Morphogenesis of the fetal membranes and placenta of the black bear, Ursus americanus (Pallas)",
        		"Knowledge of placentation in bears was limited to analysis of a single shed placenta of the European brown bear. It was shown that the mature allantoic placenta is discoidal, endotheliochorial, provided with a circular marginal hematoma, and that the allantoic sac is large and permanent. The present study, based on a small series of conceptuses younger than any previously examined, provides new details concerning implantation and the morphogenesis of the fetal membranes, placenta and paraplacental organs. Implantation in the black bear is central and superficial; the orientation of the embryonic disc is antimesometrial. Amniogenesis and chorion formation are almost certainly accomplished by folding. The decidual reaction is minimal. As in other carnivores the yolk sac is prominent early, but progressively declines. A well developed choriovitelline placenta is formed early, but is eliminated before the limb bud stage. The bilaminar omphalopleure persists only slightly beyond the limb bud stage. The allantois, however, is extensive and permanent. Initially, the allantoic placenta is cup shaped but eventually flattens out to form the discoidal placenta. It is lobuliform and endotheliochorial, and probably a marked attenuation of the interhemal membrane occurs in later stages. The marginal hematoma, which is completed before the limb bud stage, is formed by enlargement and coalescence of separate extravasations, and apparently remains fully functional to term. Overall, morphogenetic sequences and relationships in bears more nearly resemble those of the dog than of other carnivores. (35 references.)");

         System.out.println("----------------------------------------");
         System.out.println(response);   
	}
	
	@Test
	public void testHttp() throws ClientProtocolException, IOException, HttpException {
        Client client = new Client(httpUrl); 

    	String response = client.sendTitleAndAbstract(
    			workflow,
        		"Morphogenesis of the fetal membranes and placenta of the black bear, Ursus americanus (Pallas)",
        		"Knowledge of placentation in bears was limited to analysis of a single shed placenta of the European brown bear. It was shown that the mature allantoic placenta is discoidal, endotheliochorial, provided with a circular marginal hematoma, and that the allantoic sac is large and permanent. The present study, based on a small series of conceptuses younger than any previously examined, provides new details concerning implantation and the morphogenesis of the fetal membranes, placenta and paraplacental organs. Implantation in the black bear is central and superficial; the orientation of the embryonic disc is antimesometrial. Amniogenesis and chorion formation are almost certainly accomplished by folding. The decidual reaction is minimal. As in other carnivores the yolk sac is prominent early, but progressively declines. A well developed choriovitelline placenta is formed early, but is eliminated before the limb bud stage. The bilaminar omphalopleure persists only slightly beyond the limb bud stage. The allantois, however, is extensive and permanent. Initially, the allantoic placenta is cup shaped but eventually flattens out to form the discoidal placenta. It is lobuliform and endotheliochorial, and probably a marked attenuation of the interhemal membrane occurs in later stages. The marginal hematoma, which is completed before the limb bud stage, is formed by enlargement and coalescence of separate extravasations, and apparently remains fully functional to term. Overall, morphogenetic sequences and relationships in bears more nearly resemble those of the dog than of other carnivores. (35 references.)");

         System.out.println("----------------------------------------");
         System.out.println(response);   
	}	
}
