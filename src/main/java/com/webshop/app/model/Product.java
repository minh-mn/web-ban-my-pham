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
	private String image;
	private boolean active;

	// ===== GALLERY IMAGE =====
	private List<ProductImage> images = new ArrayList<>();

	// ===== ISSUE 123: PRODUCT MEDIA IMAGE / VIDEO =====
	private List<ProductMedia> mediaList = new ArrayList<>();

	// ===== RATING =====
	private Double avgRating;
	private int reviewCount;

	// ===== RELATION =====
	private Category category;
	private Brand brand;

	private long categoryId;
	private long brandId;

	private String categoryName;
	private String brandName;

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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image == null ? null : image.trim();
	}

	public String getImageUrl() {
		return image;
	}

	// ===== GALLERY IMAGE =====

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

	public boolean isHasImages() {
		return images != null && !images.isEmpty();
	}

	public boolean getHasImages() {
		return isHasImages();
	}

	// ===== ISSUE 123: PRODUCT MEDIA IMAGE / VIDEO =====

	public List<ProductMedia> getMediaList() {
		if (mediaList == null) {
			return Collections.emptyList();
		}

		return mediaList;
	}

	public void setMediaList(List<ProductMedia> mediaList) {
		if (mediaList == null) {
			this.mediaList = new ArrayList<>();
		} else {
			this.mediaList = new ArrayList<>(mediaList);
		}
	}

	/*
	 * Alias cho JSP nếu muốn gọi product.productMediaList.
	 */
	public List<ProductMedia> getProductMediaList() {
		return getMediaList();
	}

	public void setProductMediaList(List<ProductMedia> productMediaList) {
		setMediaList(productMediaList);
	}

	/*
	 * Alias ngắn nếu sau này muốn gọi product.media.
	 */
	public List<ProductMedia> getMedia() {
		return getMediaList();
	}

	public void setMedia(List<ProductMedia> media) {
		setMediaList(media);
	}

	public boolean isHasMedia() {
		return mediaList != null && !mediaList.isEmpty();
	}

	public boolean getHasMedia() {
		return isHasMedia();
	}

	public boolean isHasVideoMedia() {
		if (mediaList == null || mediaList.isEmpty()) {
			return false;
		}

		for (ProductMedia media : mediaList) {
			if (media != null && media.isVideo()) {
				return true;
			}
		}

		return false;
	}

	public boolean getHasVideoMedia() {
		return isHasVideoMedia();
	}

	public boolean isHasImageMedia() {
		if (mediaList == null || mediaList.isEmpty()) {
			return false;
		}

		for (ProductMedia media : mediaList) {
			if (media != null && media.isImage()) {
				return true;
			}
		}

		return false;
	}

	public boolean getHasImageMedia() {
		return isHasImageMedia();
	}

	// ===== ACTIVE =====

	public boolean isActive() {
		return active;
	}

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

	public boolean getHasDiscount() {
		return isHasDiscount();
	}

	public boolean isOutOfStock() {
		return stock <= 0;
	}

	public boolean getOutOfStock() {
		return isOutOfStock();
	}

	public boolean isLowStock() {
		return stock > 0 && stock <= 5;
	}

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
				", images=" + getImages().size() +
				", mediaList=" + getMediaList().size() +
				", categoryId=" + categoryId +
				", brandId=" + brandId +
				", categoryName='" + categoryName + '\'' +
				", brandName='" + brandName + '\'' +
				", selectedForPromotion=" + selectedForPromotion +
				'}';
	}
}