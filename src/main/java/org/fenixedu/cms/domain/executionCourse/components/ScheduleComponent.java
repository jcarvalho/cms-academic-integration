package org.fenixedu.cms.domain.executionCourse.components;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.dataTransferObject.InfoLessonInstanceAggregation;
import net.sourceforge.fenixedu.domain.Coordinator;
import net.sourceforge.fenixedu.domain.CourseLoad;
import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Shift;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.util.PeriodState;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;
import org.fenixedu.cms.domain.executionCourse.LessonBean;

import com.google.common.collect.Lists;

@ComponentType(type = "schedule", name = "Schedule", description = "Schedule of an execution course")
public class ScheduleComponent extends ScheduleComponent_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        boolean hasPermissionToViewSchedule = hasPermissionToViewSchedule(executionCourse);

        globalContext.put("executionCourse", executionCourse);
        globalContext.put("hasPermissionToViewSchedule", hasPermissionToViewSchedule);
        if (hasPermissionToViewSchedule) {
            List<LessonBean> lessons = getInfoLessons(executionCourse);

            globalContext.put("minHour", minHour(lessons));
            globalContext.put("maxHour", maxHour(lessons));

            globalContext.put("schedule", lessons);
        }
    }

    private int minHour(List<LessonBean> lessonBeans) {
        return lessonBeans.stream().map(LessonBean::getBeginHour).min(Comparator.naturalOrder()).orElse(8);
    }

    private int maxHour(List<LessonBean> lessonBeans) {
        return lessonBeans.stream().map(LessonBean::getEndHour).max(Comparator.naturalOrder()).orElse(24);
    }

    private List<LessonBean> getInfoLessons(ExecutionCourse executionCourse) {
        final List<LessonBean> lessons = Lists.newArrayList();
        for (final CourseLoad courseLoad : executionCourse.getCourseLoadsSet()) {
            for (final Shift shift : courseLoad.getShiftsSet()) {
                for (final InfoLessonInstanceAggregation infoLesson : InfoLessonInstanceAggregation.getAggregations(shift)) {
                    lessons.add(new LessonBean(infoLesson));
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

}
