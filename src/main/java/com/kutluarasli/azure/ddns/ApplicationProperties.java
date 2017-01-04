package com.kutluarasli.azure.ddns;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class ApplicationProperties extends Properties {

    private ApplicationProperties(){

    }

    public static ApplicationProperties readFrom(String filePath) throws IOException {

        validateFile(filePath);

        FileInputStream fileInputStream = new FileInputStream(filePath);

        ApplicationProperties instance = new ApplicationProperties();
        instance.load(fileInputStream);

        fileInputStream.close();

        return instance;
    }

    private static void validateFile(String filePath) {
        File file = new File(filePath);
        if(!file.exists()){
            String error = String.format("File not found at %s", file);
            throw new RuntimeException(error);
        }
    }

    String getAppId(){
        return getMandatoryProperty("APP_ID");
    }

    String getTenantId(){
        return getMandatoryProperty("TENANT_ID");
    }

    String getSecret(){
        return getMandatoryProperty("SECRET");
    }

    String getSubscriptionId(){
        return getMandatoryProperty("SUBSCRIPTION_ID");
    }

    String getResourceName(){
        return getMandatoryProperty("RESOURCE_GROUP_NAME");
    }

    String getZoneName(){
        return getMandatoryProperty("ZONE_NAME");
    }

    String getRecordSetName(){
        return getMandatoryProperty("RECORDSET_NAME");
    }

    String getIPCheckURL(){
        return getMandatoryProperty("AMAZON_CHECKIP_URL");
    }

    private String getMandatoryProperty(String key){
        String value = getProperty(key);
        if(value == null || value.equals("")){
            String error = String.format("Missing configuration key: %s", key);
            throw new RuntimeException(error);
        }
        return value.trim();
    }


}
