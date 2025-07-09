package com.devstaq.auth.persistence.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Collection;

/**
 * The Privilege Entity. Part of the basic User ->> Role ->> Privilege structure.
 */
@Data
@Entity
public class Privilege {

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** The name. */
	private String name;

	/** The description of the role. */
	private String description;

	/** The roles which have this privilege. */
	@ToString.Exclude
	@ManyToMany(mappedBy = "privileges")
	private Collection<Role> roles;

	/**
	 * Instantiates a new privilege.
	 */
	public Privilege() {
		super();
	}

	/**
	 * Instantiates a new privilege.
	 *
	 * @param name the name
	 */
	public Privilege(final String name) {
		super();
		this.name = name;
	}

	/**
	 * Instantiates a new privilege.
	 *
	 * @param name the name
	 * @param description the description
	 */
	public Privilege(final String name, final String description) {
		super();
		this.name = name;
		this.description = description;
	}
}
