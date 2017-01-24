package m2_lab6;

import java.util.ArrayList;

public class BoostExamples {

	public static void main(String args[]) {
		ArrayList<AbstractBoostType> boostTypes = new ArrayList<AbstractBoostType>();
		if (args.length == 0) {
			printProgramUsage();
		} else {
			String boostType = args[0];
			boolean printExplanation = false; 
			if (args.length != 1 && "true".equalsIgnoreCase(args[1])) {
				printExplanation = true;
			}
			switch (boostType) {
			case "index": boostTypes.add(new BoostAtIndexingImpl(printExplanation)); break;
			case "query": boostTypes.add(new BoostAtQueryImpl(printExplanation)); break;
			case "both": boostTypes.add(new BoostAtIndexingImpl(printExplanation)); 
			             boostTypes.add(new BoostAtQueryImpl(printExplanation)); break;
			default: System.out.println("Invalid Index Option"); printProgramUsage();
			         System.exit(1);
			}
			
			for (AbstractBoostType boostTypeAtHand : boostTypes) {
				boostTypeAtHand.populateIndex();
				boostTypeAtHand.searchAndPrintResults();
			}
			
			
		}
	}
	
	private static void printProgramUsage() {
		System.out.println("--------------------------");
		System.out.println("Program Usage: ");
		System.out.println("--------------------------");
		System.out.println("Param 1: Type of boost: ");
		System.out.println("index -- Index Time Boosting ");
		System.out.println("query -- Query Time Boosting ");
		System.out.println("both -- Demo both Index and Query boosting ");
		System.out.println("Param 2: Print scoring info: Either true or false ");
		System.out.println("--------------------------");
	}
}
