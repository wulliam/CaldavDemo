/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wuliang.caldav.xml;

import java.util.Date;
import org.wuliang.CalDavConstants;

/**
 * <!ELEMENT limit-freebusy-set EMPTY>
 * <!ATTLIST limit-freebusy-set start CDATA #REQUIRED
 *                              end   CDATA #REQUIRED>
 *
 * @author ricky
 */
public class LimitFreeBusySet extends StartEndRequiredData {

    public LimitFreeBusySet(Date start, Date end) {
        super(CalDavConstants.CALDAV_XML_LIMIT_FREEBUSY_SET, start, end);
    }

}
