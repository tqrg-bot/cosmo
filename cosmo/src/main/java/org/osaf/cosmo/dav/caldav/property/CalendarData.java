/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.caldav.property;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.dav.caldav.CaldavConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents the <code>CALDAV:calendar-data</code> property as used
 * to transmit a set of icalendar objects in the body of a report
 * response.
 */
public class CalendarData extends AbstractDavProperty
    implements CaldavConstants {

    private String calendarData;

    /** */
    public CalendarData(String calendarData) {
        super(CALENDARDATA, true);
        this.calendarData = calendarData;
    }

    /** */
    public Object getValue() {
        return null;
    }

    /** */
    public Element toXml(Document doc) {
        Element cdata = DomUtil.
            createElement(doc, ELEMENT_CALDAV_CALENDAR_DATA, NAMESPACE_CALDAV);
        if (calendarData != null) {
            DomUtil.setText(cdata, calendarData);
        }
        return cdata;
    }
}