package com.divide2.licenses.services;

import com.divide2.licenses.clients.OrganizationDiscoveryClient;
import com.divide2.licenses.clients.OrganizationFeignClient;
import com.divide2.licenses.clients.OrganizationRestTemplateClient;
import com.divide2.licenses.config.ServiceConfig;
import com.divide2.licenses.model.License;
import com.divide2.licenses.model.Organization;
import com.divide2.licenses.repository.LicenseRepository;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
/*@DefaultProperties(
        commandProperties = {
                @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",
                        value = "10000")})*/

public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config;


    @Autowired
    OrganizationFeignClient organizationFeignClient;

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    OrganizationDiscoveryClient organizationDiscoveryClient;


    public License getLicense(String organizationId, String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        return license.withComment(config.getExampleProperty());
    }

    @HystrixCommand(
//            fallbackMethod = "buildFallbackLicenseList",
            threadPoolKey = "licenseByOrgThreadPool",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "30"),
                    @HystrixProperty(name = "maxQueueSize", value = "10")
            },
            commandProperties = {
                   /* @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "12000")*/
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5")

            }
    )
    public List<License> getLicensesByOrg(String organizationId) {
//        sleep();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    private void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private List<License> buildFallbackLicenseList(String organizationId) {
        List<License> licenses = new ArrayList<>();
        License license = new License()
                .withId("0000000000-0000000000")
                .withOrganizationId(organizationId)
                .withProductName("sorry no licensing infomation currentlly available");
        licenses.add(license);
        return licenses;
    }

    public void saveLicense(License license) {
        license.withId(UUID.randomUUID().toString());
        licenseRepository.save(license);
    }


    public void updateLicense(License license) {
        licenseRepository.save(license);
    }

    public void deleteLicense(License license) {
        licenseRepository.delete(license.getLicenseId());
    }

    public License getLicense(String organizationId, String licenseId, String clientType) {

        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        Organization org = retrieveOrgInfo(organizationId, clientType);
        return license
                .withOrganizationName(org.getName())
                .withContactEmail(org.getContactEmail())
                .withContactPhone(org.getContactPhone())
                .withComment(config.getExampleProperty());

    }


    private Organization retrieveOrgInfo(String organizationId, String clientType) {
        Organization organization = null;
        switch (clientType) {
            case "feign":
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "discovery":
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestClient.getOrganization(organizationId);
        }
        return organization;
    }
}
