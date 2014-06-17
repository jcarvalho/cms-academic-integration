package org.fenixedu.cms.domain;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

public class ExecutionCourseComponent extends ExecutionCourseComponent_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        componentContext.put("executionCourse", executionCourse);
    }
    
}
