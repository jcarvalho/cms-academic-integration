package org.fenixedu.cms.domain.executionCourse.components;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.fenixedu.dataTransferObject.InfoLessonInstanceAggregation;
import net.sourceforge.fenixedu.domain.Coordinator;
import net.sourceforge.fenixedu.domain.CourseLoad;
import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Shift;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.util.PeriodState;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.CMSComponent;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;
import org.fenixedu.cms.domain.executionCourse.LessonBean;

import com.google.common.collect.Lists;

@ComponentType(name = "Schedule", description = "Schedule of an execution course")
public class ScheduleComponent implements CMSComponent {

    private static final int MAX_HOUR = 23;
    private static final int MIN_HOUR = 8;

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        boolean hasPermissionToViewSchedule = hasPermissionToViewSchedule(executionCourse);

        globalContext.put("executionCourse", executionCourse);
        globalContext.put("hasPermissionToViewSchedule", hasPermissionToViewSchedule);
        if (hasPermissionToViewSchedule) {
            List<LessonBean> lessons = getInfoLessons(executionCourse);

            globalContext.put("minHour", hourMinuteSecond(minHour(lessons), 0, 0));
            globalContext.put("maxHour", hourMinuteSecond(maxHour(lessons), 0, 0));

            globalContext.put("schedule", lessons);
        }
    }

    private int minHour(List<LessonBean> lessonBeans) {
        int min = lessonBeans.stream().map(LessonBean::getBeginHour).min(Comparator.naturalOrder()).orElse(MIN_HOUR);
        return Math.max(min - 1, MIN_HOUR);
    }

    private int maxHour(List<LessonBean> lessonBeans) {
        int max = lessonBeans.stream().map(LessonBean::getEndHour).max(Comparator.naturalOrder()).orElse(MAX_HOUR);
        return Math.min(max + 2, MAX_HOUR);
    }

    private List<LessonBean> getInfoLessons(ExecutionCourse executionCourse) {
        final List<LessonBean> lessons = Lists.newArrayList();
        for (final CourseLoad courseLoad : executionCourse.getCourseLoadsSet()) {
            for (final Shift shift : courseLoad.getShiftsSet()) {
                for (final InfoLessonInstanceAggregation infoLesson : InfoLessonInstanceAggregation.getAggregations(shift)) {
                    lessons.add(new LessonBean(infoLesson, executionCourse));
                }
            }
        }
        return lessons.stream().sorted().collect(Collectors.toList());
    }

    private boolean hasPermissionToViewSchedule(ExecutionCourse executionCourse) {
        if (executionCourse.getExecutionPeriod().getState() != PeriodState.NOT_OPEN) {
            return true;
        }

        if (!Authenticate.isLogged()) { //public access
            return false;
        }

        final User userview = Authenticate.getUser();
        if (userview.getPerson().hasRole(RoleType.RESOURCE_ALLOCATION_MANAGER)) { // allow gop to view
            return true;
        }

        for (Degree degree : executionCourse.getDegreesSortedByDegreeName()) {
            for (Coordinator coordinator : degree.getCurrentCoordinators()) {
                if (coordinator.getPerson().equals(userview.getPerson())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String hourMinuteSecond(int hour, int minute, int seconds) {
        return LocalTime.of(hour, minute, seconds).format(DateTimeFormatter.ISO_TIME);
    }
}
