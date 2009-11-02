package wwmm.crystaleye.fetch;

import java.io.File;

/**
 * @TODO Abstract Fetcher to an interface that each implementation uses. Then we
 *       can use Spring / something to inject the list of fetchers to call
 *       straight into this class or the CLI.
 * @TODO IoC would also get rid of properties path passing boilerplate
 */
public class FetchManager {

    private File propsFile;

    private FetchManager() {
    	;
    }

    public FetchManager(File propsFile) {
        this.propsFile = propsFile;
    }

    public void run() {
        ActaCurrent acta = new ActaCurrent(propsFile);
        acta.execute();
        
        AcsCurrent acs = new AcsCurrent(propsFile);
        acs.execute();

        RscCurrent rsc = new RscCurrent(propsFile);
        rsc.execute();

        ChemSocJapanCurrent japan = new ChemSocJapanCurrent(propsFile);
        japan.execute();
        
        ElsevierCurrent elsevier = new ElsevierCurrent(propsFile);
        elsevier.execute();
    }
}
