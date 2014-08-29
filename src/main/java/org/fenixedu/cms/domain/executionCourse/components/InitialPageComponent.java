package org.fenixedu.cms.domain.executionCourse.components;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

@ComponentType(type = "initialPage", name = "InitialPage",
        description = "Provides the information needed for the initial page of an Execution Course")
public class InitialPageComponent extends InitialPageComponent_Base {
    public final static int ANNOUNCEMENTS_TO_SHOW = 5;

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("professorships", executionCourse.getProfessorshipsSortedAlphabetically());
        globalContext.put("isStudent", isStudent(Authenticate.getUser()));
        globalContext.put("executionCourse", executionCourse);
    }
    
    private boolean isStudent(User user) {
        return user != null && user.getPerson() != null && user.getPerson().getStudent() != null;
    }
}
