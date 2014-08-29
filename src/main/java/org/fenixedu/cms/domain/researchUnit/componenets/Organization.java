package org.fenixedu.cms.domain.researchUnit.componenets;

import com.google.common.collect.Maps;
import net.sourceforge.fenixedu.domain.organizationalStructure.Function;
import net.sourceforge.fenixedu.domain.organizationalStructure.PersonFunction;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.department.DepartmentSite;
import org.fenixedu.cms.domain.researchUnit.ResearchUnitSite;

import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.*;

public class Organization extends Organization_Base {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        //temporary hack until unit sites refactorization!!!
        Unit unit = null;
        if(page.getSite() instanceof ResearchUnitSite) {
            unit = ((ResearchUnitSite) page.getSite()).getResearchUnit();
        } else if(page.getSite() instanceof DepartmentSite) {
            unit = ((DepartmentSite) page.getSite()).getDepartment().getDepartmentUnit();
        }
        globalContext.put("unitBean", new UnitFunctionsBean(unit));
    }

    public static class UnitFunctionsBean {
        private Unit unit;

        public UnitFunctionsBean(Unit unit) {
            this.unit = unit;
        }

        public Map<Function, List<PersonFunction>> getPersonFunctionsByFunction() {
            Map<Function, List<PersonFunction>> personFunctionsByFunction = Maps.newHashMap();
            for(Function function : getUnit().getOrderedActiveFunctions()) {
                personFunctionsByFunction.put(function, function.getActivePersonFunctions());
            }
            return personFunctionsByFunction;
        }

        public List<UnitFunctionsBean> getSubunitBeans() {
            return getUnit().getSubUnits().stream().map(UnitFunctionsBean::new).collect(toList());
        }

        public Unit getUnit() {
            return unit;
        }
    }
}
