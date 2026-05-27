package com.webshop.app.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Product {

	// ===== CORE FIELDS =====
	private int id;
	private String title;
	private String slug;
	private String description;

	private BigDecimal price = BigDecimal.ZERO;
	private int discountPercent;
	private BigDecimal finalPrice;

	private int stock;
	private String image; // DB column: ảnh đại diện
	private boolean active;

	// ===== GALLERY =====
	private List<ProductImage> images = new ArrayList<>();

	// ===== RATING =====
	private Double avgRating;
	private int reviewCount;

	// ===== RELATION =====
	private Category category;
	private Brand brand;

	private long categoryId;
	private long brandId;

	/*
	 * View fields dùng cho admin product picker.
	 * Các field này có thể được ProductDAO set khi JOIN brand/category.
	 */
	private String categoryName;
	private String brandName;

	/*
	 * Dùng cho màn hình chọn sản phẩm áp dụng khuyến mãi.
	 * true nếu sản phẩm đang nằm trong bảng target:
	 * - store_coupon_product
	 * - store_branddiscount_product
	 * - store_promotionevent_product
	 */
	private boolean selectedForPromotion;

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
		this.title = title == null ? null : title.trim();
	}


	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug == null ? null : slug.trim();
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description == null ? null : description.trim();
	}


	public BigDecimal getPrice() {
		return price == null ? BigDecimal.ZERO : price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price == null ? BigDecimal.ZERO : price;
	}


	public int getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = Math.max(discountPercent, 0);
	}


	public BigDecimal getFinalPrice() {
		if (finalPrice != null) {
			return finalPrice;
		}

		return getPrice();
	}

	public void setFinalPrice(BigDecimal finalPrice) {
		this.finalPrice = finalPrice;
	}


	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = Math.max(stock, 0);
	}


	/**
	 * Ảnh đại diện.
	 */
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image == null ? null : image.trim();
	}


	/**
	 * JSP helper: ${product.imageUrl}
	 */
	public String getImageUrl() {
		return image;
	}


	/**
	 * Gallery ảnh con.
	 * JSP: ${product.images}
	 */
	public List<ProductImage> getImages() {
		if (images == null) {
			return Collections.emptyList();
		}

		return images;
	}

	public void setImages(List<ProductImage> images) {
		if (images == null) {
			this.images = new ArrayList<>();
		} else {
			this.images = new ArrayList<>(images);
		}
	}


	public boolean isActive() {
		return active;
	}

	/**
	 * JSP helper: ${product.active}
	 */
	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}


	// ===== RATING =====

	public Double getAvgRating() {
		return avgRating == null ? 0.0 : avgRating;
	}

	public void setAvgRating(Double avgRating) {
		this.avgRating = avgRating;
	}


	public int getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(int reviewCount) {
		this.reviewCount = Math.max(reviewCount, 0);
	}


	// ===== RELATION OBJECTS =====

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


	// ===== RELATION IDS =====

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = Math.max(categoryId, 0);
	}


	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = Math.max(brandId, 0);
	}


	// ===== VIEW FIELDS FOR PRODUCT PICKER =====

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName == null ? null : categoryName.trim();
	}


	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName == null ? null : brandName.trim();
	}


	public boolean isSelectedForPromotion() {
		return selectedForPromotion;
	}

	/**
	 * JSP helper: ${product.selectedForPromotion}
	 */
	public boolean getSelectedForPromotion() {
		return selectedForPromotion;
	}

	public void setSelectedForPromotion(boolean selectedForPromotion) {
		this.selectedForPromotion = selectedForPromotion;
	}


	// ===== HELPER FOR JSP / BUSINESS =====

	public boolean isHasDiscount() {
		return discountPercent > 0
				&& finalPrice != null
				&& getPrice().compareTo(BigDecimal.ZERO) > 0
				&& finalPrice.compareTo(getPrice()) < 0;
	}

	/**
	 * JSP helper: ${product.hasDiscount}
	 */
	public boolean getHasDiscount() {
		return isHasDiscount();
	}


	public boolean isOutOfStock() {
		return stock <= 0;
	}

	/**
	 * JSP helper: ${product.outOfStock}
	 */
	public boolean getOutOfStock() {
		return isOutOfStock();
	}


	public boolean isLowStock() {
		return stock > 0 && stock <= 5;
	}

	/**
	 * JSP helper: ${product.lowStock}
	 */
	public boolean getLowStock() {
		return isLowStock();
	}


	public BigDecimal getEffectivePrice() {
		return getFinalPrice();
	}


	public String getStockStatusLabel() {
		if (isOutOfStock()) {
			return "Hết hàng";
		}

		if (isLowStock()) {
			return "Sắp hết";
		}

		return "Còn hàng";
	}


	public String getDisplayTitle() {
		if (title == null || title.isBlank()) {
			return "Sản phẩm #" + id;
		}

		return title;
	}


	public boolean belongsToBrand(long targetBrandId) {
		return targetBrandId > 0 && this.brandId == targetBrandId;
	}


	public boolean belongsToCategory(long targetCategoryId) {
		return targetCategoryId > 0 && this.categoryId == targetCategoryId;
	}


	public boolean isAvailableForPromotion() {
		return active && stock > 0;
	}


	@Override
	public String toString() {
		return "Product{" +
				"id=" + id +
				", title='" + title + '\'' +
				", slug='" + slug + '\'' +
				", price=" + getPrice() +
				", discountPercent=" + discountPercent +
				", finalPrice=" + finalPrice +
				", stock=" + stock +
				", image='" + image + '\'' +
				", active=" + active +
				", categoryId=" + categoryId +
				", brandId=" + brandId +
				", categoryName='" + categoryName + '\'' +
				", brandName='" + brandName + '\'' +
				", selectedForPromotion=" + selectedForPromotion +
				'}';
	}
}