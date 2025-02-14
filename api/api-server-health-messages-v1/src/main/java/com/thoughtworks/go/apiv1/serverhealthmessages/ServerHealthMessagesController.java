/*
 * Copyright 2022 ThoughtWorks, Inc.
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
package com.thoughtworks.go.apiv1.serverhealthmessages;


import com.thoughtworks.go.api.ApiController;
import com.thoughtworks.go.api.ApiVersion;
import com.thoughtworks.go.api.spring.ApiAuthenticationHelper;
import com.thoughtworks.go.apiv1.serverhealthmessages.representers.ServerHealthMessagesRepresenter;
import com.thoughtworks.go.serverhealth.ServerHealthService;
import com.thoughtworks.go.serverhealth.ServerHealthStates;
import com.thoughtworks.go.spark.Routes;
import com.thoughtworks.go.spark.spring.SparkSpringController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

@Component
public class ServerHealthMessagesController extends ApiController implements SparkSpringController {
    private final ServerHealthService serverHealthService;
    private final ApiAuthenticationHelper apiAuthenticationHelper;

    @Autowired
    public ServerHealthMessagesController(ServerHealthService serverHealthService, ApiAuthenticationHelper apiAuthenticationHelper) {
        super(ApiVersion.v1);
        this.serverHealthService = serverHealthService;
        this.apiAuthenticationHelper = apiAuthenticationHelper;
    }

    @Override
    public String controllerBasePath() {
        return Routes.ServerHealthMessages.BASE;
    }

    @Override
    public void setupRoutes() {
        path(Routes.ServerHealthMessages.BASE, () -> {
            before("", mimeType, this::setContentType);
            before("/*", mimeType, this::setContentType);

            before("", mimeType, apiAuthenticationHelper::checkUserAnd403);
            before("/*", mimeType, apiAuthenticationHelper::checkUserAnd403);

            get("", mimeType, this::show);
            head("", mimeType, this::show);
        });
    }

    public String show(Request request, Response response) {
        ServerHealthStates allLogs = serverHealthService.logs();
        String json = jsonizeAsTopLevelArray(request, outputListWriter -> ServerHealthMessagesRepresenter.toJSON(outputListWriter, allLogs));
        String etag = etagFor(json);

        if (fresh(request, etag)) {
            return notModified(response);
        }

        setEtagHeader(response, etag);
        return json;
    }
}
