# 🏗️ Sơ Đồ Tổng Quan - Mối Liên Kết Product với Các Entity

## 📊 Entity Relationship Diagram

```mermaid
erDiagram
    %% Core Product Entities
    Product {
        UUID productId PK
        String productUrl UK
        String productName
        String description
        String unitOfMeasure
        String brand
        String model
        String sku UK
        String barcode
        UUID supplierId FK
        Boolean isFeatured
        UUID productTypeId FK
    }

    ProductType {
        UUID productTypeId PK
        String productTypeName
        String productTypeCode UK
        String description
        UUID categoryId FK
    }

    ProductAttribute {
        UUID attributeId PK
        String attributeName
        String attributeCode UK
        String unit
        Boolean isRequired
        String dataType
    }

    ProductAttributeValue {
        UUID productId PK,FK
        UUID attributeId PK,FK
        String valueText
        BigDecimal valueNumber
    }

    %% Classification Entities
    Category {
        UUID categoryId PK
        String categoryName
        String categoryCode UK
        String description
        String categoryType
    }

    %% Business Entities
    Supplier {
        UUID supplierId PK
        String supplierName
        String contactPerson
        String phone
        String email
        String address
        String bankName
        String bankAccount
    }

    %% Service & Package Entities
    Service {
        UUID serviceId PK
        String serviceName
        String serviceCode UK
        String description
        BigDecimal basePrice
        Integer estimatedDuration
        UUID serviceTypeId FK
    }

    ServiceProduct {
        UUID serviceProductId PK
        UUID serviceId FK
        UUID productId FK
        Integer quantity
        BigDecimal unitPrice
        BigDecimal totalPrice
        String notes
        Boolean isRequired
    }

    ServicePackage {
        UUID packageId PK
        String packageName
        String packageCode UK
        String description
        BigDecimal packagePrice
        Integer estimatedDuration
        Boolean isActive
    }

    ServicePackageProduct {
        UUID servicePackageProductId PK
        UUID packageId FK
        UUID productId FK
        Integer quantity
        BigDecimal unitPrice
        BigDecimal totalPrice
        String notes
        Boolean isRequired
    }

    %% Promotion Entities
    Promotion {
        UUID promotionId PK
        String promotionName
        String promotionCode UK
        String description
        String promotionType
        BigDecimal discountValue
        BigDecimal minOrderAmount
        LocalDateTime startDate
        LocalDateTime endDate
        Boolean isActive
        UUID freeProductId FK
        UUID buyProductId FK
        UUID getProductId FK
    }

    PromotionLine {
        UUID lineId PK
        UUID promotionId FK
        String targetVehicleTypes
        String targetServices
        String targetProducts
        Integer requiredQuantity
        BigDecimal requiredAmount
        UUID itemId FK
        String itemType
    }

    %% Media Entity
    Media {
        UUID mediaId PK
        String entityType
        UUID entityId FK
        String mediaUrl
        String mediaType
        Boolean isMain
        Integer sortOrder
        String altText
    }

    %% Relationships
    Product ||--o{ ProductAttributeValue : "has attributes"
    ProductAttribute ||--o{ ProductAttributeValue : "defines values"
    Product }o--|| ProductType : "belongs to"
    ProductType }o--|| Category : "categorized by"
    Product }o--|| Supplier : "supplied by"
    
    Service ||--o{ ServiceProduct : "uses products"
    Product ||--o{ ServiceProduct : "used in services"
    
    ServicePackage ||--o{ ServicePackageProduct : "includes products"
    Product ||--o{ ServicePackageProduct : "included in packages"
    
    Product ||--o{ Promotion : "free product"
    Product ||--o{ Promotion : "buy product"
    Product ||--o{ Promotion : "get product"
    Promotion ||--o{ PromotionLine : "has conditions"
    Product ||--o{ PromotionLine : "targeted by"
    
    Product ||--o{ Media : "has media"
```

## 🔗 Chi Tiết Mối Quan Hệ

### 1. **Product Core Relationships**

#### **🏷️ ProductType (Many-to-One)**
- **Relationship**: `Product` → `ProductType`
- **Type**: Many-to-One (N:1)
- **Description**: Một Product thuộc về một ProductType
- **Implementation**: `@ManyToOne` với `@JoinColumn(name = "product_type_id")`

