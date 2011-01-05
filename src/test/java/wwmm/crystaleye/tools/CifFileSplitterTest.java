package wwmm.crystaleye.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFException;
import wwmm.crystaleye.util.CifIO;

import java.io.*;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Sam Adams
 */
public class CifFileSplitterTest {

    private static File createTmpDir() throws IOException {
        File dir;
        do {
            dir = new File("target/tmp/"+ UUID.randomUUID().toString());
            if (dir.exists()) {
                continue;
            }
            FileUtils.forceMkdir(dir);
        } while (!dir.isDirectory());
        return dir;
    }

    private static void delete(File tmpdir) {
        try {
            FileUtils.deleteDirectory(tmpdir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyCif(String name, File file) throws IOException {
        InputStream in = getClass().getResourceAsStream(name);
        if (in == null) {
            throw new FileNotFoundException("File not found: "+name);
        }
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            try {
                IOUtils.copy(in, out);
            } finally {
                IOUtils.closeQuietly(out);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


    @Test
    public void testSplitCifFile() throws IOException, CIFException {
        File tmpdir = createTmpDir();
        try {
            File cifFile = new File(tmpdir, "C0CC00881H.TXT");
            copyCif("./C0CC00881H.TXT", cifFile);

            List<File> split = new CifFileSplitter().split(cifFile);
            assertEquals(3, split.size());
            assertEquals("C0CC00881H_3a.cif", split.get(0).getName());
            assertEquals("C0CC00881H_2a.cif", split.get(1).getName());
            assertEquals("C0CC00881H_3b.cif", split.get(2).getName());

            CIF cif;
            cif = CifIO.readCif(split.get(0), "UTF-8");
            assertEquals(2, cif.getDataBlockList().size());
            assertEquals("global", cif.getDataBlockList().get(0).getId());
            assertEquals("3a", cif.getDataBlockList().get(1).getId());

            cif = CifIO.readCif(split.get(1), "UTF-8");
            assertEquals(2, cif.getDataBlockList().size());
            assertEquals("global", cif.getDataBlockList().get(0).getId());
            assertEquals("2a", cif.getDataBlockList().get(1).getId());

            cif = CifIO.readCif(split.get(2), "UTF-8");
            assertEquals(2, cif.getDataBlockList().size());
            assertEquals("global", cif.getDataBlockList().get(0).getId());
            assertEquals("3b", cif.getDataBlockList().get(1).getId());
        } finally {
            delete(tmpdir);
        }
    }

}
