package wwmm.crystaleye.fetch;

/**
 * @TODO Abstract Fetcher to an interface that each implementation uses. Then we
 *       can use Spring / something to inject the list of fetchers to call
 *       straight into this class or the CLI.
 * @TODO IoC would also get rid of properties path passing boilerplate
 */
public class FetchManager {

    String propertiesPath;

    public FetchManager() {
    }

    public FetchManager(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public void setPropertiesPath(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public void run() {
        ActaCurrent acta = new ActaCurrent(propertiesPath);
        acta.execute();
        
        AcsCurrent acs = new AcsCurrent(propertiesPath);
        acs.execute();

        RscCurrent rsc = new RscCurrent(propertiesPath);
        rsc.execute();

        ChemSocJapanCurrent japan = new ChemSocJapanCurrent(propertiesPath);
        japan.execute();
        
        ElsevierCurrent elsevier = new ElsevierCurrent(propertiesPath);
        elsevier.execute();
    }
}
