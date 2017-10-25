package com.divide2.licenses.event.handlers;

import com.divide2.licenses.event.CustomChannels;
import com.divide2.licenses.event.models.OrganizationChangeModel;
import com.divide2.licenses.repository.OrganizationRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;


@EnableBinding(CustomChannels.class)
public class  OrganizationChangeHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    @Autowired
    private OrganizationRedisRepository organizationRedisRepository;

    @StreamListener("inboundOrgChanges")
    public void loggerSink(OrganizationChangeModel orgChange) {
        logger.debug("From Kafka message organization id is : {}", orgChange.getOrganizationId());

        logger.debug("Received a message of type " + orgChange.getType());
        switch(orgChange.getAction()){
            case "GET":
                logger.debug("Received a GET event from the organization service for organization id {}", orgChange.getOrganizationId());
                break;
            case "SAVE":
                logger.debug("Received a SAVE event from the organization service for organization id {}", orgChange.getOrganizationId());
                break;
            case "UPDATE":
                logger.debug("Received a UPDATE event from the organization service for organization id {}", orgChange.getOrganizationId());
                organizationRedisRepository.deleteOrganization(orgChange.getOrganizationId());
                break;
            case "DELETE":
                logger.debug("Received a DELETE event from the organization service for organization id {}", orgChange.getOrganizationId());
                organizationRedisRepository.deleteOrganization(orgChange.getOrganizationId());
                break;
            default:
                logger.error("Received an UNKNOWN event from the organization service of type {}", orgChange.getType());
                break;

        }
    }
}
