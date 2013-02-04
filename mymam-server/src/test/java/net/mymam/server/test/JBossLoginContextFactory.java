/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mymam.server.test;

import org.jboss.security.auth.spi.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Provides a {@link LoginContext} for use by unit tests.
 *
 * <p/>
 * This is based on Stephen Coy's
 * <a href="https://github.com/sfcoy/demos.git">Arquillian Security Demo</a>,
 * but uses database authentication instead of UsersRoles properties files.
 *
 * <p/>
 * See also:
 * <ul>
 *     <li>https://community.jboss.org/wiki/TestingSecuredEJBsOnJBossAS71xWithArquillian</li>
 *     <li>http://stackoverflow.com/questions/11386651</li>
 * </ul>
 *
 * @author fstab
 */
public class JBossLoginContextFactory {

    static class NamePasswordCallbackHandler implements CallbackHandler {
        private final String username;
        private final String password;

        private NamePasswordCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback current : callbacks) {
                if (current instanceof NameCallback) {
                    ((NameCallback) current).setName(username);
                } else if (current instanceof PasswordCallback) {
                    ((PasswordCallback) current).setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(current);
                }
            }
        }
    }

    static class JBossJaasConfiguration extends Configuration {

        private final String configurationName;

        JBossJaasConfiguration(String configurationName) {
            this.configurationName = configurationName;
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if (!configurationName.equals(name)) {
                throw new IllegalArgumentException("Unexpected configuration name '" + name + "'");
            }

            return new AppConfigurationEntry[] {
                    createDatabaseModuleConfigEntry(),
                    createClientLoginModuleConfigEntry(),
            };
        }

        /**
         * The {@link org.jboss.security.auth.spi.DatabaseServerLoginModule} creates the
         * association between users and roles.
         *
         * @return
         */
        private AppConfigurationEntry createDatabaseModuleConfigEntry() {
            Map options = new HashMap();
            options.put("dsJndiName", "java:jboss/jdbc/mymamDS");
            options.put("principalsQuery", "select hashedpassword from user_table where username=?");
            options.put("rolesQuery", "select name, 'Roles' from role join role_user_table on role.id = role_user_table.roles_id join user_table on user_table.id = role_user_table.users_id where user_table.username=?");
            options.put("hashAlgorithm", "MD5");
            options.put("hashEncoding", "base64");
            return new AppConfigurationEntry("org.jboss.security.auth.spi.DatabaseServerLoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
        }

        /**
         * The {@link org.jboss.security.ClientLoginModule} associates the user credentials with the
         * {@link org.jboss.security.SecurityContext} where the JBoss security runtime can find it.
         *
         * @return
         */
        private AppConfigurationEntry createClientLoginModuleConfigEntry() {
            Map<String, String> options = new HashMap<String, String>();
            options.put("multi-threaded", "true");
            options.put("restore-login-identity", "true");

            return new AppConfigurationEntry("org.jboss.security.ClientLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
        }
    }

    /**
     * Obtain a LoginContext configured for use with the ClientLoginModule.
     *
     * @return the configured LoginContext.
     */
    public static LoginContext createLoginContext(final String username, final String password) throws LoginException {
        final String configurationName = "Arquillian Testing";
        CallbackHandler cbh = new JBossLoginContextFactory.NamePasswordCallbackHandler(username, password);
        Configuration config = new JBossJaasConfiguration(configurationName);
        return new LoginContext(configurationName, new Subject(), cbh, config);
    }
}
