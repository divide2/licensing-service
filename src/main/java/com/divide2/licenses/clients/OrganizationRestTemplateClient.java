package com.divide2.licenses.clients;

import com.divide2.licenses.model.Organization;
import com.divide2.licenses.repository.OrganizationRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class OrganizationRestTemplateClient {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);

    @Autowired
    private OAuth2RestTemplate restTemplate;

    @Autowired
    private OrganizationRedisRepository organizationRedisRepository;

/*    public Organization getOrganization(String organizationId) {
        String url = "http://zuulservice/api/organization/v1/organizations/{organizationId}";
        ResponseEntity<Organization> restExchange = restTemplate.exchange(url, HttpMethod.GET, null, Organization.class, organizationId);
        return restExchange.getBody();
    }*/

    private Organization checkRedisCache(String organizationId) {
        try {
            return organizationRedisRepository.findOrganization(organizationId);
        } catch (Exception ex) {
            logger.error("Error encountered while trying to retrieve organization {} check Redis Cache.Exception {}", organizationId, ex);
            return null;
        }

    }

    private void cacheOrganizationObject(Organization organization) {

        try {
            organizationRedisRepository.saveOrganization(organization);
        } catch (Exception e) {
            logger.error("Unable to cache organization {} , exception {}", organization.getId(), e);
        }
    }

    public Organization getOrganization(String organizationId){

        Organization org = checkRedisCache(organizationId);

        if (org!=null){
            logger.debug("I have successfully retrieved an organization {} from the redis cache: {}", organizationId, org);
            return org;
        }

        logger.debug("Unable to locate organization from the redis cache: {}.", organizationId);

        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        "http://zuulservice/api/organization/v1/organizations/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        /*Save the record from cache*/
        org = restExchange.getBody();

        if (org!=null) {
            cacheOrganizationObject(org);
        }

        return org;
    }
}
