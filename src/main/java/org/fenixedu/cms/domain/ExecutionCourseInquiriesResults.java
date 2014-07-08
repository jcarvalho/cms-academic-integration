package org.fenixedu.cms.domain;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.presentationTier.Action.publico.InquirieResultsBean;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.security.Authenticate;

public class ExecutionCourseInquiriesResults extends ExecutionCourseInquiriesResults_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        InquirieResultsBean bean = new InquirieResultsBean(executionCourse);
        globalContext.put("hasAccess", Authenticate.isLogged() && Authenticate.getUser().getPerson() != null);
        globalContext.put("institutionAcronym", Unit.getInstitutionAcronym());
        globalContext.put("notAvailableMessage", notAvailableMessage(bean));
        globalContext.put("inquiriesResultsBean", bean);
    }

    private String notAvailableMessage(InquirieResultsBean bean) {
        return Optional.ofNullable(bean.getNewQucIsNotAvailableMessage()).orElseGet(() -> bean.getOldQucIsNotAvailableMessage());
    }

}
