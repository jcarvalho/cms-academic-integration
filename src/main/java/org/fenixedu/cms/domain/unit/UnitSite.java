package org.fenixedu.cms.domain.unit;

import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.i18n.LocalizedString;

import pt.ist.fenixframework.DomainObject;

public class UnitSite extends UnitSite_Base {

    public UnitSite(Unit unit) {
        super();
        setUnit(unit);
        setBennu(Bennu.getInstance());
    }

    @Override
    public LocalizedString getName() {
        return getUnit().getNameI18n().toLocalizedString();
    }

    @Override
    public LocalizedString getDescription() {
        return getUnit().getNameI18n().toLocalizedString();
    }

    @Override
    public DomainObject getObject() {
        return getUnit();
    }
}
