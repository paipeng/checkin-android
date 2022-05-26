package com.paipeng.checkin.utils;

import junit.framework.TestCase;

public class CommonUtilTest extends TestCase {

    public void testConvertHexToString() {
        String hex = "E5-A7-93-E5-90-8D-3A-20-E5-88-98-E5-A4-87-0A-E5-8D-95-E4-BD-8D-3A-20-E4-B8-89-E5-9B-BD-E8-9C-80-E5-9B-BD-0A-E8-AF-81-E5-8D-A1-E7-BC-96-E5-8F-B7-3A-20-53-30-30-30-30-30-30-31-0A-E8-BF-87-E6-9C-9F-E6-97-A5-E6-9C-9F-3A-20-32-30-32-33-2D-31-32-2D-30-31-0A-E8-8A-AF-E7-89-87-E5-BA-8F-E5-8F-B7-3A-20-46-41-32-46-42-46-41-32-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-90-00";
        hex = hex.replace("-", "");

        byte[] data = StringUtil.hexStringToBinary(hex);


        int data_len = 0;
        for (int i = 0; i < data.length; i++) {
            System.out.print(data[i] + " ");
            if (data[i] == 0) {
                data_len = i;
                break;
            }
        }

        String text = new String(data, 0, data_len);
        System.out.println("text: " + text);
    }
}