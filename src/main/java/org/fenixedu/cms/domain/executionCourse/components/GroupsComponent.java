package org.fenixedu.cms.domain.executionCourse.components;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

@ComponentType(name = "Groups", description = "Groups for an Execution Course")
public class GroupsComponent extends GroupsComponent_Base {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("groupings", executionCourse.getGroupings());
    }
    
}
