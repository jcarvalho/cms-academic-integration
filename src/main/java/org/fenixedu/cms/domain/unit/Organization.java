package org.fenixedu.cms.domain.unit;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.fenixedu.domain.organizationalStructure.Function;
import net.sourceforge.fenixedu.domain.organizationalStructure.PersonFunction;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import com.google.common.collect.Maps;
import org.joda.time.YearMonthDay;

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
            return getPersonFunctionsByFunction(getUnit());
        }

        public Stream<UnitFunctionsBean> getSubunitBeans() {
            Predicate<Unit> hasPersons = subunit->!getPersonFunctionsByFunction(subunit).isEmpty();
            return getUnit().getActiveSubUnits(new YearMonthDay()).stream().filter(hasPersons).map(UnitFunctionsBean::new);
        }

        private Map<Function, List<PersonFunction>> getPersonFunctionsByFunction(Unit unit) {
            return unit.getOrderedActiveFunctions().stream().flatMap(function->function.getActivePersonFunctions().stream())
                    .collect(Collectors.groupingBy(personFunction->personFunction.getFunction()));
        }

        public Unit getUnit() {
            return unit;
        }
    }
}
