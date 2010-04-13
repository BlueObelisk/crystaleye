package wwmm.crystaleye.task;

import java.io.File;

public abstract class Task {
	
	private File dbRoot;
	private int[] keys;

	public Task(File storageRoot, int... keys) {
		this.dbRoot = storageRoot;
		this.keys = keys;
	}
	
	public File getDbRoot() {
		return this.dbRoot;
	}
	
	public int[] getKeys() {
		return this.keys;
	}
	
	public abstract void perform();

}
