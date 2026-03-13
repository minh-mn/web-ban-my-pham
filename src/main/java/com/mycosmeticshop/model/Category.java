package com.mycosmeticshop.model;

import java.util.ArrayList;
import java.util.List;

public class Category {

	private int id;
	private String name;
	private String slug;
	private Integer parentId;
	private boolean active;

	private int productCount; // ✅ CHO JSP
	private List<Category> children = new ArrayList<>();

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

	/** ✅ JSP: ${category.productCount} */
	public int getProductCount() {
		return productCount;
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}

	// ===== CHILDREN =====
	public List<Category> getChildren() {
		return children;
	}

	public void setChildren(List<Category> children) {
		this.children = children;
	}
}
