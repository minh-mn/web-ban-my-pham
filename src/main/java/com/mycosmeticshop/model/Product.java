package com.mycosmeticshop.model;

import java.math.BigDecimal;
import java.util.List;

public class Product {

	// ===== CORE FIELDS =====
	private int id;
	private String title;
	private String slug;
	private String description;

	private BigDecimal price;
	private int discountPercent;
	private BigDecimal finalPrice;

	private int stock;
	private String image; // DB column (ảnh đại diện)
	private boolean active;

	// ===== GALLERY (ảnh con mô tả) =====
	private List<ProductImage> images;

	// ===== RATING =====
	private Double avgRating;
	private int reviewCount;

	// ===== RELATION =====
	private Category category;
	private Brand brand;

	// ================= GETTER / SETTER =================

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}

	public BigDecimal getFinalPrice() {
		return finalPrice != null ? finalPrice : price;
	}

	public void setFinalPrice(BigDecimal finalPrice) {
		this.finalPrice = finalPrice;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	/** Ảnh đại diện */
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	/** ✅ JSP: ${product.imageUrl} */
	public String getImageUrl() {
		return image;
	}

	/** ✅ Gallery ảnh con (JSP: ${product.images}) */
	public List<ProductImage> getImages() {
		return images;
	}

	public void setImages(List<ProductImage> images) {
		this.images = images;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	// ===== ⭐ RATING =====
	public Double getAvgRating() {
		return avgRating != null ? avgRating : 0.0;
	}

	public void setAvgRating(Double avgRating) {
		this.avgRating = avgRating;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	// ===== HELPER FOR JSP =====

	public boolean isHasDiscount() {
		return discountPercent > 0 && finalPrice != null && finalPrice.compareTo(price) < 0;
	}

	public boolean isOutOfStock() {
		return stock <= 0;
	}

	public boolean isLowStock() {
		return stock > 0 && stock <= 5;
	}
}
