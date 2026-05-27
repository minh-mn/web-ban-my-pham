package com.webshop.app.model;

import java.util.ArrayList;
import java.util.List;

public class Category {

	private int id;
	private String name;
	private String slug;
	private Integer parentId;
	private boolean active;

	/*
	 * Used by JSP to display the number of active products
	 * belonging to this category.
	 */
	private int productCount;

	/*
	 * Used to display hierarchical categories:
	 * parent category -> child categories.
	 */
	private List<Category> children = new ArrayList<>();

	// ===== CONSTRUCTORS =====

	public Category() {
	}

	public Category(int id, String name, String slug, Integer parentId, boolean active) {
		this.id = id;
		this.name = name;
		this.slug = slug;
		this.parentId = parentId;
		this.active = active;
	}

	// ===== GETTER / SETTER =====

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * JSP: ${category.productCount}
	 */
	public int getProductCount() {
		return productCount;
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}

	/**
	 * JSP: ${category.children}
	 */
	public List<Category> getChildren() {
		return children;
	}

	public void setChildren(List<Category> children) {
		this.children = children != null ? children : new ArrayList<>();
	}

	// ===== HELPER METHODS =====

	public boolean hasParent() {
		return parentId != null && parentId > 0;
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	public void addChild(Category child) {
		if (child != null) {
			this.children.add(child);
		}
	}
}