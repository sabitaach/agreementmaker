/**
 * 
 */
package am.app.mappingEngine.oaei.oaei2011;

import am.Utility;
import am.app.Core;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.AbstractMatcherParametersPanel;
import am.app.mappingEngine.AbstractParameters;
import am.app.mappingEngine.MatcherFactory;
import am.app.mappingEngine.MatchersRegistry;
import am.app.mappingEngine.SimilarityMatrix;
import am.app.mappingEngine.Combination.CombinationParameters;
import am.app.mappingEngine.IterativeInstanceStructuralMatcher.IterativeInstanceStructuralParameters;
import am.app.mappingEngine.LexicalSynonymMatcher.LexicalSynonymMatcherParameters;
import am.app.mappingEngine.baseSimilarity.advancedSimilarity.AdvancedSimilarityParameters;
import am.app.mappingEngine.multiWords.MultiWordsParameters;
import am.app.mappingEngine.oaei.OAEI_Track;
import am.app.mappingEngine.oaei2010.OAEI2010MatcherParameters;
import am.app.mappingEngine.oaei2010.OAEI2010MatcherParametersPanel;
import am.app.mappingEngine.parametricStringMatcher.ParametricStringParameters;

/**
 * 
 */
public class OAEI2011Matcher extends AbstractMatcher{
	
	private static final long serialVersionUID = -2258529392257305604L;
	
	//This should be false in batch mode & using learning matcher / true for alignment evaluation
	boolean showAllMatchers = true;

	
	//Use ontology evaluation for adapting configuration
	boolean ontologyEvaluation = true;
	
	boolean syntacticActive = true;
	
	SimilarityMatrix syntacticClassMatrix;
	SimilarityMatrix syntacticPropMatrix;
	
	//AbstractMatcher lastLayer;

	public OAEI2011Matcher(){
		super();
		needsParam = true;
		param = new OAEI2011MatcherParameters(OAEI_Track.AllMatchers); // should this be here?? Probably not.
	}
	
	public String getDescriptionString() {
		return "The method adopted in the OAEI2010 competition ";
	}
	
	/** *****************************************************************************************************
	 ************************ Init structures*************************************
	 * *******************************************************************************************************
	 */
	
	public void match() throws Exception {
    	matchStart();
    	OAEI2011MatcherParameters parameters = (OAEI2011MatcherParameters)param;
		AbstractMatcher finalResult = null;
		
		switch( parameters.currentTrack ) {
		case Anatomy:
			finalResult = runAnatomy();
			break;
		case Benchmarks:
			finalResult = runBenchmarks();
			break;
		case Conference:
			finalResult = runConference();
			break;

		default:
			throw new Exception("No valid track selected.");
		}
		
		if( finalResult != null ) {
			classesMatrix = finalResult.getClassesMatrix();
			propertiesMatrix = finalResult.getPropertiesMatrix();
			classesAlignmentSet = finalResult.getClassAlignmentSet();
			propertiesAlignmentSet = finalResult.getPropertyAlignmentSet();
		}
    	matchEnd();
    	if( Core.DEBUG ) System.out.println("OAEI2010-Conference matcher completed in (h.m.s.ms) "+Utility.getFormattedTime(executionTime));
    	//System.out.println("Classes alignments found: "+classesAlignmentSet.size());
    	//System.out.println("Properties alignments found: "+propertiesAlignmentSet.size());
	}

