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
package org.osaf.cosmo.eim.schema.message;

/**
 * Constants related to the message schema.
 *
 * @see MessageStamp
 */
public interface MessageConstants {
    /** */
    public static final String FIELD_MESSAGE_ID = "messageId";
    /** */
    public static final int MAXLEN_MESSAGE_ID = 256;
    /** */
    public static final String FIELD_HEADERS = "headers";
    /** */
    public static final String FIELD_FROM = "fromAddress";
    /** */
    public static final int MAXLEN_FROM = 256;
    /** */
    public static final String FIELD_TO = "toAddress";
    /** */
    public static final int MAXLEN_TO = 1024;
    /** */
    public static final String FIELD_CC = "ccAddress";
    /** */
    public static final int MAXLEN_CC = 1024;
    /** */
    public static final String FIELD_BCC = "bccAddress";
    /** */
    public static final int MAXLEN_BCC = 1024;
    /** */
    public static final String FIELD_ORIGINATORS = "originators";
    /** */
    public static final int MAXLEN_ORIGINATORS = 1024; 
    /** */
    public static final String FIELD_DATE_SENT = "dateSent";
    /** */
    public static final int MAXLEN_DATE_SENT = 256;
    /** */
    public static final String FIELD_IN_REPLY_TO = "inReplyTo";
    /** */
    public static final int MAXLEN_IN_REPLY_TO = 256;
    /** */
    public static final String FIELD_REFERENCES = "references";
}
