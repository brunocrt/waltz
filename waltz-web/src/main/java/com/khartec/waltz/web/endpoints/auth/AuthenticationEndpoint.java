/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.auth;


import com.auth0.jwt.JWTSigner;
import com.khartec.waltz.common.MapBuilder;
import com.khartec.waltz.model.settings.NamedSettings;
import com.khartec.waltz.model.user.LoginRequest;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.settings.SettingsService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.khartec.waltz.common.MapUtilities.newHashMap;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.before;
import static spark.Spark.post;


@Service
public class AuthenticationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("auth");

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class);

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final SettingsService settingsService;
    private final Filter filter;


    @Autowired
    public AuthenticationEndpoint(UserService userService,
                                  UserRoleService userRoleService,
                                  SettingsService settingsService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.settingsService = settingsService;

        this.filter = settingsService
                .getValue(NamedSettings.authenticationFilter)
                .flatMap(className -> instantiateFilter(className))
                .orElseGet(createDefaultFilter());
    }


    private Supplier<Filter> createDefaultFilter() {
        return () -> {
            LOG.info("Using default (jwt) authentication filter");
            return new JWTAuthenticationFilter(settingsService);
        };
    }


    private Optional<Filter> instantiateFilter(String className) {
        try {
            LOG.info("Setting authentication filter to: " + className);

            Filter filter = (Filter) Class
                    .forName(className)
                    .getConstructor(SettingsService.class)
                    .newInstance(settingsService);

            return Optional.of(filter);
        } catch (Exception e) {
            LOG.error("Cannot instantiate authentication filter class: " + className, e);
            return Optional.empty();
        }
    }


    @Override
    public void register() {

        post(mkPath(BASE_URL, "login"), (request, response) -> {

            LoginRequest login = readBody(request, LoginRequest.class);
            if (userService.authenticate(login)) {

                List<Role> roles = userRoleService.getUserRoles(login.userName());
                Map<String, Object> claims = new MapBuilder()
                        .add("iss", "Waltz")
                        .add("sub", login.userName())
                        .add("roles", roles)
                        .add("displayName", login.userName())
                        .add("employeeId", login.userName())
                        .build();
                JWTSigner signer = new JWTSigner(JWTUtilities.SECRET);
                String token = signer.sign(claims);

                return newHashMap("token", token);
            } else {
                response.status(401);

                return "Unknown user/password";
            }
        }, transformer);

        before(mkPath("api", "*"), filter);

    }

}