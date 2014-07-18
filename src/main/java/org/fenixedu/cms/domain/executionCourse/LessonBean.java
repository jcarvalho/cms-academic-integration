package org.fenixedu.cms.domain.executionCourse;

import java.util.Calendar;
import java.util.TimeZone;

import net.sourceforge.fenixedu.dataTransferObject.InfoLessonInstanceAggregation;
import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.collect.ComparisonChain;

public class LessonBean implements Comparable<LessonBean> {
    private static final String[] COLORS = new String[] { "CCAE87", "997649", "FFE8E0", "BECC87", "#FF9999", "#FFCC99",
            "#FFFF99", "#CCFF99", "#99FF99", "#99FF88" };

    private final InfoLessonInstanceAggregation info;
    private final ExecutionCourse executionCourse;

    public LessonBean(InfoLessonInstanceAggregation info, ExecutionCourse executionCourse) {
        this.info = info;
        this.executionCourse = executionCourse;
    }

    public String getEnd() {
        return isoDate(getWeekDay(), getEndHour(), getEndMinutes(), 0);
    }

    public String getStart() {
        return isoDate(getWeekDay(), getBeginHour(), getBeginMinutes(), 0);
    }

    private String isoDate(int dayOfWeek, int hour, int minutes, int seconds) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        return ISODateTimeFormat.dateTime().print(calendar.getTime().getTime());
    }

    public String getId() {
        return info.getExternalId();
    }

    public int getWeekDay() {
        return info.getDiaSemana().getDiaSemana();
    }

    public int getBeginHour() {
        return info.getBeginHourMinuteSecond().getHour();
    }

    public int getBeginMinutes() {
        return info.getBeginHourMinuteSecond().getMinuteOfHour();
    }

    public int getEndHour() {
        return info.getEndHourMinuteSecond().getHour();
    }

    public int getEndMinutes() {
        return info.getEndHourMinuteSecond().getMinuteOfHour();
    }

    public String getShiftType() {
        return info.getShift().getShiftTypesPrettyPrint();
    }

    public String getShiftTypeCode() {
        return info.getShift().getShiftTypesCodePrettyPrint();
    }

    public String getShiftSpace() {
        return info.getAllocatableSpace().getName();
    }

    public String getShiftWeeks() {
        return info.prettyPrintWeeks();
    }

    public InfoLessonInstanceAggregation getInfo() {
        return info;
    }

    public String getTextColor() {
        return "#333";
    }

    public String getSpaceUrl() {
        return spaceUrl(this.info, this.executionCourse);
    }

    public String getColor() {
        int id = info.getShift().getTypes().stream().findFirst().get().ordinal();
        return COLORS[id % COLORS.length];
    }

    @Override
    public int compareTo(LessonBean o) {
        return ComparisonChain.start().compare(this.getWeekDay(), o.getWeekDay()).compare(this.getBeginHour(), o.getBeginHour())
                .compare(this.getBeginMinutes(), o.getBeginMinutes()).result();
    }

    public String spaceUrl(InfoLessonInstanceAggregation info, ExecutionCourse executionCourse) {
        final StringBuilder strBuffer = new StringBuilder();
        final String context = CoreConfiguration.getConfiguration().applicationUrl();
        final String roomName = info.getAllocatableSpace().getName();
        final String executionPeriod = executionCourse.getExecutionPeriod().getExternalId();

        strBuffer.append(context).append("/publico/").append("siteViewer.do?method=roomViewer&amp;roomName=").append(roomName);
        strBuffer.append("&amp;objectCode=").append(executionPeriod).append("&amp;executionPeriodOID=").append(executionPeriod);

        return strBuffer.toString();
    }

}
