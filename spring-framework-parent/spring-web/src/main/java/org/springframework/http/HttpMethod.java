/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http;

/**
 * Java 5 enumeration of HTTP request methods. Intended for use
 * with {@link org.springframework.http.client.ClientHttpRequest}
 * and {@link org.springframework.web.client.RestTemplate}.
 * 
 * <p> Java 5 HTTP请求方法的枚举。 旨在与
 * org.springframework.http.client.ClientHttpRequest和
 * org.springframework.web.client.RestTemplate一起使用。
 *
 * @author Arjen Poutsma
 * @since 3.0
 */
public enum HttpMethod {

	GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE

}
