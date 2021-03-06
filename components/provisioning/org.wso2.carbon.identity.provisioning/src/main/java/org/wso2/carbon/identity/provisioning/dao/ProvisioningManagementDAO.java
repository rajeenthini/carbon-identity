/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants;
import org.wso2.carbon.identity.provisioning.ProvisionedIdentifier;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProvisioningManagementDAO {

    private static final Log log = LogFactory.getLog(ProvisioningManagementDAO.class);

    /**
     * @param identityProviderName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantId
     * @throws IdentityApplicationManagementException
     */
    public void addProvisioningEntity(String identityProviderName, String connectorType,
                                      ProvisioningEntity provisioningEntity, int tenantId)
            throws IdentityApplicationManagementException {

        Connection dbConnection = null;
        try {
            dbConnection = JDBCPersistenceManager.getInstance().getDBConnection();

            PreparedStatement prepStmt = null;

            // id of the identity provider
            int idpId = getIdentityProviderIdentifier(dbConnection, identityProviderName, tenantId);

            // id of the provisioning configuration
            int provisioningConfigId = getProvisioningConfigurationIdentifier(dbConnection, idpId,
                    connectorType);

            // PROVISIONING_CONFIG_ID, ENTITY_TYPE,
            // ENTITY_LOCAL_USERSTORE, ENTITY_NAME, ENTITY_VALUE,
            // TENANT_ID
            String sqlStmt = IdentityProvisioningConstants.SQLQueries.ADD_PROVISIONING_ENTITY_SQL;

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, provisioningConfigId);
            prepStmt.setString(2, provisioningEntity.getEntityType().toString());
            prepStmt.setString(3, UserCoreUtil.extractDomainFromName(provisioningEntity.getEntityName()));
            prepStmt.setString(4, UserCoreUtil.removeDomainFromName(provisioningEntity.getEntityName()));
            prepStmt.setString(5, provisioningEntity.getIdentifier().getIdentifier());
            prepStmt.setInt(6, tenantId);

            prepStmt.execute();
            dbConnection.commit();
        } catch (SQLException | IdentityException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            String msg = "Error occurred while adding Provisioning entity for tenant " + tenantId;
            throw new IdentityApplicationManagementException(msg, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
    }

    /**
     * @param identityProviderName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantId
     * @throws IdentityApplicationManagementException
     */
    public void deleteProvisioningEntity(String identityProviderName, String connectorType,
                                         ProvisioningEntity provisioningEntity, int tenantId)
            throws IdentityApplicationManagementException {

        Connection dbConnection = null;
        try {
            dbConnection = JDBCPersistenceManager.getInstance().getDBConnection();

            PreparedStatement prepStmt = null;

            // id of the identity provider
            int idpId = getIdentityProviderIdentifier(dbConnection, identityProviderName, tenantId);

            // id of the provisioning configuration
            int provisioningConfigId = getProvisioningConfigurationIdentifier(dbConnection, idpId,
                    connectorType);

            // PROVISIONING_CONFIG_ID, ENTITY_TYPE,
            // ENTITY_LOCAL_USERSTORE, ENTITY_NAME, TENANT_ID
            String sqlStmt = IdentityProvisioningConstants.SQLQueries.DELETE_PROVISIONING_ENTITY_SQL;

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, provisioningConfigId);
            prepStmt.setString(2, provisioningEntity.getEntityType().toString());
            prepStmt.setString(3, UserCoreUtil.extractDomainFromName(provisioningEntity.getEntityName()));
            prepStmt.setString(4, UserCoreUtil.removeDomainFromName(provisioningEntity.getEntityName()));
            prepStmt.setInt(5, tenantId);

            prepStmt.execute();
            dbConnection.commit();
        } catch (SQLException | IdentityException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            String msg = "Error occurred while deleting Provisioning entity for tenant " + tenantId;
            throw new IdentityApplicationManagementException(msg, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
    }

    /**
     * @param identityProviderName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantId
     * @throws IdentityApplicationManagementException
     */
    public ProvisionedIdentifier getProvisionedIdentifier(String identityProviderName, String connectorType,
                                                          ProvisioningEntity provisioningEntity, int tenantId)
            throws IdentityApplicationManagementException {

        Connection dbConnection = null;
        try {
            dbConnection = JDBCPersistenceManager.getInstance().getDBConnection();

            PreparedStatement prepStmt = null;

            // id of the identity provider
            int idpId = getIdentityProviderIdentifier(dbConnection, identityProviderName, tenantId);

            // id of the provisioning configuration
            int provisioningConfigId = getProvisioningConfigurationIdentifier(dbConnection, idpId,
                    connectorType);

            // PROVISIONING_CONFIG_ID, ENTITY_TYPE,
            // ENTITY_LOCAL_USERSTORE, ENTITY_NAME, ENTITY_VALUE,
            // TENANT_ID
            String sqlStmt = IdentityProvisioningConstants.SQLQueries.GET_PROVISIONING_ENTITY_SQL;

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, provisioningConfigId);
            prepStmt.setString(2, provisioningEntity.getEntityType().toString());
            prepStmt.setString(3, UserCoreUtil.extractDomainFromName(provisioningEntity.getEntityName()));
            prepStmt.setString(4, UserCoreUtil.removeDomainFromName(provisioningEntity.getEntityName()));
            prepStmt.setInt(5, tenantId);


            ResultSet rs = prepStmt.executeQuery();
            if (rs.next()) {
                String entityId = rs.getString(1);
                ProvisionedIdentifier provisionedIdentifier = new ProvisionedIdentifier();
                provisionedIdentifier.setIdentifier(entityId);
                return provisionedIdentifier;
            } else {
                return null;
            }

        } catch (SQLException | IdentityException e) {
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            String msg = "Error occurred while adding Provisioning entity for tenant " + tenantId;
            throw new IdentityApplicationManagementException(msg, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
    }

    /**
     * @param newIdentityProvider
     * @param currentIdentityProvider
     * @param tenantId
     * @throws IdentityApplicationManagementException
     */
    public void updateProvisionedIdentifier(IdentityProvider newIdentityProvider,
                                            IdentityProvider currentIdentityProvider, int tenantId)
            throws IdentityApplicationManagementException {

        Connection dbConnection = null;

        try {

            dbConnection = JDBCPersistenceManager.getInstance().getDBConnection();

            int idPId =
                    getIdentityProviderIdByName(dbConnection,
                            newIdentityProvider.getIdentityProviderName(),
                            tenantId);

            if (idPId <= 0) {
                String msg =
                        "Trying to update non-existent Identity Provider for tenant " +
                                tenantId;
                throw new IdentityApplicationManagementException(msg);
            }

            PreparedStatement prepStmt = null;

            // SP_IDP_NAME=?, SP_IDP_PRIMARY=?,SP_IDP_HOME_REALM_ID=?,
            // SP_IDP_THUMBPRINT=?,
            // SP_IDP_TOKEN_EP_ALIAS=?,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED=?,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID=?,SP_IDP_USER_CLAIM_URI=?,
            // SP_IDP_ROLE_CLAIM_URI=?,SP_IDP_DEFAULT_AUTHENTICATOR_NAME=?,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME=?
            String sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            prepStmt.setString(1, newIdentityProvider.getIdentityProviderName());

            if (newIdentityProvider.isPrimary()) {
                prepStmt.setString(2, "1");
            } else {
                prepStmt.setString(2, "0");
            }

            prepStmt.setString(3, newIdentityProvider.getHomeRealmId());
            prepStmt.setBinaryStream(4, setBlobValue(newIdentityProvider.getCertificate()));
            prepStmt.setString(5, newIdentityProvider.getAlias());

            if (newIdentityProvider.getJustInTimeProvisioningConfig() != null &&
                    newIdentityProvider.getJustInTimeProvisioningConfig().isProvisioningEnabled()) {
                prepStmt.setString(6, "1");
                prepStmt.setString(7, newIdentityProvider.getJustInTimeProvisioningConfig()
                        .getProvisioningUserStore());

            } else {
                prepStmt.setString(6, "0");
                prepStmt.setString(7, null);
            }

            if (newIdentityProvider.getClaimConfig() != null) {
                prepStmt.setString(8, newIdentityProvider.getClaimConfig().getUserClaimURI());
                prepStmt.setString(9, newIdentityProvider.getClaimConfig().getRoleClaimURI());
            } else {
                prepStmt.setString(8, null);
                prepStmt.setString(9, null);
            }

            // update the default authenticator
            if (newIdentityProvider.getDefaultAuthenticatorConfig() != null &&
                    newIdentityProvider.getDefaultAuthenticatorConfig().getName() != null) {
                prepStmt.setString(10, newIdentityProvider.getDefaultAuthenticatorConfig()
                        .getName());
            } else {
                // its not a must to have a default authenticator.
                prepStmt.setString(10, null);
            }

            // update the default provisioning connector.
            if (newIdentityProvider.getDefaultProvisioningConnectorConfig() != null &&
                    newIdentityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
                prepStmt.setString(11, newIdentityProvider.getDefaultProvisioningConnectorConfig()
                        .getName());
            } else {
                // its not a must to have a default provisioning connector..
                prepStmt.setString(11, null);
            }

            prepStmt.setString(12, newIdentityProvider.getIdentityProviderDescription());

            prepStmt.setInt(13, tenantId);
            prepStmt.setString(14, currentIdentityProvider.getIdentityProviderName());

            prepStmt.executeUpdate();

            prepStmt.clearParameters();
            IdentityApplicationManagementUtil.closeStatement(prepStmt);

            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, newIdentityProvider.getIdentityProviderName());
            ResultSet rs = prepStmt.executeQuery();

            if (rs.next()) {

                // id of the updated identity provider.
                int idpId = rs.getInt(1);

            }

            dbConnection.commit();
        } catch (SQLException | IdentityException e) {
            log.error(e.getMessage(), e);
            IdentityApplicationManagementUtil.rollBack(dbConnection);
            String msg =
                    "Error occurred while updating Identity Provider information  for tenant " +
                            tenantId;
            throw new IdentityApplicationManagementException(msg, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
    }

    /**
     * @param idPName
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityApplicationManagementException
     */
    public void deleteProvisionedIdentifier(String idPName, int tenantId, String tenantDomain)
            throws IdentityApplicationManagementException {

        Connection dbConnection = null;
    }

    /**
     * @param conn
     * @param tenantId
     * @param idPName
     * @throws SQLException
     */
    private void deleteIdP(Connection conn, int tenantId, String idPName) throws SQLException {

        PreparedStatement prepStmt = null;
        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_SQL;

        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idPName);
            prepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idpName
     * @param tenantId
     * @return
     * @throws SQLException
     * @throws IdentityApplicationManagementException
     */
    private int getIdentityProviderIdByName(Connection dbConnection, String idpName, int tenantId)
            throws SQLException,
            IdentityApplicationManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            if (dbConnection == null) {
                dbConnection = JDBCPersistenceManager.getInstance().getDBConnection();
            } else {
                dbConnInitialized = false;
            }
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROW_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idpName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (IdentityException e) {
            throw new IdentityApplicationManagementException("Error while reading Identity Provider by name.", e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
            IdentityApplicationManagementUtil.closeResultSet(rs);
            if (dbConnInitialized) {
                IdentityApplicationManagementUtil.closeConnection(dbConnection);
            }
        }
        return 0;
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws SQLException
     * @throws IdentityApplicationManagementException
     */
    private int getIdentityProviderIdentifier(Connection dbConnection, String idPName, int tenantId)
            throws SQLException,
            IdentityApplicationManagementException {

        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ID_BY_NAME_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idPName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new IdentityApplicationManagementException("Invalid Identity Provider Name " +
                        idPName);
            }
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(rs);
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idPId
     * @param connectorType
     * @return
     * @throws SQLException
     * @throws IdentityApplicationManagementException
     */
    private int getProvisioningConfigurationIdentifier(Connection dbConnection, int idPId,
                                                       String connectorType) throws SQLException,
            IdentityApplicationManagementException {

        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            sqlStmt = IdentityProvisioningConstants.SQLQueries.GET_IDP_PROVISIONING_CONFIG_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, connectorType);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new IdentityApplicationManagementException("Invalid connector type " +
                        connectorType);
            }
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(rs);
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    private InputStream setBlobValue(String value) throws SQLException {
        if (value != null) {
            return new ByteArrayInputStream(value.getBytes());
        }
        return null;
    }


    public List<String> getSPNamesOfProvisioningConnectorsByIDP(String idPName, String tenantDomain)
            throws IdentityApplicationManagementException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> spNames = new ArrayList<String>();
        try {
            dbConnection = JDBCPersistenceManager.getInstance().getDBConnection();
            String sqlStmt = null;
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                sqlStmt = IdentityProvisioningConstants.SQLQueries.GET_SP_NAMES_OF_SUPER_TENANT_PROV_CONNECTORS_BY_IDP;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setString(1, idPName);
                prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            } else {
                sqlStmt = IdentityProvisioningConstants.SQLQueries.GET_SP_NAMES_OF_PROVISIONING_CONNECTORS_BY_IDP;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setString(1, idPName);
                prepStmt.setString(2, tenantDomain);
            }
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                spNames.add(rs.getString(1));
            }
        } catch (SQLException | IdentityException e) {
            String msg = "Error occurred while retrieving SP names of provisioning connectors by IDP name";
            throw new IdentityApplicationManagementException(msg, e);
        } finally {
            if (prepStmt != null) {
                IdentityApplicationManagementUtil.closeStatement(prepStmt);
            }
            if (rs != null) {
                IdentityApplicationManagementUtil.closeResultSet(rs);
            }
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
        return spNames;
    }
}
