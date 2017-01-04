package com.kutluarasli.azure.ddns;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Program {

    private static Logger logger = LoggerFactory.getLogger(Program.class);

    public static void main(String[] args) throws IOException {

        try {

            String configFilePath = readConfigFilePathFromArguments(args);

            AzureProperties config = AzureProperties.readFrom(configFilePath);

            UpdateManager updateManager = new UpdateManager(config);
            updateManager.run();

        } catch (Exception e) {

            logger.error("Update failed", e);

            e.printStackTrace();
        }
    }

    private static String readConfigFilePathFromArguments(String[] args){
        if(args.length == 0){
            throw new RuntimeException("Config file path is requred as an argument");
        }
        return args[0].trim();

    }
}
