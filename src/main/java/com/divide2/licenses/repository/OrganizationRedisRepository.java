package com.divide2.licenses.repository;

import com.divide2.licenses.model.Organization;

public interface OrganizationRedisRepository {
    void saveOrganization(Organization organization);

    void updateOrganization(Organization organization);

    void deleteOrganization(String organizationId);

    Organization findOrganization(String organizationId);

}
