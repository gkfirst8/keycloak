package org.keycloak.broker.oidc.mappers;

import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RoleMapper extends AbstractClaimMapper {

    public static final String[] COMPATIBLE_PROVIDERS = {KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID};

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    public static final String ROLE = "role";

    static {
        ProviderConfigProperty property;
        ProviderConfigProperty property1;
        property1 = new ProviderConfigProperty();
        property1.setName(CLAIM);
        property1.setLabel("Claim");
        property1.setHelpText("Name of claim to search for in token.  You can reference nested claims using a '.', i.e. 'address.locality'.");
        property1.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property1);
        property1 = new ProviderConfigProperty();
        property1.setName(CLAIM_VALUE);
        property1.setLabel("Claim Value");
        property1.setHelpText("Value the claim must have.  If the claim is an array, then the value must be contained in the array.");
        property1.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property1);
        property = new ProviderConfigProperty();
        property.setName(ROLE);
        property.setLabel("Role");
        property.setHelpText("Role to grant to user.  To reference an application role the syntax is appname.approle, i.e. myapp.myrole");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    public static final String PROVIDER_ID = "oidc-role-idp-mapper";

    public static String[] parseRole(String role) {
        int scopeIndex = role.indexOf('.');
        if (scopeIndex > -1) {
            String appName = role.substring(0, scopeIndex);
            role = role.substring(scopeIndex + 1);
            String[] rtn = {appName, role};
            return rtn;
        } else {
            String[] rtn = {null, role};
            return rtn;

        }
    }


    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Role Importer";
    }

    @Override
    public String getDisplayType() {
        return "Role Importer";
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String roleName = mapperModel.getConfig().get(ROLE);
        if (hasClaimValue(mapperModel, context)) {
            RoleModel role = getRoleFromString(realm, roleName);
            if (role == null) throw new IdentityBrokerException("Unable to find role: " + roleName);
            user.grantRole(role);
        }
    }

    protected RoleModel getRoleFromString(RealmModel realm, String roleName) {
        String[] parsedRole = parseRole(roleName);
        RoleModel role = null;
        if (parsedRole[0] == null) {
            role = realm.getRole(parsedRole[1]);
        } else {
            ClientModel client = realm.getClientByClientId(parsedRole[0]);
            role = client.getRole(parsedRole[1]);
        }
        return role;
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String roleName = mapperModel.getConfig().get(ROLE);
        if (!hasClaimValue(mapperModel, context)) {
            RoleModel role = getRoleFromString(realm, roleName);
            if (role == null) throw new IdentityBrokerException("Unable to find role: " + roleName);
            user.deleteRoleMapping(role);
        }

    }

    @Override
    public String getHelpText() {
        return "If a claim exists, grant the user the specified realm or application role.";
    }

}
