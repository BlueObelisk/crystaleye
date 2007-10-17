package uk.ac.cam.ch.crystaleye.site;

import java.util.Set;

public class BondLengthCmlDescription {
	
	String cmlPath;
	String cmlId;
	Set<String> atomNos;
	String doi;
	String formula;
	String compoundClass;
	boolean isPolymeric;
	int uniqueSubMols;
	
	private BondLengthCmlDescription() {
		;
	}

	public BondLengthCmlDescription(String cmlPath, String cmlId, Set<String> atomNos, String doi, String formula, String compoundClass, boolean isPolymeric, int uniqueSubMols) {
		this.cmlPath = cmlPath;
		this.cmlId = cmlId;
		this.atomNos = atomNos;
		this.doi = doi;
		this.formula = formula;
		this.compoundClass = compoundClass;
		this.isPolymeric = isPolymeric;
		this.uniqueSubMols = uniqueSubMols;
	}

	public void setAtomIds(Set<String> atomIds) {
		this.atomNos = atomIds;
	}

	public void setCmlPath(String cmlPath) {
		this.cmlPath = cmlPath;
	}

	public void setCompoundClass(String compoundClass) {
		this.compoundClass = compoundClass;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public void setIsPolymeric(boolean isPolymeric) {
		this.isPolymeric = isPolymeric;
	}

	public Set<String> getAtomIds() {
		return atomNos;
	}

	public String getCmlPath() {
		return cmlPath;
	}

	public String getCompoundClass() {
		return compoundClass;
	}

	public String getDoi() {
		return doi;
	}

	public String getFormula() {
		return formula;
	}

	public boolean isPolymeric() {
		return isPolymeric;
	}

	public void setCmlId(String cmlId) {
		this.cmlId = cmlId;
	}

	public String getCmlId() {
		return cmlId;
	}

	public void setAtomNos(Set<String> atomNos) {
		this.atomNos = atomNos;
	}

	public void setPolymeric(boolean isPolymeric) {
		this.isPolymeric = isPolymeric;
	}

	public void setUniqueSubMols(int uniqueSubMols) {
		this.uniqueSubMols = uniqueSubMols;
	}

	public Set<String> getAtomNos() {
		return atomNos;
	}

	public int getUniqueSubMols() {
		return uniqueSubMols;
	}
	
	
}
