/**
 * <p>Title: FundCount, LLC</p>
 * <p>Description: FundCount project</p>
 * <p>Copyright: Copyright (c) 2001-2013 Fundcount, LLC</p>
 * <p>Company: FundCount, LLC</p>
 */
package org.traccar.web.server.model;

import static org.junit.Assert.*;

import static java.util.Calendar.*;

import org.junit.Test;
import org.traccar.web.shared.model.Position;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NotificationServiceTest {
    NotificationServiceImpl.NotificationSender sender = new NotificationServiceImpl.NotificationSender();

    Position position(String date) {
        Position position = new Position();
        try {
            position.setTime(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return position;
    }

    @Test
    public void testTimeFrameSameTimeZone() {
        Position pos = position("01/01/2018 11:00:00");
        assertTrue(sender.isTimeFrameOk(pos, "", TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, null, TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, "11am-11am", TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, "11am-11:01am", TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, "10:59am-11am", TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, "9am-10am,11am-11:30am", TimeZone.getDefault()));
        assertFalse(sender.isTimeFrameOk(pos, "9am-10am,11:01am-11:30am", TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, "11am-1pm", TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, "11am-12pm", TimeZone.getDefault()));
        assertTrue(sender.isTimeFrameOk(pos, "11am-11:59pm", TimeZone.getDefault()));
    }

    Position position(String date, TimeZone timeZone) {
        Position position = new Position();
        try {
            Date parsed = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(date);
            Calendar cLocal = Calendar.getInstance();
            cLocal.setTime(parsed);
            Calendar cTZ = Calendar.getInstance(timeZone);
            cTZ.set(cLocal.get(YEAR), cLocal.get(MONTH), cLocal.get(DAY_OF_MONTH),
                    cLocal.get(HOUR_OF_DAY), cLocal.get(MINUTE), cLocal.get(SECOND));
            position.setTime(cTZ.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return position;
    }

    @Test
    public void testTimeFrameDifferentTimeZone() {
        TimeZone tzGMT = TimeZone.getTimeZone("GMT");
        TimeZone tzGMTMinus1 = TimeZone.getTimeZone("GMT-1");
        TimeZone tzGMTPlus1 = TimeZone.getTimeZone("GMT+1");
        Position pos = position("02/28/2018 00:00:00", tzGMT);
        assertTrue(sender.isTimeFrameOk(pos, "0am-0am", tzGMT));
        assertFalse(sender.isTimeFrameOk(pos, "0am-0am", tzGMTMinus1));
        assertFalse(sender.isTimeFrameOk(pos, "0am-10:59pm", tzGMTMinus1));
        assertTrue(sender.isTimeFrameOk(pos, "1am-11pm", tzGMTMinus1));
        assertTrue(sender.isTimeFrameOk(pos, "11pm-11pm", tzGMTMinus1));
        assertTrue(sender.isTimeFrameOk(pos, "11pm-11:55pm", tzGMTMinus1));
        assertFalse(sender.isTimeFrameOk(pos, "0am-0am", tzGMTPlus1));
        assertFalse(sender.isTimeFrameOk(pos, "11pm-11:55pm", tzGMTPlus1));
        assertFalse(sender.isTimeFrameOk(pos, "11am-0am", tzGMTPlus1));
        assertTrue(sender.isTimeFrameOk(pos, "0am-11am", tzGMTPlus1));
        assertTrue(sender.isTimeFrameOk(pos, "1am-1am", tzGMTPlus1));

        pos = position("01/31/2018 14:20:28");
        assertTrue(sender.isTimeFrameOk(pos, "0:01am-11:59pm", TimeZone.getDefault()));
    }

    @Test
    public void testCourse() {
        Position pos = new Position();
        assertTrue(sender.isCourseOk(pos, null));
        assertTrue(sender.isCourseOk(pos, ""));
        assertTrue(sender.isCourseOk(pos, "100-150"));
        pos.setCourse(99d);
        assertFalse(sender.isCourseOk(pos, "100-150"));
        assertTrue(sender.isCourseOk(pos, "99-99"));
        assertTrue(sender.isCourseOk(pos, "99-100"));
        assertTrue(sender.isCourseOk(pos, "98-99"));
    }

    @Test
    public void testDayOfWeek() {
        Position pos = position("01/01/2018 00:00:00");
        assertTrue(sender.isDayOfWeekOk(pos, "Mon", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon-Mon", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon-Tue", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon-Wed", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon-Thu", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon-Fri", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon-Sat", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon-Sun", TimeZone.getDefault()));
        assertFalse(sender.isDayOfWeekOk(pos, "Tue", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Mon,Tue", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "0,1", TimeZone.getDefault()));
        assertTrue(sender.isDayOfWeekOk(pos, "Tue,1", TimeZone.getDefault()));

        TimeZone tzGMT = TimeZone.getTimeZone("GMT");
        TimeZone tzGMTMinus1 = TimeZone.getTimeZone("GMT-1");
        TimeZone tzGMTPlus1 = TimeZone.getTimeZone("GMT+1");
        pos = position("01/01/2018 00:00:00", tzGMT);
        assertTrue(sender.isDayOfWeekOk(pos, "1", tzGMT));
        assertFalse(sender.isDayOfWeekOk(pos, "1", tzGMTMinus1));
        assertTrue(sender.isDayOfWeekOk(pos, "1", tzGMTPlus1));

        pos = position("01/07/2018 01:00:00", tzGMT);
        assertTrue(sender.isDayOfWeekOk(pos, "7", tzGMT));
        assertTrue(sender.isDayOfWeekOk(pos, "7", tzGMTMinus1));
        assertTrue(sender.isDayOfWeekOk(pos, "7", tzGMTPlus1));
        assertTrue(sender.isDayOfWeekOk(pos, "0", tzGMT));
        assertTrue(sender.isDayOfWeekOk(pos, "0", tzGMTMinus1));
        assertTrue(sender.isDayOfWeekOk(pos, "0", tzGMTPlus1));

        pos = position("01/31/2018 14:20:28");
        assertTrue(sender.isDayOfWeekOk(pos, "1-3", TimeZone.getDefault()));
    }
}
