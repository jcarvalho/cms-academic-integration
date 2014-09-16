package org.fenixedu.cms.domain.unit;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.sourceforge.fenixedu.domain.organizationalStructure.Function;
import net.sourceforge.fenixedu.domain.organizationalStructure.PersonFunction;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

import com.google.common.collect.Maps;

@ComponentType(name = "Unit Organization", description = "Provides the organizational structure for this unit")
public class Organization extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        globalContext.put("unitBean", new UnitFunctionsBean(unit(page)));
    }

    public static class UnitFunctionsBean {
        private final Unit unit;

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

        public Stream<UnitFunctionsBean> getSubunitBeans() {
            return getUnit().getSubUnits().stream().map(UnitFunctionsBean::new);
        }

        public Unit getUnit() {
            return unit;
        }
    }
}
