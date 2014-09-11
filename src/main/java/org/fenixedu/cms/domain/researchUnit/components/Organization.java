package org.fenixedu.cms.domain.researchUnit.components;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import net.sourceforge.fenixedu.domain.organizationalStructure.Function;
import net.sourceforge.fenixedu.domain.organizationalStructure.PersonFunction;
import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.CMSComponent;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.researchUnit.ResearchUnitSite;

import com.google.common.collect.Maps;

public class Organization implements CMSComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ResearchUnit researchUnit = ((ResearchUnitSite) page.getSite()).getResearchUnit();
        globalContext.put("unitBean", new UnitFunctionsBean(researchUnit));
    }

    public static class UnitFunctionsBean {
        private Unit unit;

        public UnitFunctionsBean(Unit unit) {
            this.unit = unit;
        }

        public Map<Function, List<PersonFunction>> getPersonFunctionsByFunction() {
            Map<Function, List<PersonFunction>> personFunctionsByFunction = Maps.newHashMap();
            for (Function function : getUnit().getOrderedActiveFunctions()) {
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
