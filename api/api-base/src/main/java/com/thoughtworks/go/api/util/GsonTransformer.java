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

package com.thoughtworks.go.api.util;

import com.google.gson.*;
import com.google.gson.internal.bind.util.ISO8601Utils;
import spark.ResponseTransformer;

import java.util.Date;
import java.util.TimeZone;

public class GsonTransformer implements ResponseTransformer {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .disableHtmlEscaping()
            .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> src == null ? JsonNull.INSTANCE : new JsonPrimitive(ISO8601Utils.format(src, false, TimeZone.getTimeZone("UTC"))))
            .create();

    private GsonTransformer() {
    }

    @Override
    public String render(Object model) {
        if (model == null) {
            return "";
        }
        return GSON.toJson(model);
    }

    public <T> T fromJson(String string, Class<T> classOfT) {
        return GSON.fromJson(string, classOfT);
    }

    public static GsonTransformer getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final GsonTransformer INSTANCE = new GsonTransformer();
    }
}
