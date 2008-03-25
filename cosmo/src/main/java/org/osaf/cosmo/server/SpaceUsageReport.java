/*
 * Copyright 2006-2008 Open Source Applications Foundation
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;

/**
 * Aggregates information about a user's usage of storage space.
 */
public class SpaceUsageReport {
    private static final Log log = LogFactory.getLog(SpaceUsageReport.class);

    private User user;
    private List<UsageLineItem> lineItems;

    /** */
    public SpaceUsageReport(User user,
                            HomeCollectionItem home) {
        lineItems = new ArrayList<UsageLineItem>();
        addLineItems(home, "/" + user.getUsername());
    }

    /** */
    public List<UsageLineItem> getLineItems() {
        return lineItems;
    }

    private void addLineItems(CollectionItem item,
                              String path) {
        lineItems.add(new UsageLineItem(item, path));
        for (Item child : item.getChildren()) {
            String childPath = path + "/" + child.getName();
            if (child instanceof CollectionItem)
                addLineItems((CollectionItem) child, childPath);
            else
                lineItems.add(new UsageLineItem(child, childPath));
        }
    }

    /**
     * Represents usage information for a single item.
     */
    public class UsageLineItem {

        private Item item;
        private String path;

        /** */
        public UsageLineItem(Item item,
                             String path) {
            this.item = item;
            this.path = path;
        }

        /** */
        public Date getLastAccessed() {
            return item.getModifiedDate();
        }

        /** */
        public Long getSize() {
            if (item instanceof FileItem)
                return ((FileItem)item).getContentLength();
            return new Long(0);
        }

        /** */
        public String getPath() {
            return path;
        }

        /** */
        public User getOwner() {
            return item.getOwner();
        }
    }
}
