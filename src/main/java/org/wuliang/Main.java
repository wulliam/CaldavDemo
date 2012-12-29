/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wuliang;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.log4j.BasicConfigurator;
import org.wuliang.caldav.xml.Comp;
import org.wuliang.caldav.xml.Filter;
import org.wuliang.caldav.xml.Prop;
import org.wuliang.caldav.xml.RequestCalendarData;
import org.wuliang.caldav.xml.TimeRange;

/**
 *
 * @author ricky
 */
public class Main {

    static public Header[] doOptions(HttpClient client, String uri
            ) throws IOException {
        // make an options call
        OptionsMethod options = null;
        try {
            options = new OptionsMethod(uri);
            client.executeMethod(options);
            System.err.println(options.getStatusLine());
            return options.getResponseHeaders();
        } finally {
            if (options != null) {
                options.releaseConnection();
            }
        }
    }

    static public MultiStatus doPropFind(HttpClient client, String uri
            ) throws IOException, DavException {
        // make a propfind call
        PropFindMethod propFind = null;
        try {
            propFind = new PropFindMethod(uri,
                    DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_0);
            client.executeMethod(propFind);
            MultiStatus multiStatus = propFind.getResponseBodyAsMultiStatus();
            return multiStatus;
        } finally {
            if (propFind != null) {
                propFind.releaseConnection();
            }
        }
    }

    static public String doCreateEvent(
            HttpClient client, String uri) throws IOException {
        // create a new EVENT
        PutMethod put = null;
        try {
            UUID uuid = UUID.randomUUID();
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar c = new net.fortuna.ical4j.model.Calendar();
            c.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
            c.getProperties().add(Version.VERSION_2_0);
            c.getProperties().add(CalScale.GREGORIAN);
            TimeZoneRegistry registry = builder.getRegistry();
            VTimeZone tz = registry.getTimeZone("Europe/Madrid").getVTimeZone();
            c.getComponents().add(tz);
            VEvent vevent = new VEvent(new net.fortuna.ical4j.model.Date(),
                    new Dur(0, 1, 0, 0), "test");
            vevent.getProperties().add(new Uid(uuid.toString()));
            c.getComponents().add(vevent);
            String href = uri + uuid.toString() + ".ics";
            put = new PutMethod(href);
            put.addRequestHeader("If-None-Match", "*");
            put.setRequestEntity(new StringRequestEntity(c.toString(), "text/calendar", "UTF-8"));
            client.executeMethod(put);
            return href;
        } finally {
            if (put != null) {
                put.releaseConnection();
            }
        }
    }

     static public void doDeleteEvent(
            HttpClient client, String href) throws IOException {
         DeleteMethod delete = null;
         try {
             delete = new DeleteMethod(href);
             client.executeMethod(delete);
         } finally {
             if (delete != null) {
                delete.releaseConnection();
            }
         }
     }
    
    static public MultiStatus doCalendarQueryReport(HttpClient client, String uri
            ) throws IOException, DavException {
        // get events of the calendar (calendar-query)
        ReportMethod calendarQuery = null;
        try {
            RequestCalendarData calendarData = new RequestCalendarData();
            Filter filter = new Filter("VCALENDAR");
            filter.getCompFilter().add(new Filter("VEVENT"));
            Calendar start = Calendar.getInstance();
            start.add(Calendar.MONTH, -1);
            Calendar end = Calendar.getInstance();
            filter.getCompFilter().get(0).setTimeRange(
                    new TimeRange(start.getTime(), end.getTime()));
            ReportInfo reportInfo = new CalendarQueryReportInfo(calendarData,
                    filter);
            calendarQuery = new ReportMethod(uri, reportInfo);
            client.executeMethod(calendarQuery);
            MultiStatus multiStatus = calendarQuery.getResponseBodyAsMultiStatus();
            return multiStatus;
        } finally {
            if (calendarQuery != null) {
                calendarQuery.releaseConnection();
            }
        }
    }

    static public MultiStatus doCalendarMultiGetReport(HttpClient client, String uri,
            String href) throws IOException, DavException, ParserConfigurationException {
        ReportMethod report = null;
        try {
            // append props GETTETAG
            DavPropertyNameSet props = new DavPropertyNameSet();
            props.add(DavPropertyName.GETETAG);
            // append calendar-data
            RequestCalendarData calendarData = new RequestCalendarData();
            Comp vcalendar = new Comp("VCALENDAR");
            //vcalendar.getProp().add(new Prop("VERSION"));
            vcalendar.getComp().add(new Comp("VEVENT"));
            vcalendar.getComp().get(0).getProp().add(new Prop("SUMMARY"));
            vcalendar.getComp().get(0).getProp().add(new Prop("UID"));
            vcalendar.getComp().get(0).getProp().add(new Prop("DTSTART"));
            vcalendar.getComp().get(0).getProp().add(new Prop("DTEND"));
            vcalendar.getComp().get(0).getProp().add(new Prop("DESCRIPTION"));
            //vcalendar.getComp().get(0).getProp().add(new Prop("DURATION"));
            //vcalendar.getComp().get(0).getProp().add(new Prop("RRULE"));
            //vcalendar.getComp().get(0).getProp().add(new Prop("RDATE"));
            //vcalendar.getComp().get(0).getProp().add(new Prop("EXRULE"));
            //vcalendar.getComp().get(0).getProp().add(new Prop("EXDATE"));
            //vcalendar.getComp().get(0).getProp().add(new Prop("RECURRENCE-ID"));
            //vcalendar.getComp().add(new Comp("VTIMEZONE"));
            calendarData.setComp(vcalendar);
            // create the report
            ReportInfo reportInfo = new CalendarMultiGetReportInfo(props, 
                    calendarData, new String[]{href});
            report = new ReportMethod(uri, reportInfo);
            client.executeMethod(report);
            MultiStatus multiStatus = report.getResponseBodyAsMultiStatus();
            return multiStatus;
        } finally {
            if (report != null) {
                report.releaseConnection();
            }
        }
    }

