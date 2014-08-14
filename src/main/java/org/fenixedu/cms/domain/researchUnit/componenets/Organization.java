package org.fenixedu.cms.domain.researchUnit.componenets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sourceforge.fenixedu.domain.organizationalStructure.Function;
import net.sourceforge.fenixedu.domain.organizationalStructure.PersonFunction;
import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.researchUnit.ResearchUnitSite;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.*;

public class Organization extends Organization_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
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
