package com.ard333.sample.hibernatesearchgraalvmnativeimage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.lucene.search.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TermVector;

@Entity
@Indexed
class Foo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Field(termVector = TermVector.YES, analyze = Analyze.YES)
	private String value;

	public Foo() {
	}

	public Foo(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("Foo[id=%d, value='%s']", id, value);
	}
}


public class Main {

	@SuppressWarnings(value = { "unchecked" })
	public static void main(String[] args) throws InterruptedException {

		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

		Map<String, Object> settings = new HashMap<>();
		settings.put(Environment.DRIVER, "org.h2.Driver");
		settings.put(Environment.URL, "jdbc:h2:./foo");
		settings.put(Environment.HBM2DDL_AUTO, "update");
		settings.put("hibernate.search.default.directory_provider", "filesystem");
		settings.put("hibernate.search.default.indexBase", "./indexes");

		registryBuilder.applySettings(settings);
		StandardServiceRegistry registry = registryBuilder.build();

		Metadata metadata = new MetadataSources(registry).addAnnotatedClass(Foo.class).getMetadataBuilder().build();
		Session session = metadata.getSessionFactoryBuilder().build().openSession();

		Transaction transaction = session.getTransaction();
		transaction.begin();
		session.persist(new Foo("Hello"));
		transaction.commit();

		transaction.begin();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.createIndexer().startAndWait();
		Query q = fullTextSession
			.getSearchFactory()
			.buildQueryBuilder()
			.forEntity(Foo.class)
			.get()
			.keyword()
			.onField("value")
			.matching("Hello")
			.createQuery();

		FullTextQuery fullTextQ = fullTextSession.createFullTextQuery(q, Foo.class);
		List<Foo> result = fullTextQ.getResultList();

		System.out.println("\n=================================");
		System.out.println(result);
		System.out.println("=================================\n");
		transaction.commit();

		StandardServiceRegistryBuilder.destroy(registry);
	}
}
