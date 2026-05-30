package com.webshop.app.model;

import java.util.ArrayList;
import java.util.Collections;
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
	 * Used by JSP/admin list to display the number of tags
	 * belonging to this category.
	 */
	private int tagCount;

	/*
	 * Used by homepage hot category UI.
	 * This value is usually the representative image of an active product
	 * inside the category, so the homepage can render circle category thumbnails.
	 */
	private String hotImageUrl;

	/*
	 * Optional short label for homepage/category highlight badges.
	 */
	private String highlightLabel;

	/*
	 * Used to display hierarchical categories:
	 * parent category -> child categories.
	 */
	private List<Category> children = new ArrayList<>();

	/*
	 * Used to display category tags on admin category pages
	 * and product pages.
	 */
	private List<CategoryTag> tags = new ArrayList<>();

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
		this.name = name == null ? null : name.trim();
	}


	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug == null ? null : slug.trim();
	}


	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		if (parentId == null || parentId <= 0) {
			this.parentId = null;
		} else {
			this.parentId = parentId;
		}
	}


	public boolean isActive() {
		return active;
	}

	/**
	 * JSP helper: ${category.active}
	 */
	public boolean getActive() {
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
		this.productCount = Math.max(productCount, 0);
	}


	/**
	 * JSP: ${category.tagCount}
	 */
	public int getTagCount() {
		if (tagCount > 0) {
			return tagCount;
		}

		return tags == null ? 0 : tags.size();
	}

	public void setTagCount(int tagCount) {
		this.tagCount = Math.max(tagCount, 0);
	}


	/**
	 * JSP: ${category.hotImageUrl}
	 */
	public String getHotImageUrl() {
		return hotImageUrl;
	}

	public void setHotImageUrl(String hotImageUrl) {
		this.hotImageUrl = hotImageUrl == null ? null : hotImageUrl.trim();
	}


	/**
	 * JSP: ${category.highlightLabel}
	 */
	public String getHighlightLabel() {
		return highlightLabel;
	}

	public void setHighlightLabel(String highlightLabel) {
		this.highlightLabel = highlightLabel == null ? null : highlightLabel.trim();
	}


	/**
	 * JSP: ${category.children}
	 */
	public List<Category> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}

		return children;
	}

	public void setChildren(List<Category> children) {
		if (children == null) {
			this.children = new ArrayList<>();
		} else {
			this.children = new ArrayList<>(children);
		}
	}


	/**
	 * JSP: ${category.tags}
	 */
	public List<CategoryTag> getTags() {
		if (tags == null) {
			return Collections.emptyList();
		}

		return tags;
	}

	public void setTags(List<CategoryTag> tags) {
		if (tags == null) {
			this.tags = new ArrayList<>();
		} else {
			this.tags = new ArrayList<>(tags);
		}

		this.tagCount = this.tags.size();
	}

	// ===== HELPER METHODS =====

	public boolean hasParent() {
		return parentId != null && parentId > 0;
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	public boolean hasTags() {
		return tags != null && !tags.isEmpty();
	}

	public void addChild(Category child) {
		if (child != null) {
			if (this.children == null) {
				this.children = new ArrayList<>();
			}

			this.children.add(child);
		}
	}

	public void addTag(CategoryTag tag) {
		if (tag != null) {
			if (this.tags == null) {
				this.tags = new ArrayList<>();
			}

			this.tags.add(tag);
			this.tagCount = this.tags.size();
		}
	}

	public void clearTags() {
		if (this.tags == null) {
			this.tags = new ArrayList<>();
		} else {
			this.tags.clear();
		}

		this.tagCount = 0;
	}

	public String getDisplayName() {
		if (name == null || name.isBlank()) {
			return "Danh mục #" + id;
		}

		return name;
	}

	@Override
	public String toString() {
		return "Category{" +
				"id=" + id +
				", name='" + name + '\'' +
				", slug='" + slug + '\'' +
				", parentId=" + parentId +
				", active=" + active +
				", productCount=" + productCount +
				", tagCount=" + getTagCount() +
				", children=" + getChildren().size() +
				", tags=" + getTags().size() +
				'}';
	}
}