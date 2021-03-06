package org.jclouds.imagestore.imagegenerator.reedsolomon;

/*
 * Copyright 2007 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * <p>
 * This class contains utility methods for performing mathematical operations over the Galois Fields.
 * Operations use a given primitive polynomial in calculations.
 * </p>
 * 
 * <p>
 * Throughout this package, elements of the GF are represented as an {@code int} for convenience and speed
 * (but at the cost of memory).
 * </p>
 * 
 * @author Sean Owen
 * @author David Olivier
 */
public final class GenericGF {

    public enum GenericGFs {
        AZTEC_DATA_12(new GenericGF(4201, 12)), // x^12 + x^6 + x^5 + x^3 + 1, 1.0256
            AZTEC_DATA_10(new GenericGF(1033, 10)), // x^10 + x^3 + 1, 1,0088
            AZTEC_DATA_8(new GenericGF(301, 8)), // x^8 + x^5 + x^3 + x^2+1, 1,1875
            // AZTEC_DATA_6(new GenericGF(67, 6)), // x^6 + x + 1, 1,1758
            // AZTEC_DATA_4(new GenericGF(19, 4)), // x^4 + x + 1, 1,1133
            QR_CODE_FIELD_256(new GenericGF(285, 8)); // x^8 + x^4 + x^3 + x^2 + 1, 1,0469

        public final GenericGF mGf;

        GenericGFs(final GenericGF gf) {
            mGf = gf;
        }

    }

    private static final int INITIALIZATION_THRESHOLD = 0;

    private int[] expTable;
    private int[] logTable;
    private GenericGFPoly zero;
    private GenericGFPoly one;
    private final int size;
    private final int primitive;
    private boolean initialized = false;
    private final int fieldSize;

    /**
     * Create a representation of GF(size) using the given primitive polynomial.
     * 
     * @param size
     *            the size as exponent of the Galois Field
     * @param primitive
     *            irreducible polynomial whose coefficients are represented by
     *            the bits of an int, where the least-significant bit represents the constant
     *            coefficient
     */
    private GenericGF(int primitive, int fieldSize) {
        this.primitive = primitive;
        this.size = 1 << fieldSize;
        this.fieldSize = fieldSize;

        if (size <= INITIALIZATION_THRESHOLD) {
            initialize();
        }
    }

    private void initialize() {
        expTable = new int[primitive];
        logTable = new int[size];
        int x = 1;
        for (int i = 0; i < size; i++) {
            expTable[i] = x;
            x <<= 1; // x = x * 2; we're assuming the generator alpha is 2
            if (x >= size) {
                x ^= primitive;
                x &= size - 1;
            }
        }
        for (int i = 0; i < size - 1; i++) {
            logTable[expTable[i]] = i;
        }
        // logTable[0] == 0 but this should never be used
        zero = new GenericGFPoly(this, new int[] {
            0
        });
        one = new GenericGFPoly(this, new int[] {
            1
        });
        initialized = true;
    }

    private void checkInit() {
        if (!initialized) {
            initialize();
        }
    }

    GenericGFPoly getZero() {
        checkInit();

        return zero;
    }

    GenericGFPoly getOne() {
        checkInit();

        return one;
    }

    /**
     * @return the monomial representing coefficient * x^degree
     */
    GenericGFPoly buildMonomial(int degree, int coefficient) {
        checkInit();

        if (degree < 0) {
            throw new IllegalArgumentException();
        }
        if (coefficient == 0) {
            return zero;
        }
        int[] coefficients = new int[degree + 1];
        coefficients[0] = coefficient;
        return new GenericGFPoly(this, coefficients);
    }

    /**
     * Implements both addition and subtraction -- they are the same in GF(size).
     * 
     * @return sum/difference of a and b
     */
    static int addOrSubtract(int a, int b) {
        return a ^ b;
    }

    /**
     * @return 2 to the power of a in GF(size)
     */
    int exp(int a) {
        checkInit();
        return expTable[a];
    }

    /**
     * @return base 2 log of a in GF(size)
     */
    int log(int a) {
        checkInit();

        if (a == 0) {
            throw new IllegalArgumentException();
        }
        return logTable[a];
    }

    /**
     * @return multiplicative inverse of a
     */
    int inverse(int a) {
        checkInit();

        if (a == 0) {
            throw new ArithmeticException();
        }
        return expTable[size - logTable[a] - 1];
    }

    /**
     * @return product of a and b in GF(size)
     */
    int multiply(int a, int b) {
        checkInit();

        if (a == 0 || b == 0) {
            return 0;
        }
        try {
            return expTable[(logTable[a] + logTable[b]) % (size - 1)];
        } catch (ArrayIndexOutOfBoundsException exc) {
            throw exc;
        }
    }

    public int getSize() {
        return size;
    }

    public int getPrimitive() {
        return primitive;
    }

    public int getFieldSize() {
        return fieldSize;
    }

}
