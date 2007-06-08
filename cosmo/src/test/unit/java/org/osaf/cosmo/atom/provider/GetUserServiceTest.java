/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.atom.provider;

import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test class for {@link ProviderProxy#getService()} tests.
 */
public class GetUserServiceTest extends BaseProviderProxyTestCase {
    private static final Log log = LogFactory.getLog(GetUserServiceTest.class);

    public void testGetService() throws Exception {
        helper.makeAndStoreDummyCollection();
        helper.makeAndStoreDummyCollection();
        helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createServiceRequestContext();

        ResponseContext res = provider.getService(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
    }

    public void testGenerationError() throws Exception {
        helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createServiceRequestContext();
        helper.enableGeneratorFailure();

        ResponseContext res = provider.getService(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }
}