package wwmm.crystaleye;

import static junit.framework.Assert.*;

import org.junit.Test;

public class BasicHttpClientTest {
	
	@Test
	public void testClientInitOnConstruction() {
		BasicHttpClient bhc = new BasicHttpClient();
		Assert.assertNotNull(bhc.client);
	}

}
