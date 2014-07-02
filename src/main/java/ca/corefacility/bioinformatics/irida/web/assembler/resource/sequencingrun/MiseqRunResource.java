
package ca.corefacility.bioinformatics.irida.web.assembler.resource.sequencingrun;

import ca.corefacility.bioinformatics.irida.model.run.MiseqRun;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Resource class for a MiseqRun
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
@XmlRootElement(name = "miseqRun")
public class MiseqRunResource extends SequencingRunResource {
	
	MiseqRun miseqRun;

    public MiseqRunResource() {
        this(new MiseqRun());
    }

    public MiseqRunResource(MiseqRun miseqRun) {
        super(miseqRun);
        this.miseqRun = miseqRun;
    }
    
    @XmlElement
    public String getInvestigatorName() {
        return miseqRun.getInvestigatorName();
    }

    public void setInvestigatorName(String investigatorName) {
    	miseqRun.setInvestigatorName(investigatorName);
    }

    @XmlElement
    public String getProjectName() {
        return miseqRun.getProjectName();
    }

    public void setProjectName(String projectName) {
    	miseqRun.setProjectName(projectName);
    }

    @XmlElement
    public String getExperimentName() {
        return miseqRun.getExperimentName();
    }

    public void setExperimentName(String experimentName) {
    	miseqRun.setExperimentName(experimentName);
    }

    @XmlElement
    public String getWorkflow() {
        return miseqRun.getWorkflow();
    }

    public void setWorkflow(String workflow) {
    	miseqRun.setWorkflow(workflow);
    }

    @XmlElement
    public String getApplication() {
        return miseqRun.getApplication();
    }

    public void setApplication(String application) {
    	miseqRun.setApplication(application);
    }

    @XmlElement
    public String getAssay() {
        return miseqRun.getAssay();
    }

    public void setAssay(String assay) {
    	miseqRun.setAssay(assay);
    }

    @XmlElement
    public String getChemistry() {
        return miseqRun.getChemistry();
    }

    public void setChemistry(String chemistry) {
    	miseqRun.setChemistry(chemistry);
    }    
    
}
