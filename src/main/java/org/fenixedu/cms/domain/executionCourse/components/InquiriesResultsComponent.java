package org.fenixedu.cms.domain.executionCourse.components;

import java.util.Optional;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.presentationTier.Action.publico.InquirieResultsBean;
import net.sourceforge.fenixedu.util.FenixConfigurationManager;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.CMSComponent;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

@ComponentType(name = "InquiriesResults", description = "Inquirires Results of an Execution Course")
public class InquiriesResultsComponent implements CMSComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        InquirieResultsBean bean = new InquirieResultsBean(executionCourse);
        globalContext.put("hasAccess", Authenticate.isLogged() && Authenticate.getUser().getPerson() != null);
        globalContext.put("institutionAcronym", Unit.getInstitutionAcronym());
        globalContext.put("notAvailableMessage", notAvailableMessage(bean));
        globalContext.put("executionCourse", executionCourse);
        globalContext.put("inquiriesResultsBean", bean);
        globalContext.put("loginUrl", loginUrl());
    }

    private String notAvailableMessage(InquirieResultsBean bean) {
        return Optional.ofNullable(bean.getNewQucIsNotAvailableMessage()).orElseGet(() -> bean.getOldQucIsNotAvailableMessage());
    }

    private String loginUrl() {
        if (CoreConfiguration.casConfig().isCasEnabled()) {
            return CoreConfiguration.casConfig().getCasLoginUrl();
        } else {
            return FenixConfigurationManager.getConfiguration().getLoginPage();
        }
    }
}
