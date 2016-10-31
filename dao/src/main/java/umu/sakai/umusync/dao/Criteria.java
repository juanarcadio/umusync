package umu.sakai.umusync.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import umu.sakai.umusync.api.ISyncManager;


@Entity
@Table(name="SAKAI_SYNCSITES_CRITERIA")
public class Criteria implements umu.sakai.umusync.api.dao.ICriteria {
	
	@SequenceGenerator(name="SyncIdGen", sequenceName="SEQ_SYNCSITES_CRITERIA")
	@Id @GeneratedValue(generator="SyncIdGen")
	@Column(name="CRITERIA_ID")
    protected long id;
	
	@Column(name="CRITERIA_NAME")
	@Length(max=30)
	@NotNull
	protected String name;

	@Column(name="PROPERTY")
	@Length(max=30)
	@NotNull
	protected String property;

	@Column(name="COMPARADOR")
	@Length(max=1)
	@NotNull
	protected String comparador = ISyncManager.COMPARATOR_EQUALS;
	
	@Column(name="VALOR")
	@Length(max=30)
	protected String valor;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getComparador() {
		return comparador;
	}

	public void setComparador(String comparador) {
		this.comparador = comparador;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comparador == null) ? 0 : comparador.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((valor == null) ? 0 : valor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Criteria other = (Criteria) obj;
		if (comparador == null) {
			if (other.comparador != null)
				return false;
		} else if (!comparador.equals(other.comparador))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (valor == null) {
			if (other.valor != null)
				return false;
		} else if (!valor.equals(other.valor))
			return false;
		return true;
	}



}
