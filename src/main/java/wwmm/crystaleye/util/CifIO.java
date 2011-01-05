package wwmm.crystaleye.util;

import nu.xom.Document;
import org.apache.commons.io.IOUtils;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;

import java.io.*;

/**
 * @author Sam Adams
 */
public class CifIO {

    public static CIF readCif(File file, String encoding) throws CIFException, IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(fis, encoding));
            return readCif(in);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    public static CIF readCif(Reader in) throws IOException, CIFException {
        CIFParser parser = createCifParser();
        try {
            Document doc = parser.parse(in);
            return (CIF) doc.getRootElement();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private static CIFParser createCifParser() {
        CIFParser parser = new CIFParser();
        parser.setSkipHeader(true);
        parser.setSkipErrors(true);
        parser.setCheckDuplicates(true);
        parser.setBlockIdsAsIntegers(false);
        return parser;
    }

    public static void writeCif(CIF cif, File file, String encoding) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos, encoding));
            try {
                cif.writeCIF(out);
            } finally {
                IOUtils.closeQuietly(out);
            }
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

}
