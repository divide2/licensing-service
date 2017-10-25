package com.divide2.licenses.clients;

import com.divide2.licenses.model.Organization;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("zuulservice")
public interface OrganizationFeignClient {
    @RequestMapping(value = "/organization/v1/organizations/{organizationId}", method = RequestMethod.GET, consumes = "application/json")
    Organization getOrganization(@PathVariable("organizationId") String organizationId);

}
