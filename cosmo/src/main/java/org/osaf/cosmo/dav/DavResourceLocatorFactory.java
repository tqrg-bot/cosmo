/*
 * Copyright 007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav;

import java.net.URL;

/**
 * <p>
 * The interface for factory classes that create instances of
 * {@link DavResourceLocator}.
 * </p>
 */
public interface DavResourceLocatorFactory {

    /**
     * <p>
     * Returns a locator for the resource at the specified path relative
     * to the given context URL. The context URL includes enough path
     * information to identify the dav namespace.
     * </p>
     * <p>
     *
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param path the unescaped path of the resource
     * </p>
     */
    public DavResourceLocator createResourceLocatorByPath(URL context,
                                                          String path);

    /**
     * <p>
     * Returns a locator for the resource at the specified URI relative
     * to the given context URL. The context URL includes enough path
     * information to identify the dav namespace.
     * </p>
     * <p>
     * If the URI is absolute, its scheme and authority must match those of
     * the context URL. The URI path must begin with the context URL's path.
     * </p>
     *
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param path the unescaped path of the resource
     * </p>
     */
    public DavResourceLocator createResourceLocatorByUri(URL context,
                                                         String uri)
        throws DavException;
}
