package com.itextpdf.adapters.ndi.signing.services;

import com.itextpdf.adapters.ndi.signing.services.api.IChallengeCodeGenerator;

public class ChallengeCodeGenerator implements IChallengeCodeGenerator {


    @Override
    public Integer newCode() {
        return addDigitsIfNeeded(Math.round(Math.random() * (1000000 - 1)));
    }

    /**
     * Makes the code containing 6 meaningful digits
     * @param aInput
     * @return
     */
    private int addDigitsIfNeeded(long aInput) {
        String chCode = String.valueOf(aInput);

        if (chCode.length() == 6) {
            return (int)aInput;
        }

        String firstDigit = String.valueOf(Math.round(Math.random() * 8 + 1));
        chCode = firstDigit + new String(new char[5 - chCode.length()]).replace('\0', '0');
        return Integer.parseInt(chCode);
    }

}
