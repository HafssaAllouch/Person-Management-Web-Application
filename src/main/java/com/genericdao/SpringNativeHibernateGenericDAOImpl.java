package com.genericdao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.query.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Classe de base pour tous les DAOs, elle implémente les méthodes CRUD
 * génériques définit par le contrat GenericDAO<T>. Cette implémentation est
 * basée sur Hibernate nativement
 * 
 * @version 1.1
 * 
 * @author <a href="mailto:tarik.boudaa@gmail.com">T.BOUDAA Ecole Nationale des
 *         Sciences Appliquées </a>
 * 
 * @param <T>  le type d'objet métier manipulé
 * @param <PK> le type utilisé pour l'indentifiant d'un objet métier
 */

public abstract class SpringNativeHibernateGenericDAOImpl<T, PK extends Serializable> implements GenericDao<T, PK> {

	/** La classe BO manipulé par le DAO */
	protected Class<T> boClass;
	
	/** Utilisé par tous les DAOs */
	protected final Logger LOGGER;

	/** la fabrique des session */
	@Autowired
	protected SessionFactory sf;

	public SpringNativeHibernateGenericDAOImpl() {
		Type t = getClass().getGenericSuperclass();
		ParameterizedType pt = (ParameterizedType) t;
		boClass = (Class) pt.getActualTypeArguments()[0];
		LOGGER = Logger.getLogger(boClass);

	}

	public SpringNativeHibernateGenericDAOImpl(Class<T> pClass) {
		boClass = pClass;
		LOGGER = Logger.getLogger(boClass);
		LOGGER.debug("le dao de " + boClass + " a été initialisé");
	}

	protected Session getSession() {
		return sf.getCurrentSession();
	}

	public T create(T o) {

		LOGGER.debug("appel de la méthode create");

		// On obtient la session en cours
		Session s = sf.getCurrentSession();

		s.persist(o);

		return o;
	}

	public void update(T o) {

		LOGGER.debug("appel de la méthode update");

		// On obtient la session en cours
		Session s = getSession();

		s.merge(o);

	}

	public List<T> getAll() {

		LOGGER.debug("appel de la méthode getAll");

		// On obtient la session en cours
		Session s = getSession();

		List<T> list = new ArrayList<>();

		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<T> crQuery = builder.createQuery(boClass);
		Root<T> root = crQuery.from(boClass); // Spécifiez la racine de la requête
		crQuery.select(root); // Sélectionnez la racine
		Query<T> query = s.createQuery(crQuery);
		list = query.getResultList();
		return list;
	}

	public List<T> getEntityByColValue(String pColumnName, String pValue) {
		Map<String, String> colValues = new HashMap<String, String>();
		colValues.put(pColumnName, pValue);
		return getEntityByColValue(colValues, null);
	}

	public List<T> getEntityByColValue(Map<String, String> colValues) throws EntityNotFoundException {
		return getEntityByColValue(colValues, null);
	}

	public List<T> getEntityByColValue(Map<String, String> colValues, Map<String, String> orderCols)
			throws EntityNotFoundException {

		LOGGER.debug("appel de la méthode getEntityByColValue");

		// On obtient la session en cours
		Session s = getSession();

		List<T> list = new ArrayList<T>();

		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(boClass);
		Root<T> root = criteria.from(boClass);
		criteria.select(root);
		for (Map.Entry<String, String> entry : colValues.entrySet()) {
			criteria.where(builder.equal(root.get(entry.getKey()), entry.getValue()));
		}

		if (orderCols != null) {
			for (Map.Entry<String, String> entry : orderCols.entrySet()) {
				if ("as".equals(entry.getValue())) {
					criteria.orderBy(builder.asc(root.get(entry.getKey())));

				} else {
					criteria.orderBy(builder.desc(root.get(entry.getKey())));

				}
			}
		}
		list = s.createQuery(criteria).getResultList();
		return list;
	}

	public void delete(PK pId) throws EntityNotFoundException {

		LOGGER.debug("appel de la méthode delete");

		// On obtient la session en cours
		Session s = getSession();

		T obj = (T) findById(pId);
		s.remove(obj);

	}

	public List<T> getAllDistinct() throws EntityNotFoundException {

		Collection<T> result = new LinkedHashSet<T>(getAll());
		return new ArrayList<T>(result);

	}

	public T findById(PK pId) throws EntityNotFoundException {
		LOGGER.debug("appel de la méthode findById");

		// On obtient la session en cours
		Session s = getSession();

		T obj = (T) s.get(boClass, pId);

		return obj;
	}

	public boolean exists(PK pId) {

		try {
			findById(pId);

		} catch (EntityNotFoundException ex) {
			return false;
		}

		return true;

	}

}
