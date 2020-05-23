package qzui;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.admin.AdminModule;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.*;

import javax.inject.Named;

@Module
public class AppModule {

    private final ImmutableMap<String, RestxPrincipal> PRINCIPALS = defaultPrincipals();

    private ImmutableMap<String, RestxPrincipal> defaultPrincipals() {
        ImmutableMap.Builder<String, RestxPrincipal> builder = ImmutableMap.<String, RestxPrincipal>builder();
        builder.put("admin", AdminModule.RESTX_ADMIN_PRINCIPAL);
        return builder.build();
    }

    @Provides
    public SignatureKey signatureKey() {
        return new SignatureKey("5621d6b0-c047-4420-a59d-412a252de015 quartz-ui 5399913187595030480 quartz-ui".getBytes(Charsets.UTF_8));
    }

    @Provides
    @Named("restx.admin.password")
    public String restxAdminPassword() {
        return System.getProperty("qzui.admin.password", "qzui");
    }

    @Provides
    @Named("app.name")
    public String appName() {
        return "quartz-ui";
    }

    @Provides
    public CredentialsStrategy credentialsStrategy() {
        return new BCryptCredentialsStrategy();
    }

    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(SecuritySettings securitySettings,
                                                                   @Named("restx.admin.passwordHash") final String adminPasswordHash) {

        return new StdBasicPrincipalAuthenticator(new UserService<RestxPrincipal>() {

            @Override
            public Optional<RestxPrincipal> findUserByName(String name) {
                return Optional.fromNullable(PRINCIPALS.get(name));
            }

            @Override
            public Optional<RestxPrincipal> findAndCheckCredentials(String name, String passwordHash) {
                RestxPrincipal principal = PRINCIPALS.get(name);
                if (principal == null || !adminPasswordHash.equals(passwordHash)) {
                    return Optional.absent();
                } else {
                    return Optional.of(principal);
                }
            }
        }, securitySettings);
    }
}
