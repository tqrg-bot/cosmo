/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.util.Dates;

/**
 * @author cyrusdaboo
 * 
 * A list of instances . Instances are created by adding a component, either the
 * master recurrence component or an overridden instance of one. Its is the
 * responsibility of the caller to ensure all the components added are for the
 * same event (i.e. UIDs are all the same). Also, the master instance MUST be
 * added first.
 */

public class InstanceList extends TreeMap {

    private static final long serialVersionUID = 1838360990532590681L;
    private boolean isUTC = false;
    private TimeZone timezone = null;
    
    public InstanceList() {
        super();
    }

    /**
     * Add a component (either master or override instance) if it falls within
     * the specified time range.
     * 
     * @param comp
     * @param rangeStart
     * @param rangeEnd
     */
    public void addComponent(Component comp, Date rangeStart, Date rangeEnd) {

        // See if it contains a recurrence ID
        if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null) {
            addMaster(comp, rangeStart, rangeEnd);
        } else {
            addOverride(comp, rangeStart, rangeEnd);
        }
    }
    
    /**
     * @return if the InstanceList generates instances in UTC format.
     */
    public boolean isUTC() {
        return isUTC;
    }

    /**
     * Instruct the InstanceList to generate instances in UTC time periods.
     * If set to false, InstanceList will generate floating time instances
     * for events with floating date/times.
     * @param isUTC
     */
    public void setUTC(boolean isUTC) {
        this.isUTC = isUTC;
    }
    
    /**
     * @return timezone used to convert floating times to UTC.  Only
     * used if isUTC is set to true.
     */
    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Set the timezone to use when converting floating times to
     * UTC.  Only used if isUTC is set to true.
     * @param timezone
     */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    /**
     * Add a master component if it falls within the specified time range.
     * 
     * @param comp
     * @param rangeStart
     * @param rangeEnd
     */
    protected void addMaster(Component comp, Date rangeStart, Date rangeEnd) {

        Date start = getStartDate(comp);
        
        if (start == null) {
            return;
        }
        
        
        if(start instanceof DateTime) {
            // convert to UTC if configured
            start = convertToUTCIfNecessary((DateTime) start);
            // adjust floating time if timezone is present
            start = adjustFloatingDateIfNecessary(start);
        }
        
        
        Dur duration = null;
        Date end = getEndDate(comp);
        if (end == null) {
            if (start instanceof DateTime) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            end = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
        } else {
            if(end instanceof DateTime) {
                // Convert to UTC if needed
                end = convertToUTCIfNecessary((DateTime) end);
                // Adjust floating end time if timezone present
                end = adjustFloatingDateIfNecessary(end);
                // Handle case where dtend is before dtstart, in which the duration
                // will be 0, since it is a timed event
                if(end.before(start)) {
                    end = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(0, 0, 0, 0).getTime(start), start);
                }
            } else {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 1 day since its an all-day event
                if(end.before(start)) {
                    end = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(1, 0, 0, 0).getTime(start), start);
                }
            }
            duration = new Dur(start, end);
        }
        
        // Always add first instance if included in range..
        if (start.before(rangeEnd) && 
                (end.after(rangeStart) || end.equals(rangeStart))) {
         
            Instance instance = new Instance(comp, start, end);
            put(instance.getRid().toString(), instance);
        }
        
        // Adjust startRange to account for instances that occur before
        // the startRange, and end after it
        rangeStart = adjustStartRangeIfNecessary(rangeStart, start, duration);

        // recurrence dates..
        PropertyList rDates = comp.getProperties()
                .getProperties(Property.RDATE);
        for (Iterator i = rDates.iterator(); i.hasNext();) {
            RDate rdate = (RDate) i.next();
            // Both PERIOD and DATE/DATE-TIME values allowed
            if (Value.PERIOD.equals(rdate.getParameters().getParameter(
                    Parameter.VALUE))) {
                for (Iterator j = rdate.getPeriods().iterator(); j.hasNext();) {
                    Period period = (Period) j.next();
                    Date periodStart = adjustFloatingDateIfNecessary(period.getStart());
                    Date periodEnd = adjustFloatingDateIfNecessary(period.getEnd());
                    // Add period if it overlaps rage
                    if (periodStart.before(rangeEnd)
                            && periodEnd.after(rangeStart)) {
                        Instance instance = new Instance(comp, periodStart, periodEnd);
                        put(instance.getRid().toString(), instance);
                    }
                }
            } else {
                for (Iterator j = rdate.getDates().iterator(); j.hasNext();) {
                    Date startDate = (Date) j.next();
                    startDate = adjustFloatingDateIfNecessary(startDate);
                    Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration
                            .getTime(startDate), startDate);
                    // Add RDATE if it overlaps range
                    if (startDate.before(rangeEnd)
                            && endDate.after(rangeStart)) {
                        Instance instance = new Instance(comp, startDate, endDate);
                        put(instance.getRid().toString(), instance);
                    }
                }
            }
        }

        // recurrence rules..
        PropertyList rRules = comp.getProperties()
                .getProperties(Property.RRULE);
        for (Iterator i = rRules.iterator(); i.hasNext();) {
            RRule rrule = (RRule) i.next();
            // DateList startDates = rrule.getRecur().getDates(start.getDate(),
            // adjustedRangeStart, rangeEnd, (Value)
            // start.getParameters().getParameter(Parameter.VALUE));
            DateList startDates = rrule.getRecur().getDates(start, rangeStart,
                    rangeEnd,
                    (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);
            for (int j = 0; j < startDates.size(); j++) {
                Date sd = (Date) startDates.get(j);
                Date startDate = org.osaf.cosmo.calendar.util.Dates.getInstance(sd, start);
                Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(sd), start);
                Instance instance = new Instance(comp, startDate, endDate);
                put(instance.getRid().toString(), instance);
            }
        }
        // exception dates..
        PropertyList exDates = comp.getProperties().getProperties(
                Property.EXDATE);
        for (Iterator i = exDates.iterator(); i.hasNext();) {
            ExDate exDate = (ExDate) i.next();
            for (Iterator j = exDate.getDates().iterator(); j.hasNext();) {
                Date sd = (Date) j.next();
                sd = adjustFloatingDateIfNecessary(sd);
                Instance instance = new Instance(comp, sd, sd);
                remove(instance.getRid().toString());
            }
        }
        // exception rules..
        PropertyList exRules = comp.getProperties().getProperties(
                Property.EXRULE);
        for (Iterator i = exRules.iterator(); i.hasNext();) {
            ExRule exrule = (ExRule) i.next();
            // DateList startDates = exrule.getRecur().getDates(start.getDate(),
            // adjustedRangeStart, rangeEnd, (Value)
            // start.getParameters().getParameter(Parameter.VALUE));
            DateList startDates = exrule.getRecur().getDates(start, rangeStart,
                    rangeEnd,
                    (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);
            for (Iterator j = startDates.iterator(); j.hasNext();) {
                Date sd = (Date) j.next();
                Instance instance = new Instance(comp, sd, sd);
                remove(instance.getRid().toString());
            }
        }
    }

    /**
     * Add an override component if it falls within the specified time range.
     * 
     * @param comp
     * @param rangeStart
     * @param rangeEnd
     * @return true if the override component modifies instance list and false
     *         if the override component has no effect on instance list
     */
    public boolean addOverride(Component comp, Date rangeStart, Date rangeEnd) {

        boolean modified = false;

        // Verify if component is an override
        if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null)
            return false;
        
        // First check to see that the appropriate properties are present.

        // We need a DTSTART.
        Date dtstart = getStartDate(comp);
        if (dtstart == null)
            return false;

        if(dtstart instanceof DateTime) {
            // convert to UTC if configured
            dtstart = convertToUTCIfNecessary((DateTime) dtstart);
            // adjust floating time if timezone is present
            dtstart = adjustFloatingDateIfNecessary(dtstart);
        }
        
        // We need either DTEND or DURATION.
        Date dtend = getEndDate(comp);
        if (dtend == null) {
            Dur duration;
            if (dtstart instanceof DateTime) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            dtend = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(dtstart), dtstart);
        } else {
            // Convert to UTC if needed
            if(dtend instanceof DateTime) {
                dtend = convertToUTCIfNecessary((DateTime) dtend);
                // Adjust floating end time if timezone present
                dtend = adjustFloatingDateIfNecessary(dtend);
                // Handle case where dtend is before dtstart, in which the duration
                // will be 0, since it is a timed event
                if(dtend.before(dtstart)) {
                    dtend = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(0, 0, 0, 0).getTime(dtstart), dtstart);
                }
            } else {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 1 day since its an all-day event
                if(dtend.before(dtstart)) {
                    dtend = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(1, 0, 0, 0).getTime(dtstart), dtstart);
                }
            }
        }

        // Now create the map entry
        Date riddt = getRecurrenceId(comp);
        if(riddt instanceof DateTime) {
            riddt = convertToUTCIfNecessary((DateTime) riddt);
            riddt = adjustFloatingDateIfNecessary(riddt);
        }
        
        boolean future = getRange(comp);

        Instance instance = new Instance(comp, dtstart, dtend, riddt, true,
                future);
        String key = instance.getRid().toString();

        // Replace the master instance if it exists
        if(containsKey(key)) {
            remove(key);
            modified = true;
        }
        
        // Add modification instance if its in the range
        if (dtstart.before(rangeEnd)
                && dtend.after(rangeStart)) {
            put(key, instance);
            modified = true;
        }

        // Handle THISANDFUTURE if present
        Range range = (Range) comp.getProperties().getProperty(
                Property.RECURRENCE_ID).getParameters().getParameter(
                Parameter.RANGE);

        // TODO Ignoring THISANDPRIOR
        if ((range != null) && Range.THISANDFUTURE.equals(range)) {

            // Policy - iterate over all the instances after this one, replacing
            // the original instance withg a version adjusted to match the
            // override component

            // We need to account for a time shift in the overridden component
            // by applying the same shift to the future instances
            boolean timeShift = (dtstart.compareTo(riddt) != 0);
            Dur offsetTime = (timeShift ? new Dur(riddt, dtstart) : null);
            Dur newDuration = (timeShift ? new Dur(dtstart, dtend) : null);

            // Get a sorted list rids so we can identify the starting location
            // for the override.  The starting position will be the rid after
            // the current rid, or in the case of no matching rid, the first
            // rid that is greater than the current rid.
            boolean containsKey = containsKey(key);
            TreeSet sortedKeys = new TreeSet(keySet());
            for (Iterator iter = sortedKeys.iterator(); iter.hasNext();) {
                String ikey = (String) iter.next();
                if (ikey.equals(key) || (!containsKey && ikey.compareTo(key)>0)) {
                    
                    if(containsKey && !iter.hasNext())
                        continue;
                    else if(containsKey)
                        ikey = (String) iter.next();
                    
                    boolean moreKeys = true;
                    boolean firstMatch = true;
                    while(moreKeys==true) {
                        
                        // The target key is already set for the first
                        // iteration, so for all other iterations
                        // get the next target key.
                        if(firstMatch)
                            firstMatch = false;
                        else
                            ikey = (String) iter.next();
                        
                        Instance oldinstance = (Instance) get(ikey);

                        // Do not override an already overridden instance
                        if (oldinstance.isOverridden())
                            continue;

                        // Determine start/end for new instance which may need
                        // to be offset by the start/end offset and adjusted for
                        // a new duration from the overridden component
                        Date originalstart = oldinstance.getRid();
                        Value originalvalue =
                            originalstart instanceof DateTime ?
                            Value.DATE_TIME : Value.DATE;


                        Date start = oldinstance.getStart();
                        Date end = oldinstance.getEnd();

                        if (timeShift) {
                            // Handling of overlapping overridden THISANDFUTURE
                            // components is not defined in 2445. The policy
                            // here is that a THISANDFUTURE override should
                            // override any previous THISANDFUTURE overrides. So
                            // we need to use the original start time for the
                            // instance being adjusted as the time that is
                            // shifted, and the original start time is geiven by
                            // its recurrence-id.
                            start = Dates.
                                getInstance(offsetTime.getTime(originalstart),
                                            originalvalue);
                            end = Dates.
                                getInstance(newDuration.getTime(start),
                                            originalvalue);
                        }

                        // Replace with new instance
                        Instance newinstance = new Instance(comp, start, end,
                                originalstart, false, false);
                        remove(ikey);
                        put(newinstance.getRid().toString(), newinstance);
                        modified = true;
                        
                        if(!iter.hasNext())
                            moreKeys = false;
                    }
                }
            }
        }
        
        return modified;
    }

    private Date getStartDate(Component comp) {
        DtStart prop = (DtStart) comp.getProperties().getProperty(
                Property.DTSTART);
        return (prop != null) ? prop.getDate() : null;
    }

    private Date getEndDate(Component comp) {
        DtEnd dtEnd = (DtEnd) comp.getProperties().getProperty(Property.DTEND);
        // No DTEND? No problem, we'll use the DURATION if present.
        if (dtEnd == null) {
            Date dtStart = getStartDate(comp);
            Duration duration = (Duration) comp.getProperties().getProperty(
                    Property.DURATION);
            if (duration != null) {
                dtEnd = new DtEnd(org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getDuration()
                        .getTime(dtStart), dtStart));
            }
        }
        return (dtEnd != null) ? dtEnd.getDate() : null;
    }

    private final Date getRecurrenceId(Component comp) {
        RecurrenceId rid = (RecurrenceId) comp.getProperties().getProperty(
                Property.RECURRENCE_ID);
        return (rid != null) ? rid.getDate() : null;
    }

    private final boolean getRange(Component comp) {
        RecurrenceId rid = (RecurrenceId) comp.getProperties().getProperty(
                Property.RECURRENCE_ID);
        if (rid == null)
            return false;
        Parameter range = rid.getParameters().getParameter(Parameter.RANGE);
        return (range != null) && "THISANDFUTURE".equals(range.getValue());
    }
    
    /**
     * If the InstanceList is configured to convert all date/times to UTC,
     * then convert the given DateTime instance into a UTC DateTime.
     */
    private DateTime convertToUTCIfNecessary(DateTime date) {
        // If date is not UTC and InstanceList is configured
        // to convert to UTC, then convert time to UTC
        if(!date.isUtc() && isUTC) {
            // If there is a timezone in the time, then use that
            if(date.getTimeZone()!=null) {
                try {
                    DateTime newDT = new DateTime(date.toString(),date.getTimeZone());
                    newDT.setUtc(true);
                    return newDT;
                } catch (ParseException e) {
                    // shouldnt occur
                }
            }
            // Otherwise use timezone that InstanceList is configured
            // with, or use the system default.
            else {
                try {
                    DateTime newDT = null;
                    if(timezone == null)
                        newDT = new DateTime(date.toString());
                    else
                        newDT = new DateTime(date.toString(),timezone);
                    newDT.setUtc(true);
                    return newDT;
                } catch (ParseException e) {
                   // shouldn't occur
                }
            }    
        }
        return date;
    }
    
    /**
     * Adjust startRange to account for instances that begin before the given
     * startRange, but end after. For example if you have a daily recurring event
     * at 8am lasting for an hour and your startRange is 8:01am, then you 
     * want to adjust the range back an hour to catch the instance that is
     * already occurring.
     */
    private Date adjustStartRangeIfNecessary(Date startRange, Date start, Dur dur) {
        // If startRange is not the event start, no adjustment necessary
        if(!startRange.after(start))
            return startRange;
        
        // Need to adjust startRange back one duration to account for instances
        // that occur before the startRange, but end after the startRange
        Dur negatedDur = dur.negate();
       
        Calendar cal = Dates.getCalendarInstance(startRange);
        cal.setTime(negatedDur.getTime(startRange));
        
        // Return new startRange only if it is before the original startRange 
        if(cal.getTime().before(startRange))
            return org.osaf.cosmo.calendar.util.Dates.getInstance(cal.getTime(), startRange);
        
        return startRange;
    }
    
    /**
     * Adjust a floating time if a timezone is present.  A floating time
     * is initially created with the default system timezone.  If a timezone
     * if present, we need to adjust the floating time to be in specified
     * timezone.  This allows a server in the US to return floating times for
     * a query made by someone whos timezone is in Australia.  If no timezone is
     * set for the InstanceList, then the system default timezone will be
     * used in floating time calculations.
     * 
     * What happens is a floating time will get converted into a 
     * date/time with a timezone.  This is ok for comparison and recurrence
     * generation purposes.  Note that Instances will get indexed as a UTC
     * date/time and for floating DateTimes, the the recurrenceId associated 
     * with the Instance loses its "floating" property.
     */
    private Date adjustFloatingDateIfNecessary(Date date) {
        if(timezone==null || ! (date instanceof DateTime))
            return date;
        
        DateTime dtDate = (DateTime) date;
        if(dtDate.isUtc() || dtDate.getTimeZone()!=null)
            return date;
        
        try {
            return new DateTime(dtDate.toString(), timezone);
        } catch (ParseException e) {
            throw new RuntimeException("error parsing date");
        }
        
    }
}
