package com.devstaq.auth.persistence.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * The Role Entity. Part of the basic User ->> Role ->> Privilege structure.
 */
@Data
@Entity
public class Role {
	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@ToString.Include
	private Long id;

	/** The users. */
	@ToString.Exclude
	@ManyToMany(mappedBy = "roles", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private Set<User> users = new HashSet<>();

	/** The privileges. */
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinTable(name = "roles_privileges", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id"))
	private Set<Privilege> privileges = new HashSet<>();

	/** The name. */
	private String name;

	private String description;

	/**
	 * Instantiates a new role.
	 */
	public Role() {
		super();
	}

	/**
	 * Instantiates a new role.
	 *
	 * @param name the name
	 */
	public Role(final String name) {
		super();
		this.name = name;
	}

	/**
	 * Instantiates a new role.
	 *
	 * @param name the name
	 * @param description the description
	 */
	public Role(final String name, final String description) {
		super();
		this.name = name;
		this.description = description;
	}
}