    static public net.fortuna.ical4j.model.Calendar doFreeBusyQueryReport(HttpClient client, String uri
            ) throws IOException, DavException, ParserConfigurationException, ParserException {
        // get events of the calendar (calendar-query)
        ReportMethod freeBusyQuery = null;
        net.fortuna.ical4j.model.Calendar c  = null;
        try {
            Calendar start = Calendar.getInstance();
            start.add(Calendar.MONTH, -1);
            Calendar end = Calendar.getInstance();
            TimeRange timeRange = new TimeRange(start.getTime(), end.getTime());
            ReportInfo reportInfo = new FreeBusyQueryReportInfo(timeRange);
            freeBusyQuery = new ReportMethod(uri, reportInfo);
            client.executeMethod(freeBusyQuery);
            if (freeBusyQuery.getStatusCode() == DavServletResponse.SC_OK) {
                CalendarBuilder builder = new CalendarBuilder();
                c = builder.build(freeBusyQuery.getResponseBodyAsStream());
            }
        } finally {
            if (freeBusyQuery != null) {
                freeBusyQuery.releaseConnection();
            }
        }
        return c;
    }

    static public void main(String[] args) throws Exception {
        //BasicConfigurator.configure();
        args = new String[]{"https://caldav.calendar.yahoo.com/dav/wuliang_org@yahoo.com/Calendar/Wu_Liang/",
        "username","password"};
        if (args.length != 3) {
            System.err.println("USAGE: java sample.caldav.Main URL username password");
            System.err.println("  URL:      CalDAV URL of the server.");
            System.err.println("  username: cadav username.");
            System.err.println("  password: password of the user.");
            return;
        }
        String uri = args[0];
        String username = args[1];
        String password = args[2];
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(uri);
        // define connection manager
        HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        int maxHostConnections = 20;
        params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
        connectionManager.setParams(params);
        // define the HttpClient with user and password
        HttpClient client = new HttpClient(connectionManager);
        client.setHostConfiguration(hostConfig);
        Credentials creds = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(AuthScope.ANY, creds);
        // options
        System.err.println("Sending OPTIONS...");
        Header[] headers = doOptions(client, uri);
        for (int i = 0; i < headers.length; i++) {
            System.err.println(headers[i].getName() + ": "
                    + headers[i].getValue());
        }
        // prop-find
        System.err.println("Sending PROPFIND...");
        MultiStatus multiStatus = doPropFind(client, uri);
        for (int i = 0; i < multiStatus.getResponses().length; i++) {
            MultiStatusResponse multiRes = multiStatus.getResponses()[i];
            System.err.println(multiRes.getHref());
        }
        // create a EVENT
        System.err.println("Sending PUT...");
        String created = doCreateEvent(client, uri);
        System.err.println("Created resource: " + created);
        // calendar-multiget to get the previously created Event
        System.err.println("Sending REPORT calendar-multiget...");
        multiStatus = doCalendarMultiGetReport(client, uri, created);
        for (int i = 0; i < multiStatus.getResponses().length; i++) {
            MultiStatusResponse multiRes = multiStatus.getResponses()[i];
            String href = multiRes.getHref();
            DavPropertySet propSet = multiRes.getProperties(DavServletResponse.SC_OK);
            DavProperty<String> prop = (DavProperty<String>) propSet.get(
                    CalDavConstants.CALDAV_XML_CALENDAR_DATA, CalDavConstants.CALDAV_NAMESPACE);
            System.err.println("HREF: " + href);
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar c = builder.build(new StringReader(prop.getValue()));
            System.err.println("calendar-data: " + c.toString());
        }
        // calendar-query
        System.err.println("Sending REPORT calendar-query...");
        multiStatus = doCalendarQueryReport(client, uri);
        for (int i = 0; i < multiStatus.getResponses().length; i++) {
            MultiStatusResponse multiRes = multiStatus.getResponses()[i];
            String href = multiRes.getHref();
            DavPropertySet propSet = multiRes.getProperties(DavServletResponse.SC_OK);
            DavProperty<String> prop = (DavProperty<String>) propSet.get(
                    CalDavConstants.CALDAV_XML_CALENDAR_DATA, CalDavConstants.CALDAV_NAMESPACE);
            System.err.println("HREF: " + href);
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar c = builder.build(new StringReader(prop.getValue()));
            System.err.println("calendar-data: " + c.toString());
        }
        // free-busy-report
        System.err.println("Sending REPORT free-busy-report...");
        net.fortuna.ical4j.model.Calendar c = doFreeBusyQueryReport(client, uri);
        System.err.println("free-busy-query: " + c.toString());
        Component comp = c.getComponent(Component.VFREEBUSY);
        PropertyList list = comp.getProperties(Property.FREEBUSY);
        for (FreeBusy fb : (List<FreeBusy>) list) {
            System.err.println(fb.getParameter(Parameter.FBTYPE).getValue());
            PeriodList plist = fb.getPeriods();
            for (Period per : (Set<Period>) plist) {
                System.err.println(per.getStart() + "-" + per.getEnd());
                System.err.println(per.getDuration());
            }
        }
        // delete
        System.err.println("Sending DELETE...");
        doDeleteEvent(client, created);
    }
}
