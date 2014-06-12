package org.fenixedu.cms.domain;

import java.util.List;

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
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;

import com.google.common.collect.Lists;

@ComponentType(type = "schedule", name = "Schedule", description = "Schedule of an execution course")
public class ExecutionCourseSchedule extends ExecutionCourseSchedule_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = executionCourse(page.getSite());
        if (hasPermissionToViewSchedule(executionCourse)) {
            componentContext.put("schedule", getInfoLessons(executionCourse));
        }
    }

    private List<InfoLessonInstanceAggregation> getInfoLessons(ExecutionCourse executionCourse) {
        final List<InfoLessonInstanceAggregation> infoLessons = Lists.newArrayList();
        for (final CourseLoad courseLoad : executionCourse.getCourseLoadsSet()) {
            for (final Shift shift : courseLoad.getShiftsSet()) {
                infoLessons.addAll(InfoLessonInstanceAggregation.getAggregations(shift));
            }
        }
        return infoLessons;
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

    public ExecutionCourse executionCourse(Site site) {
        return site instanceof ExecutionCourseSite ? ((ExecutionCourseSite) site).getExecutionCourse() : null;
    }

}
