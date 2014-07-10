package org.fenixedu.cms.domain;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.presentationTier.Action.publico.InquirieResultsBean;
import net.sourceforge.fenixedu.util.FenixConfigurationManager;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;

@ComponentType(type = "inquiriesResults", name = "InquiriesResults", description = "Inquirires Results of an Execution Course")
public class ExecutionCourseInquiriesResults extends ExecutionCourseInquiriesResults_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        InquirieResultsBean bean = new InquirieResultsBean(executionCourse);
        globalContext.put("hasAccess", Authenticate.isLogged() && Authenticate.getUser().getPerson() != null);
        globalContext.put("institutionAcronym", Unit.getInstitutionAcronym());
        globalContext.put("notAvailableMessage", notAvailableMessage(bean));
        globalContext.put("executionCourse", executionCourse);
        globalContext.put("inquiriesResultsBean", bean);
        globalContext.put("loginUrl", loginUrl(req));
    }

    private String notAvailableMessage(InquirieResultsBean bean) {
        return Optional.ofNullable(bean.getNewQucIsNotAvailableMessage()).orElseGet(() -> bean.getOldQucIsNotAvailableMessage());
    }
    
    private String loginUrl(HttpServletRequest request) {
        if (CoreConfiguration.casConfig().isCasEnabled()) {
            return CoreConfiguration.casConfig().getCasLoginUrl(request);
        } else {
            return FenixConfigurationManager.getConfiguration().getLoginPage();
        }
    }
}
