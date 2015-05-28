package umu.sakai.kernel.api;

public interface IUMUPhotoService {
	public byte[] getPhotoAsByteArray(String userId);
	public byte[] getPhotoAsByteArray(String userId,boolean forzar);
	public String getDirectorioUMU(String eid);
}
