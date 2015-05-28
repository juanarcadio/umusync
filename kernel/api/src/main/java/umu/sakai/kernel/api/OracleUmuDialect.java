package umu.sakai.kernel.api;

import org.hibernate.dialect.Oracle10gDialect;

public class OracleUmuDialect extends Oracle10gDialect {
	public String getQuerySequencesString() { return "select sequence_name from all_sequences"; }	
}