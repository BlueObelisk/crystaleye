package wwmm.crystaleye.find;

public class JournalCifFinder {

	int maxSleep = 1000;
	
	protected void sleep() {
		int maxTime = Integer.valueOf(maxSleep);
		try {
			Thread.sleep(((int) (maxTime * Math.random())));
		} catch (InterruptedException e) {
			System.err.println("Sleep interrupted.");
		}
	}
	
}
