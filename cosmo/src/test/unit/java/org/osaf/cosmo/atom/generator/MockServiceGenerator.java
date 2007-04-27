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
package org.osaf.cosmo.atom.generator.mock;

import org.apache.abdera.model.Service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.generator.ServiceGenerator;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Mock implementation of {@link ServiceGenerator} that generates dummy
 * services for use with atom unit tests.
 *
 * @see MockGeneratorFactory
 * @see Service
 * @see HomeCollectionItem
 */
public class MockServiceGenerator extends ServiceGenerator {
    private static final Log log =
        LogFactory.getLog(MockServiceGenerator.class);

    private MockGeneratorFactory factory;

    /** */
    public MockServiceGenerator(MockGeneratorFactory factory,
                                ServiceLocator locator) {
        super(factory.getAbdera().getFactory(), locator);
        this.factory = factory;
    }

    // ServiceGenerator methods

    public Service generateService(HomeCollectionItem home)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");
        return super.generateService(home);
    }
}
