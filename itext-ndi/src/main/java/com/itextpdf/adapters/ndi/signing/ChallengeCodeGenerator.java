package com.itextpdf.adapters.ndi.signing;

import com.itextpdf.adapters.ndi.signing.api.IChallengeCodeGenerator;

public class ChallengeCodeGenerator implements IChallengeCodeGenerator {


    @Override
    public Integer generate() {
        return addDigitsIfNeeded(Math.round(Math.random() * (10000 - 1)));
    }

    /**
     * Makes the code containing 4 meaningful digits
     * @param aInput
     * @return
     */
    private int addDigitsIfNeeded(long aInput) {
        String chCode = String.valueOf(aInput);

        if (chCode.length() == 6) {
            return (int)aInput;
        }

        String firstDigit = String.valueOf(Math.round(Math.random() * 8 + 1));
        chCode = firstDigit + new String(new char[3 - chCode.length()]).replace('\0', '0')+aInput;
        return Integer.parseInt(chCode);
    }

}
