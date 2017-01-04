package com.kutluarasli.azure.ddns;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.dns.ARecord;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.implementation.DnsManagementClientImpl;
import com.microsoft.azure.management.dns.implementation.RecordSetInner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class UpdateManager {

    private static Logger logger = LoggerFactory.getLogger(Program.class);

    private ApplicationProperties properties;

    UpdateManager(ApplicationProperties properties) {
        this.properties = properties;
    }

    void run() throws IOException {

        logger.info("Starting DNS update...");

        AzureTokenCredentials credentials = new ApplicationTokenCredentials(properties.getAppId(),
                properties.getTenantId(),
                properties.getSecret(),
                AzureEnvironment.AZURE);

        DnsManagementClientImpl client = new DnsManagementClientImpl(credentials).withSubscriptionId(properties.getSubscriptionId());

        RecordSetInner recordSet = getOrCreateRecordSet(client);

        ARecord aRecord = getOrAddRecord(recordSet);

        String newAddress = getExternalIP();
        if(isAddressChanged(aRecord, newAddress)){
            updateRecord(client, recordSet, aRecord, newAddress);
        }else{
            logger.debug("IP Address has not changed");
        }

        logger.debug("Successfully done...");
    }

    private boolean isAddressChanged(ARecord aRecord, String newIPAddress) {
        return !aRecord.ipv4Address().equals(newIPAddress);
    }

    private void updateRecord(DnsManagementClientImpl client, RecordSetInner recordSet, ARecord aRecord, String newAddress) throws IOException {
        logger.debug("Updating IP address");

        aRecord.withIpv4Address(newAddress);
        client.recordSets().createOrUpdate(properties.getResourceName(), properties.getZoneName(), properties.getRecordSetName(), RecordType.A, recordSet);

        logger.debug("IP address is updated");
    }

    private ARecord getOrAddRecord(RecordSetInner recordSet) {
        ARecord aRecord;
        if(recordSet.aRecords().size() > 0){
            aRecord = recordSet.aRecords().get(0);
        }else{
            aRecord = new ARecord();
            recordSet.aRecords().add(aRecord);
        }
        return aRecord;
    }

    private RecordSetInner getOrCreateRecordSet(DnsManagementClientImpl client) {
        logger.debug("Client created, now fetching record set pi");

        RecordSetInner recordSet = client.recordSets().get(properties.getResourceName(), properties.getZoneName(), properties.getRecordSetName(), RecordType.A);
        if(recordSet == null){
            logger.debug("No existing record set found. Creating a new one");

            recordSet = new RecordSetInner();
            recordSet = client.recordSets().createOrUpdate(properties.getResourceName(), properties.getZoneName(), properties.getRecordSetName(), RecordType.A, recordSet);

            logger.debug("A new record set created");
        }
        return recordSet;
    }

    private  String getExternalIP() throws IOException {

        logger.debug("Getting IP address");

        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(properties.getIPCheckURL()).build();
        Response response = httpClient.newCall(request).execute();
        String ip = response.body().string().trim();

        logger.debug("IP address is {}", ip);

        return ip;

    }
}