	/************************************************ CONFERENCE *******************************************************
	 *Run the Conference track.
	 * @return
	 * @throws Exception
	 *******************************************************************************************************************/
	private AbstractMatcher runConference() throws Exception {

		//FIRST LAYER: ASM and PSM
		//ASM

		AbstractMatcher asm = MatcherFactory.getMatcherInstance(MatchersRegistry.AdvancedSimilarity, 0);
		setupSubMatcher(asm, new AdvancedSimilarityParameters(getThreshold(), getMaxSourceAlign(), getMaxTargetAlign()));
		runSubMatcher(asm, "Submatcher: ASM");
		
		AbstractMatcher psm = MatcherFactory.getMatcherInstance(MatchersRegistry.ParametricString, 1);
		setupSubMatcher(psm, new ParametricStringParameters(getThreshold(),getMaxSourceAlign(), getMaxTargetAlign()).initForOAEI2010(OAEI_Track.Conference));
		runSubMatcher(psm, "Submatcher: PSM");
		
		//Second layer: LWC(ASM, PSM)
		//LWC matcher
		AbstractMatcher lwc = MatcherFactory.getMatcherInstance(MatchersRegistry.Combination, 2);
		lwc.getInputMatchers().add(asm);
		lwc.getInputMatchers().add(psm);
		setupSubMatcher(lwc, new CombinationParameters(getThreshold(),getMaxSourceAlign(), getMaxTargetAlign()).initForOAEI2010(OAEI_Track.Conference, true));
		runSubMatcher(lwc, "LWC( ASM, PSM)");

		//Third layer: GFM
		AbstractMatcher gfm = MatcherFactory.getMatcherInstance(MatchersRegistry.GroupFinder, 3);
		gfm.getInputMatchers().add(lwc);
		setupSubMatcher(gfm, new AbstractParameters(getThreshold(), getMaxSourceAlign(), getMaxTargetAlign()));
		runSubMatcher(gfm, "GFM( LWC )");
		//return gfm;

		getProgressDisplay().ignoreComplete(false);
		return gfm;
	}

/************************************************ BENCHMARKS *******************************************************
 *Run the BenchMarks track.
 * @return
 * @throws Exception
 *******************************************************************************************************************/
	private AbstractMatcher runBenchmarks() throws Exception {
		
		OAEI2010MatcherParameters parameters = (OAEI2010MatcherParameters)param;
		
		if(getProgressDisplay()!=null) getProgressDisplay().ignoreComplete(true);
		
		//ASM
		AbstractMatcher asm = null;
		if(parameters.usingASM && !isCancelled()){
		   	asm = MatcherFactory.getMatcherInstance(MatchersRegistry.AdvancedSimilarity, 0);
		   	setupSubMatcher(asm, new AdvancedSimilarityParameters(getThreshold(),1,1));
		   	runSubMatcher(asm, "Submatcher: ASM");
		}
		
		//PSM
		AbstractMatcher psm = null;
		if(parameters.usingPSM && !isCancelled()){
		   	psm = MatcherFactory.getMatcherInstance(MatchersRegistry.ParametricString,1);
		   	setupSubMatcher(psm, new ParametricStringParameters(getThreshold(), 1, 1).initForOAEI2010(parameters.currentTrack));
		   	runSubMatcher(psm, "Submatcher: PSM");
		}
			
		//VMM
		AbstractMatcher vmm = null;
		if(parameters.usingVMM && !isCancelled()){
			vmm = MatcherFactory.getMatcherInstance(MatchersRegistry.MultiWords, 2);
		   	setupSubMatcher(vmm, new MultiWordsParameters(getThreshold(), 1, 1).initForOAEI2010(parameters.currentTrack));
		   	runSubMatcher(vmm, "Submatcher: VMM");
		}
			
		//LSM .. maybe take out of Benchmarks track.
		AbstractMatcher lsm = null;
		if(parameters.usingLSM && !isCancelled()){
		   	lsm = MatcherFactory.getMatcherInstance(MatchersRegistry.LSM, 2);
		   	setupSubMatcher(lsm, new AbstractParameters(getThreshold(), 1, 1));
		   	runSubMatcher(lsm, "Submatcher: LSM");
		}
			
		//Second layer: LWC(ASM, PSM, VMM, LSM)
		
		//LWC matcher
		AbstractMatcher lwc1 = null;
		if(parameters.usingLWC1 && !isCancelled()){
		   	lwc1 = MatcherFactory.getMatcherInstance(MatchersRegistry.Combination, 3);
		   	lwc1.getInputMatchers().add(asm);
		   	lwc1.getInputMatchers().add(psm);
		   	lwc1.getInputMatchers().add(vmm);
		   	setupSubMatcher(lwc1, new CombinationParameters(getThreshold(),1,1).initForOAEI2010(parameters.currentTrack, true), true);
		   	runSubMatcher(lwc1, "LWC( ASM, PSM, VMM, LSM )");
		
			syntacticClassMatrix = lwc1.getClassesMatrix();
			syntacticPropMatrix = lwc1.getPropertiesMatrix();
		}
		
		//Third layer: IISM
		
		//FCM
		AbstractMatcher iism = null;
		if(parameters.usingIISM && !isCancelled()){
	    	iism = MatcherFactory.getMatcherInstance(MatchersRegistry.IISM, 5);
	    	if(lwc1!=null) iism.getInputMatchers().add(lwc1);
	    	setupSubMatcher(iism, new IterativeInstanceStructuralParameters(getThreshold(),1,1).setForOAEI2010());
	    	runSubMatcher(iism, "Submatcher: IISM");
	    }
		
		if(getProgressDisplay()!=null) getProgressDisplay().ignoreComplete(false);
		
		return iism;
	}


/************************************************ ANATOMY *******************************************************
 *Run the Anatomy track.
 * @return
 * @throws Exception
 *******************************************************************************************************************/
	private AbstractMatcher runAnatomy() throws Exception {
	
		OAEI2010MatcherParameters parameters = new OAEI2010MatcherParameters(OAEI_Track.Anatomy);

		getProgressDisplay().ignoreComplete(true);  // do not want the sub matchers to trigger the completion of the OAEI matcher.
		
		//LSM
		AbstractMatcher lsm = null;
		if(parameters.usingLSM && !isCancelled() ){
	    	lsm = MatcherFactory.getMatcherInstance(MatchersRegistry.LSM, 0);    	
	    	setupSubMatcher(lsm, new LexicalSynonymMatcherParameters( getThreshold(), getMaxSourceAlign(), getMaxTargetAlign() ), true );
	    	runSubMatcher(lsm, "LSM (1/4)");
		}
		
		//PSM
		AbstractMatcher psm = null;
		if(parameters.usingPSM && !isCancelled()){
	    	psm = MatcherFactory.getMatcherInstance(MatchersRegistry.ParametricString, 1);   	
	    	setupSubMatcher(psm, new ParametricStringParameters( getThreshold(), getMaxSourceAlign(), getMaxTargetAlign() ).initForOAEI2010(parameters.currentTrack), true );
	    	runSubMatcher(psm, "PSM (2/4)");
		}
		
		//VMM
		AbstractMatcher vmm = null;
		if(parameters.usingVMM && !isCancelled()){
	    	vmm = MatcherFactory.getMatcherInstance(MatchersRegistry.MultiWords, 2);
			setupSubMatcher(vmm, new MultiWordsParameters(getThreshold(), getMaxSourceAlign(), getMaxTargetAlign()).initForOAEI2010(parameters.currentTrack), true);
			runSubMatcher(vmm, "VMM (3/4)");
		}
		
		//Second layer: LWC(PSM, VMM, LSM)
		
		//LWC matcher
		AbstractMatcher lwc1 = null;
		if(parameters.usingLWC1 && !isCancelled()){
	    	lwc1 = MatcherFactory.getMatcherInstance(MatchersRegistry.Combination, 3);
	    	lwc1.getInputMatchers().add(psm);
	    	lwc1.getInputMatchers().add(vmm);
	    	lwc1.getInputMatchers().add(lsm);
			setupSubMatcher(lwc1, new CombinationParameters(getThreshold(), getMaxSourceAlign(), getMaxTargetAlign()).initForOAEI2010(parameters.currentTrack, true), true);
			runSubMatcher(lwc1, "LWC (4/4)");
		}
		
		if(getProgressDisplay()!=null) getProgressDisplay().ignoreComplete(false); // done with sub matchers.
		return lwc1;
	}
	
