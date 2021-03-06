/*
 * Copyright 2018 ThoughtWorks, Inc.
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

package com.thoughtworks.go.api;

import com.thoughtworks.go.plugin.domain.common.Image;
import com.thoughtworks.go.server.service.plugins.builder.DefaultPluginInfoFinder;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static spark.Spark.*;

public class PluginImagesControllerDelegate implements SparkController, ControllerMethods {
    private final DefaultPluginInfoFinder pluginInfoFinder;

    public PluginImagesControllerDelegate(DefaultPluginInfoFinder pluginInfoFinder) {
        this.pluginInfoFinder = pluginInfoFinder;
    }

    @Override
    public String controllerBasePath() {
        return "/api/plugin_images";
    }

    @Override
    public void setupRoutes() {
        path(controllerBasePath(), () -> {
            get("/:plugin_id/:hash", this::show);
            head("/:plugin_id/:hash", this::show);
        });
    }

    private Object show(Request request, Response response) throws IOException {
        String pluginId = request.params("plugin_id");
        String hash = request.params("hash");

        Image image = pluginInfoFinder.getImage(pluginId, hash);

        if (image == null) {
            throw halt(404, "");
        }

        response.raw().setHeader("Cache-Control", "max-age=31557600, public");

        if (fresh(request, image.getHash())) {
            notModified(response);
            return new byte[0];
        }

        response.status(200);
        response.header("Content-Type", image.getContentType());
        this.setEtagHeader(response, image.getHash());
        if (request.requestMethod().equals("head")) {
            return new byte[0];
        } else {
            return image.getDataAsBytes();
        }
    }
}
