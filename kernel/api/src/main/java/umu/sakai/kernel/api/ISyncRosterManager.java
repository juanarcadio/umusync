package umu.sakai.kernel.api;

import java.util.Collection;
import java.util.Date;

public interface ISyncRosterManager {
	public Collection<String> findCoordinadores();
	public Collection<String> findMiembrosGrupo();
	public void updateFilaCoordinadores(String section_eid, Date hora);
}
