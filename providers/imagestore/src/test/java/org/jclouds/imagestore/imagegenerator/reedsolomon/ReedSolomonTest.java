/**
 * 
 */
package org.jclouds.imagestore.imagegenerator.reedsolomon;

import java.util.Random;

import org.jclouds.imagestore.imagegenerator.reedsolomon.GenericGF.GenericGFs;
import org.testng.annotations.Test;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ReedSolomonTest {
    final static Random ran = new Random(12l);

    // bytes to corrupt must be at most halb of ec-bytes
    final static int toCorrupt = 8;
    static byte[] data = new byte[226];
    static int[][] corruptedBytes = new int[2][toCorrupt];
    static {
        ran.nextBytes(data);
        for (int i = 0; i < toCorrupt; i++) {
            corruptedBytes[0][i] = ran.nextInt(data.length);
            corruptedBytes[1][i] = ran.nextInt(data.length);
        }
    }

    @Test
    public void testByte() {
        ReedSolomon tool = new ReedSolomon();
        byte[] encoded = tool.encode(data);
        byte[] corrupted = AbstractReedSolomonTestCase.corrupt(encoded, toCorrupt, ran);
        byte[] decoded = tool.decode(corrupted);
        AbstractReedSolomonTestCase.assertArraysEqual(data, 0, decoded, 0, data.length);
    }

    @Test
    public void testInt() throws ReedSolomonException {
        // size + ec must be under 256 whereas
        final int ecBytes = 16;

        int[] bytes = ReedSolomon.castToInt(data);
        int[] encoded = new int[bytes.length + ecBytes];
        System.arraycopy(bytes, 0, encoded, 0, bytes.length);

        ReedSolomonEncoder tool = new ReedSolomonEncoder(GenericGFs.QR_CODE_FIELD_256.mGf);
        ReedSolomonDecoder tool2 = new ReedSolomonDecoder(GenericGFs.QR_CODE_FIELD_256.mGf);
        tool.encode(encoded, ecBytes);
        AbstractReedSolomonTestCase.corrupt(encoded, toCorrupt, ran);
        tool2.decode(encoded, ecBytes);
        AbstractReedSolomonTestCase.assertArraysEqual(bytes, 0, encoded, 0, bytes.length);
    }

    @Test
    public void testSplitter() {
        int multipleSize = ran.nextInt(20) * 226 + ran.nextInt(226);
        data = new byte[multipleSize];
        ran.nextBytes(data);
        testByte();
    }
}
