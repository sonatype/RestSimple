/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.rest.client;

import com.ning.http.client.Response;

import java.util.Map;

public abstract class ServiceDefinitionClient {

    abstract public Response doGet(String... paths);

    abstract public  Response doHead(String... paths);

    abstract public  Response doPut(String... paths);

    abstract public  Response doPost(Map<String, String> maps, String... paths);

    abstract public  Response doDelete(String... paths);

}