#### **📋 ProductAttribute (Many-to-Many)**
- **Relationship**: `Product` ↔ `ProductAttribute`
- **Type**: Many-to-Many (N:M)
- **Bridge Table**: `ProductAttributeValue`
- **Description**: Product có nhiều attributes, Attribute áp dụng cho nhiều products
- **Implementation**: Composite Primary Key với `@IdClass(ProductAttributeValueId)`

#### **🏢 Supplier (One-to-Many)**
- **Relationship**: `Supplier` → `Product`
- **Type**: One-to-Many (1:N)
- **Description**: Một Supplier cung cấp nhiều Products
- **Implementation**: Direct UUID reference (`supplier_id` column)

### 2. **Business Relationships**

#### **🔧 Service Integration**
- **ServiceProduct**: Bridge table giữa Service và Product
- **Relationship**: Service sử dụng Products với quantity và pricing
- **Fields**: `quantity`, `unitPrice`, `totalPrice`, `isRequired`

#### **📦 Package Integration**
- **ServicePackageProduct**: Bridge table giữa ServicePackage và Product
- **Relationship**: Package bao gồm Products với quantity và pricing
- **Fields**: `quantity`, `unitPrice`, `totalPrice`, `isRequired`

#### **🎁 Promotion Integration**
- **Direct References**: Product có thể là `freeProduct`, `buyProduct`, hoặc `getProduct`
- **PromotionLine**: Target products trong promotion conditions
- **Fields**: `targetProducts` (JSON array), `itemId`, `itemType`

### 3. **Media Integration**

#### **📸 Generic Media System**
- **EntityType**: `PRODUCT` trong Media entity
- **Relationship**: Product có nhiều Media items
- **Fields**: `entityType`, `entityId`, `mediaUrl`, `isMain`, `sortOrder`

## 📊 Relationship Summary Table

| Entity | Relationship Type | Description | Implementation |
|--------|------------------|-------------|----------------|
| **ProductType** | Many-to-One | Product belongs to ProductType | `@ManyToOne` |
| **ProductAttribute** | Many-to-Many | Product has multiple attributes | Bridge table `ProductAttributeValue` |
| **Supplier** | One-to-Many | Supplier provides multiple products | Direct UUID reference |
| **Service** | Many-to-Many | Service uses multiple products | Bridge table `ServiceProduct` |
| **ServicePackage** | Many-to-Many | Package includes multiple products | Bridge table `ServicePackageProduct` |
| **Promotion** | One-to-Many | Promotion targets multiple products | Direct references + JSON arrays |
| **Media** | One-to-Many | Product has multiple media items | Generic media system |

## 🎯 Key Features

### **✅ Flexible Attribute System**
- Dynamic attributes through `ProductAttributeValue`
- Support for text and numeric values
- Required/optional attributes

### **✅ Business Integration**
- Service-Product relationships with pricing
- Package-Product bundling
- Promotion targeting

### **✅ Media Management**
- Generic media system
- Multiple media per product
- Main media designation

### **✅ Supplier Management**
- Direct supplier reference
- One-to-many relationship

## 🔄 Data Flow

```mermaid
flowchart TD
    A[Product Creation] --> B[Assign ProductType]
    B --> C[Set Supplier]
    C --> D[Add Attributes]
    D --> E[Upload Media]
    E --> F[Create Services/Packages]
    F --> G[Setup Promotions]
    
    H[ProductType] --> B
    I[Category] --> J[ProductType]
    K[Supplier] --> C
    L[ProductAttribute] --> D
    M[Media] --> E
    N[Service] --> F
    O[ServicePackage] --> F
    P[Promotion] --> G
```

## 🚀 API Endpoints Summary

| Entity | CRUD Operations | Special Endpoints |
|--------|----------------|-------------------|
| **Product** | ✅ Full CRUD | Filter by ProductType, Supplier, Attributes |
| **ProductType** | ✅ Full CRUD | Filter by Category |
| **ProductAttribute** | ✅ Full CRUD | Filter by DataType, Required |
| **ProductAttributeValue** | ✅ Full CRUD | Bulk operations |
| **Media** | ✅ Full CRUD | Entity-specific queries |
| **Supplier** | ✅ Full CRUD | Product count by supplier |

**Sơ đồ này thể hiện toàn bộ mối liên kết giữa Product và các entity khác trong hệ thống SCSMS!** 🎯
