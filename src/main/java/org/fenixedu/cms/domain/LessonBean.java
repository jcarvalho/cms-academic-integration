package org.fenixedu.cms.domain;

import net.sourceforge.fenixedu.dataTransferObject.InfoLessonInstanceAggregation;

import com.google.common.collect.ComparisonChain;

public class LessonBean implements Comparable<LessonBean> {
    private static final String[] COLORS = new String[] { "#FF9999", "#FFCC99", "#FFFF99", "#CCFF99", "#99FF99", "#99FFFF",
            "CCAE87", "997649", "FFE8E0", "BECC87" };

    private final InfoLessonInstanceAggregation info;

    LessonBean(InfoLessonInstanceAggregation info) {
        this.info = info;
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

    public String getSpaceUrl() {
        //TODO!
        return "#";
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

}
