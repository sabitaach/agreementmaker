package am.evaluation.repairExtended;

import org.semanticweb.owlapi.model.OWLAxiom;

public class AxiomRank {

	//private Integer AxiomId;
	private OWLAxiom Axiom;
	private Integer Rank;
	
	//public AxiomRank(OWLAxiom axiom, Integer rank, Integer axiomId){
	public AxiomRank(OWLAxiom axiom, Integer rank){
		
		//AxiomId = axiomId;
		Axiom = axiom;
		Rank = rank;
	}
	
	//get set	
	/*public void setAxiomId (Integer axiomId)
    {
    	AxiomId = axiomId;           
    }
    public Integer getAxiomId()
    {
        return AxiomId;
    }*/
    
	public void setAxiom (OWLAxiom axiom)
    {
    	Axiom = axiom;           
    }
    public OWLAxiom getAxiom()
    {
        return Axiom;
    }

    public void setRank (Integer rank)
    {
    	Rank = rank;           
    }
    public Integer getRank()
    {
        return Rank;
    }
}
