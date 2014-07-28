package org.fenixedu.cms.domain.researchUnit;

import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;

public class ResearchUnitSite extends ResearchUnitSite_Base {
    public ResearchUnitSite(ResearchUnit researchUnit) {
        setResearchUnit(researchUnit);
    }

    @Override
    public void delete() {
        this.setResearchUnit(null);
        super.delete();
    }
}
