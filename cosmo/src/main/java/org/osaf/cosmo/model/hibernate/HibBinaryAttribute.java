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
package org.osaf.cosmo.model.hibernate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.io.IOUtils;
import org.hibernate.annotations.Type;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.BinaryAttribute;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.QName;

/**
 * Hibernate persistent BinaryAtttribute.
 */
@Entity
@DiscriminatorValue("binary")
public class HibBinaryAttribute extends HibAttribute implements java.io.Serializable, BinaryAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 6296196539997344427L;

    @Column(name = "binvalue", length=102400000)
    @Type(type="bytearray_blob")
    private byte[] value;

    /** default constructor */
    public HibBinaryAttribute() {
    }

    public HibBinaryAttribute(QName qname, byte[] value) {
        setQName(qname);
        this.value = value;
    }
    
    /**
     * Construct BinaryAttribute and initialize data using
     * an InputStream
     * @param qname 
     * @param value
     */
    public HibBinaryAttribute(QName qname, InputStream value) {
        setQName(qname);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(value, bos);
        } catch (IOException e) {
           throw new RuntimeException("error reading input stream");
        }
        this.value = bos.toByteArray();
    }

    // Property accessors
 
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#getValue()
     */
    public byte[] getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.BinaryAttribute#setValue(byte[])
     */
    public void setValue(byte[] value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof byte[]))
            throw new ModelValidationException(
                    "attempted to set non binary value on attribute");
        setValue((byte[]) value);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.BinaryAttribute#getLength()
     */
    public int getLength() {
        if(value!=null)
            return value.length;
        else
            return 0;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.BinaryAttribute#getInputStream()
     */
    public InputStream getInputStream() {
        if(value!=null)
            return new ByteArrayInputStream(value);
        else return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.model.Attribute#copy()
     */
    public Attribute copy() {
        BinaryAttribute attr = new HibBinaryAttribute();
        attr.setQName(getQName().copy());
        if (value != null)
            attr.setValue(value.clone());
        return attr;
    }

}
