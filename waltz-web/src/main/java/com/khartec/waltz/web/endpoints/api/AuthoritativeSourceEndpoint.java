/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.*;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static spark.Spark.delete;


@Service
public class AuthoritativeSourceEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritativeSourceEndpoint.class);
    private static final String BASE_URL = mkPath("api", "authoritative-source");

    private final AuthoritativeSourceService authoritativeSourceService;
    private final ChangeLogService changeLogService;
    private final UserRoleService userRoleService;


    @Autowired
    public AuthoritativeSourceEndpoint(
            AuthoritativeSourceService authoritativeSourceService,
            UserRoleService userRoleService,
            ChangeLogService changeLogService) {
        checkNotNull(authoritativeSourceService, "authoritativeSourceService must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(changeLogService, "changeLogService must not be null");

        this.authoritativeSourceService = authoritativeSourceService;
        this.userRoleService = userRoleService;
        this.changeLogService = changeLogService;
    }


    @Override
    public void register() {

        String recalculateFlowRatingsPath = mkPath(BASE_URL, "recalculate-flow-ratings");
        String findByDataTypeIdSelectPath = mkPath(BASE_URL, "data-type");
        String calculateConsumersForDataTypeIdSelectorPath = mkPath(BASE_URL, "data-type", "consumers");

        ListRoute<AuthoritativeSource> findByDataTypeIdSelectorRoute = (request, response)
                -> authoritativeSourceService.findByDataTypeIdSelector(readIdSelectionOptionsFromBody(request));

        getForDatum(
                recalculateFlowRatingsPath,
                this:: recalculateFlowRatingsRoute);

        postForList(calculateConsumersForDataTypeIdSelectorPath,
                this::calculateConsumersForDataTypeIdSelectorRoute);

        postForList(
                findByDataTypeIdSelectPath,
                findByDataTypeIdSelectorRoute);

        // --- TODO: convert these into new style...

        getForList(mkPath(BASE_URL, "kind", ":kind"), (request, response)
                -> authoritativeSourceService.findByEntityKind(getKind(request)));

        getForList(mkPath(BASE_URL, "kind", ":kind", ":id"), (request, response)
                -> authoritativeSourceService.findByEntityReference(getEntityReference(request)));

        getForList(mkPath(BASE_URL, "app", ":id"), (request, response)
                -> authoritativeSourceService.findByApplicationId(getId(request)));

        postForDatum(mkPath(BASE_URL, "id", ":id"), (request, response) -> {
            requireRole(userRoleService, request, Role.AUTHORITATIVE_SOURCE_EDITOR);
            String ratingStr = request.body();
            AuthoritativenessRating rating = AuthoritativenessRating.valueOf(ratingStr);
            authoritativeSourceService.update(getId(request), rating);
            return "done";
        });

        delete(mkPath(BASE_URL, "id", ":id"), (request, response) -> {
            requireRole(userRoleService, request, Role.AUTHORITATIVE_SOURCE_EDITOR);
            long id = getId(request);
            AuthoritativeSource authSource = authoritativeSourceService.getById(id);
            if (authSource == null) {
                return "done";
            }

            String msg = String.format(
                    "Removed %s as an %s authoritative source for %s",
                    authSource.applicationReference().name().orElse("an application"),
                    authSource.rating().name(),
                    authSource.dataType());

            ChangeLog log = ImmutableChangeLog.builder()
                    .message(msg)
                    .severity(Severity.INFORMATION)
                    .userId(getUsername(request))
                    .parentReference(authSource.parentReference())
                    .childKind(EntityKind.APPLICATION)
                    .operation(Operation.REMOVE)
                    .build();

            changeLogService.write(log);
            authoritativeSourceService.remove(id);

            return "done";
        });

        postForDatum(mkPath(BASE_URL, "kind", ":kind", ":id", ":dataType", ":appId"), (request, response) -> {
            requireRole(userRoleService, request, Role.AUTHORITATIVE_SOURCE_EDITOR);
            EntityReference parentRef = getEntityReference(request);
            String dataType = request.params("dataType");
            Long appId = getLong(request, "appId");

            String ratingStr = request.body();
            AuthoritativenessRating rating = AuthoritativenessRating.valueOf(ratingStr);

            authoritativeSourceService.insert(parentRef, dataType, appId, rating);
            return "done";
        });
    }


    private boolean recalculateFlowRatingsRoute(Request request, Response response) {
        requireRole(userRoleService, request, Role.ADMIN);

        String username = getUsername(request);
        LOG.info("Recalculating all flow ratings (requested by: {})", username);

        return authoritativeSourceService.recalculateAllFlowRatings();
    }


    private List<Entry<EntityReference, Collection<EntityReference>>> calculateConsumersForDataTypeIdSelectorRoute(
            Request request,
            Response response) throws IOException
    {
        IdSelectionOptions options = readIdSelectionOptionsFromBody(request);

        Map<EntityReference, Collection<EntityReference>> result = authoritativeSourceService
                .calculateConsumersForDataTypeIdSelector(options);

        return simplifyMapToList(result);
    }

}
