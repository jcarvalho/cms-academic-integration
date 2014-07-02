package org.fenixedu.cms.domain;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Grouping;
import net.sourceforge.fenixedu.domain.StudentGroup;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

import pt.ist.fenixframework.FenixFramework;

public class ExecutionCourseGrouping extends ExecutionCourseGrouping_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        if (hasGrouping(req)) {
            globalContext.put("grouping", grouping(req, executionCourse));
        }
        if (hasStudentGrouping(req)) {
            globalContext.put("studentGroup", studentGroup(req));
        }
    }

    protected StudentGroup studentGroup(final HttpServletRequest request) {
        return FenixFramework.getDomainObject(request.getParameter("studentGroupID"));
    }

    private Grouping grouping(final HttpServletRequest req, final ExecutionCourse executionCourse) {
        final String groupingID = req.getParameter("groupingID");
        return executionCourse.getExportGroupingsSet().stream().map(eg -> eg.getGrouping())
                .filter(g -> g.getExternalId().equals(groupingID)).findFirst().orElse(null);
    }

    private boolean hasGrouping(final HttpServletRequest req) {
        return req.getParameter("groupingID") != null;
    }

    private boolean hasStudentGrouping(HttpServletRequest req) {
        return req.getParameter("studentGroupID") != null;
    }

}
