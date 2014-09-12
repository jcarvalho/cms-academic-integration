package org.fenixedu.cms.domain.executionCourse.components;

import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.CMSComponent;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

@ComponentType(name = "ExecutionCourse", description = "Provides the Execution Course information")
public class ExecutionCourseComponent implements CMSComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("executionCourse", executionCourse);
    }

}
