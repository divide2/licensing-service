package com.divide2.licensing.repository;

import com.divide2.licensing.model.Organization;

public interface OrganizationRedisRepository {
    void saveOrganization(Organization organization);

    void updateOrganization(Organization organization);

    void deleteOrganization(String organizationId);

    Organization findOrganization(String organizationId);

}
