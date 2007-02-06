/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.server;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.User;

/**
 * This class encapsulates the addressing scheme for all client
 * services provided by Cosmo, those protocols and interfaces that
 * allow communication between clients and Cosmo.
 *
 * <h2>Service Addresses</h2>
 *
 * Each service is "mounted" in the server's URL-space at its own
 * prefix relative to the "application mount URL". This URL is
 * composed of the scheme, host, and port information (see RFC 1738),
 * followed by the context path of the Cosmo web application and the
 * servlet path of the protocol or interface.
 * <p>
 * For example, the URL <code>http://localhost:8080/cosmo/dav</code>
 * addresses the Cosmo WebDAV service.
 *
 * <h2>Collection Addresses</h2>
 *
 * Collections in the Cosmo database are addressed similarly
 * regardless of which service is used to access the data. See
 * {@link CollectionPath} for details on the makeup of collection
 * URLs.
 * <p>
 * Note that individual items contained within collections are not
 * addressable at this time.
 *
 * <h2>User Addresses</h2>
 *
 * Users are addressed similarly to collections. See
 * {@link UserPath} for details on the makeup of user URLs.
 *
 * @see ServiceLocatorFactory
 * @see CollectionPath
 * @see UserPath
 */
public class ServiceLocator implements ServerConstants {
    private static final Log log = LogFactory.getLog(ServiceLocator.class);

    private static final String PATH_COLLECTION = "collection";
    private static final String PATH_USER = "user";

    private String appMountUrl;
    private String ticketKey;
    private ServiceLocatorFactory factory;

    /**
     * Returns a <code>ServiceLocator</code> instance that uses the
     * uses the given application mount URL as the base for all
     * service URLs.
     *
     * @param appMountUrl the application mount URL
     * @param factory the service location factory
     */
    public ServiceLocator(String appMountUrl,
                          ServiceLocatorFactory factory) {
        this(appMountUrl, null, factory);
    }

    /**
     * Returns a <code>ServiceLocator</code> instance that uses the
     * uses the given application mount URL as the base for and
     * includes the given ticket key in all service URLs.
     *
     * @param appMountUrl the application mount URL
     * @param factory the service location factory
     * @param ticketKey the ticket key
     */
    public ServiceLocator(String appMountUrl,
                          String ticketKey,
                          ServiceLocatorFactory factory) {
        this.appMountUrl = appMountUrl;
        this.ticketKey = ticketKey;
        this.factory = factory;
    }

    /**
     * Returns a map of base service URLs keyed by service id.
     */
    public Map<String,String> getBaseUrls() {
        HashMap<String,String> urls = new HashMap<String,String>();
        urls.put(SVC_ATOM, getAtomBase());
        urls.put(SVC_MORSE_CODE, getMorseCodeBase());
        urls.put(SVC_PIM, getPimBase());
        urls.put(SVC_WEBCAL, getWebcalBase());
        return urls;
    }

    /**
     * Returns a map of URLs for the collection keyed by service id.
     */
    public Map<String,String> getCollectionUrls(CollectionItem collection) {
        HashMap<String,String> urls = new HashMap<String,String>();
        urls.put(SVC_ATOM, getAtomUrl(collection));
        urls.put(SVC_DAV, getDavUrl(collection));
        urls.put(SVC_MORSE_CODE, getMorseCodeUrl(collection));
        urls.put(SVC_PIM, getPimUrl(collection));
        urls.put(SVC_WEBCAL, getWebcalUrl(collection));
        return urls;
    }

    /**
     * Returns a map of URLs for the user keyed by service id.
     */
    public Map<String,String> getUserUrls(User user) {
        HashMap<String,String> urls = new HashMap<String,String>();
        urls.put(SVC_CMP, getCmpUrl(user));
        urls.put(SVC_DAV, getDavUrl(user));
        urls.put(SVC_DAV_PRINCIPAL, getDavPrincipalUrl(user));
        urls.put(SVC_DAV_CALENDAR_HOME, getDavCalendarHomeUrl(user));
        return urls;
    }

    /**
     * Returns the Atom base URL.
     */
    public String getAtomBase() {
        return calculateBaseUrl(factory.getAtomPrefix());
    }

    /**
     * Returns the Atom URL of the collection.
     */
    public String getAtomUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getAtomPrefix());
    }

    /**
     * Returns the CMP URL of the user.
     */
    public String getCmpUrl(User user) {
        return calculateUserUrl(user, factory.getCmpPrefix());
    }

    /**
     * Returns the WebDAV URL of the collection.
     */
    public String getDavUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getDavPrefix());
    }

    /**
     * Returns the WebDAV URL of the user.
     */
    public String getDavUrl(User user) {
        return calculateUserUrl(user, factory.getDavPrefix());
    }

    /**
     * Returns the WebDAV principal URL of the user.
     */
    public String getDavPrincipalUrl(User user) {
        return calculateUserUrl(user, factory.getDavPrincipalPrefix());
    }

    /**
     * Returns the CalDAV calendar home URL of the user.
     */
    public String getDavCalendarHomeUrl(User user) {
        return calculateUserUrl(user, factory.getDavCalendarHomePrefix());
    }

    /**
     * Returns the Morse Code base URL.
     */
    public String getMorseCodeBase() {
        return calculateBaseUrl(factory.getMorseCodePrefix());
    }

    /**
     * Returns the Morse Code URL of the collection.
     */
    public String getMorseCodeUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection,
                                      factory.getMorseCodePrefix());
    }

    /**
     * Returns the Pim UI base URL.
     */
    public String getPimBase() {
        return calculateBaseUrl(factory.getPimPrefix());
    }

    /**
     * Returns the Pim UI URL of the collection.
     */
    public String getPimUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getPimPrefix());
    }

    /**
     * Returns the webcal base URL.
     */
    public String getWebcalBase() {
        return calculateBaseUrl(factory.getWebcalPrefix());
    }

    /**
     * Returns the webcal URL of the collection.
     */
    public String getWebcalUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getWebcalPrefix());
    }

    private String calculateBaseUrl(String servicePrefix) {
        StringBuffer buf = new StringBuffer(appMountUrl);

        buf.append(servicePrefix);

        return buf.toString();
    }
    private String calculateCollectionUrl(CollectionItem collection,
                                          String servicePrefix) {
        StringBuffer buf = new StringBuffer(appMountUrl);

        buf.append(servicePrefix).
            append("/").append(PATH_COLLECTION).
            append("/").append(collection.getUid());

        if (ticketKey != null)
            buf.append("?").
                append(PARAM_TICKET).append("=").append(ticketKey);

        return buf.toString();
    }

    private String calculateUserUrl(User user,
                                    String servicePrefix) {
        StringBuffer buf = new StringBuffer(appMountUrl);

        buf.append(servicePrefix).
            append("/").append(PATH_USER).
            append("/").append(user.getUsername());

        return buf.toString();
    }
}