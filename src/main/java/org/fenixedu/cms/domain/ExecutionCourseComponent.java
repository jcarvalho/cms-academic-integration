package org.fenixedu.cms.domain;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

@ComponentType(type = "executionCourse", name = "ExecutionCourse", description = "Provides the Execution Course information")
public class ExecutionCourseComponent extends ExecutionCourseComponent_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("executionCourse", executionCourse);
    }
    
}
