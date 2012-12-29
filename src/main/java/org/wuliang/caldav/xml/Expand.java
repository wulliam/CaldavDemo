/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wuliang.caldav.xml;

import java.util.Date;
import org.wuliang.CalDavConstants;

/**
 * <!ELEMENT expand EMPTY>
 * <!ATTLIST expand start CDATA #REQUIRED
 *                  end   CDATA #REQUIRED>
 *
 * @author ricky
 */
public class Expand extends StartEndRequiredData {

    public Expand(Date start, Date end) {
        super(CalDavConstants.CALDAV_XML_EXPAND, start, end);
    }


}
