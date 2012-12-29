/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wuliang;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wuliang.caldav.xml.TimeRange;

/**
 *
 * @author ricky
 */
public class FreeBusyQueryReportInfo extends ReportInfo {

    TimeRange timeRange = null;

    public FreeBusyQueryReportInfo(TimeRange timeRange) {
        super(FreeBusyQueryReport.FREE_BUSY_QUERY, DavConstants.DEPTH_1, null);
        this.timeRange = timeRange;
    }

    @Override
    public Element toXml(Document document) {
        // create calendar-multiget element
        Element freeBusyQuery = DomUtil.createElement(document,
                FreeBusyQueryReport.FREE_BUSY_QUERY.getLocalName(),
                FreeBusyQueryReport.FREE_BUSY_QUERY.getNamespace());
        freeBusyQuery.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(),
                    Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(),
                    DavConstants.NAMESPACE.getURI());
        // append time range
        freeBusyQuery.appendChild(timeRange.toXml(document));
        return freeBusyQuery;
    }
}
