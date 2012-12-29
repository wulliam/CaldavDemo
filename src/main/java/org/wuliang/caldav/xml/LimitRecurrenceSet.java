/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wuliang.caldav.xml;

import java.util.Date;
import org.wuliang.CalDavConstants;

/**
 * <!ELEMENT limit-recurrence-set EMPTY>
 * <!ATTLIST limit-recurrence-set start CDATA #REQUIRED
 *                                end   CDATA #REQUIRED>
 *
 * @author ricky
 */
public class LimitRecurrenceSet extends StartEndRequiredData {

    public LimitRecurrenceSet(Date start, Date end) {
        super(CalDavConstants.CALDAV_XML_LIMIT_RECURRENCE_SET, start, end);
    }
    
}
