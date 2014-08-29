package org.fenixedu.cms.domain.department.componenets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.Department;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.thesis.Thesis;
import net.sourceforge.fenixedu.domain.thesis.ThesisState;
import net.sourceforge.fenixedu.presentationTier.Action.publico.ThesisFilterBean;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.department.DepartmentSite;
import pt.ist.fenixframework.FenixFramework;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ComponentType(name = "departmentTheses", description = "Theses information for a Department")
public class DepartmentTheses extends DepartmentTheses_Base {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Department department = ((DepartmentSite) page.getSite()).getDepartment();
        globalContext.put("department", department);
        globalContext.put("unit", department.getDepartmentUnit());
        SortedSet<ExecutionYear> years = Sets.newTreeSet();
        Map<String, Collection<Thesis>> theses = Maps.newHashMap();

        ThesisFilterBean bean = createFilterBean(department, globalContext);

        collectTheses(bean);

        globalContext.put("filter", bean);
        globalContext.put("years", years);
        globalContext.put("theses", theses);
    }

    protected Map<ExecutionYear, List<Thesis>> collectTheses(ThesisFilterBean bean) {
        Predicate<Thesis> enrolmentFilter = thesis -> thesis.getEnrolment() != null;
        Predicate<Thesis> isFinalFilter = thesis -> !thesis.isFinalThesis() || thesis.isFinalAndApprovedThesis();
        Predicate<Thesis> stateFilter = thesis -> bean.getState() == null || thesis.getState() == bean.getState();
        Predicate<Thesis> executionYearFilter = thesis -> bean.getYear()==null || thesis.getExecutionYear() == bean.getYear();

        return bean.getDegree().getThesisSet().stream().filter(enrolmentFilter.or(isFinalFilter).or(stateFilter).or(executionYearFilter)).collect(
                Collectors.groupingBy(Thesis::getExecutionYear));
    }

    protected ThesisFilterBean createFilterBean(Department department, TemplateContext context) {
        ThesisFilterBean bean = new ThesisFilterBean();

        if(context.containsKey("degreeId")) {
            bean.setDegree(FenixFramework.getDomainObject((String) context.get("degreeID")));
        }

        if(context.containsKey("executionYearID")) {
            bean.setYear(FenixFramework.getDomainObject((String) context.get("executionYearID")));
        }

        bean.setState(ThesisState.valueOf((String) context.getOrDefault("thesisState", ThesisState.EVALUATED.getName())));

        bean.setDepartment(department);

        bean.setDegreeOptions(Lists.newArrayList(department.getDegreesSet()));

        return bean;
    }


}