	private void setupSubMatcher( AbstractMatcher m, AbstractParameters p ) { setupSubMatcher(m, p, false); }
	
	private void setupSubMatcher( AbstractMatcher m, AbstractParameters p, boolean progressDelay ) {
		m.setParam(p);
		m.setUseProgressDelay(progressDelay);
		m.setSourceOntology(sourceOntology);
    	m.setTargetOntology(targetOntology);
	}
	
	private void runSubMatcher(AbstractMatcher m, String label) throws Exception {
		long startime = 0, endtime = 0, time = 0;
		long measure = 1000000;
		
		if( Core.DEBUG ) System.out.println("Running " + m.getRegistryEntry().getMatcherShortName() );
		startime = System.nanoTime()/measure;
		
		if(getProgressDisplay()!=null) getProgressDisplay().setProgressLabel(label);
		m.setProgressDisplay(getProgressDisplay());
		m.match();
		m.setProgressDisplay(null);
		if( m.isCancelled() ) { cancel(true); } // the user canceled the matching process  
		
		endtime = System.nanoTime()/measure;
	    time = (endtime-startime);
		if( Core.DEBUG ) System.out.println(m.getRegistryEntry().getMatcherShortName() + " completed in (h.m.s.ms) "+Utility.getFormattedTime(time));
		
		if(showAllMatchers && !m.isCancelled()) Core.getUI().getControlPanel().getTablePanel().addMatcher(m);
	}
	
	
	public AbstractMatcherParametersPanel getParametersPanel() {
		if(parametersPanel == null){
			parametersPanel = new OAEI2010MatcherParametersPanel();
		}
		return parametersPanel;
	}
}