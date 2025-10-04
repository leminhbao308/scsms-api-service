# 🏗️ Sơ Đồ Tổng Quan - Mối Liên Kết Product

## 🎯 **Product Entity - Trung Tâm Hệ Thống**

```
                    ┌─────────────────┐
                    │     PRODUCT     │
                    │   (product_id)  │
                    └─────────────────┘
                            │
                    ┌───────┼───────┐
                    │       │       │
                    ▼       ▼       ▼
            ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
            │ PRODUCTTYPE │ │  SUPPLIER   │ │   MEDIA     │
            │ (Many-to-1) │ │ (1-to-Many) │ │ (1-to-Many) │
            └─────────────┘ └─────────────┘ └─────────────┘
                    │
                    ▼
            ┌─────────────┐
            │  CATEGORY   │
            │ (Many-to-1) │
            └─────────────┘
```

## 🔗 **Chi Tiết Mối Quan Hệ**

### **1. Core Relationships (Trực tiếp)**

#### **🏷️ ProductType ← Product (Many-to-One)**
```
ProductType (1) ←→ (N) Product
├── product_type_id (FK)
├── productTypeName
├── productTypeCode
└── category_id (FK)
```

#### **🏢 Supplier → Product (One-to-Many)**
```
Supplier (1) ←→ (N) Product
├── supplier_id (FK in Product)
├── supplierName
├── contactPerson
└── phone, email, address
```

#### **📸 Media → Product (One-to-Many)**
```
Media (N) ←→ (1) Product
├── entityType = "PRODUCT"
├── entityId = product_id
├── mediaUrl
├── isMain
└── sortOrder
```

### **2. Attribute System (Many-to-Many)**

#### **📋 Product ↔ ProductAttribute**
```
Product (N) ←→ (M) ProductAttribute
        ↕
ProductAttributeValue (Bridge Table)
├── product_id (PK, FK)
├── attribute_id (PK, FK)
├── valueText
└── valueNumber
```

**ProductAttribute Examples:**
- Trọng lượng (WEIGHT) - DECIMAL
- Thời gian bảo hành (WARRANTY_MONTHS) - INTEGER
- Màu sắc (COLOR) - STRING
- Kích thước (DIMENSIONS) - STRING
- Công suất (POWER) - NUMBER
- Độ nhớt (VISCOSITY) - STRING
- Tiêu chuẩn chất lượng (QUALITY_STANDARD) - STRING

### **3. Business Integration**

#### **🔧 Service Integration**
```
Service (N) ←→ (M) Product
        ↕
ServiceProduct (Bridge Table)
├── service_id (FK)
├── product_id (FK)
├── quantity
├── unitPrice
├── totalPrice
└── isRequired
```

#### **📦 Package Integration**
```
ServicePackage (N) ←→ (M) Product
        ↕
ServicePackageProduct (Bridge Table)
├── package_id (FK)
├── product_id (FK)
├── quantity
├── unitPrice
├── totalPrice
└── isRequired
```

#### **🎁 Promotion Integration**
```
Promotion (1) ←→ (N) Product
├── freeProduct_id (FK) - Sản phẩm miễn phí
├── buyProduct_id (FK) - Sản phẩm cần mua
├── getProduct_id (FK) - Sản phẩm được tặng
└── targetProducts (JSON array)

PromotionLine (1) ←→ (N) Product
├── targetProducts (JSON array)
├── itemId (FK)
└── itemType (PRODUCT/SERVICE/ANY)
```

## 📊 **Relationship Matrix**

| Entity | Type | Cardinality | Description |
|--------|------|-------------|-------------|
| **ProductType** | Direct | N:1 | Product belongs to ProductType |
| **Category** | Indirect | N:1 | Through ProductType |
| **Supplier** | Direct | 1:N | Supplier provides Products |
| **ProductAttribute** | Bridge | N:M | Through ProductAttributeValue |
| **Media** | Generic | 1:N | Product has multiple Media |
| **Service** | Bridge | N:M | Through ServiceProduct |
| **ServicePackage** | Bridge | N:M | Through ServicePackageProduct |
| **Promotion** | Direct | 1:N | Promotion targets Products |

## 🎯 **Key Features**

### **✅ Flexible Architecture**
- **Dynamic Attributes**: Thêm/sửa attributes mà không cần thay đổi schema
- **Generic Media**: Hệ thống media linh hoạt cho tất cả entities
- **Business Integration**: Tích hợp sâu với Service, Package, Promotion

### **✅ Data Integrity**
- **Unique Constraints**: productUrl, sku, productTypeCode
- **Foreign Keys**: Đảm bảo referential integrity
- **Composite Keys**: ProductAttributeValue với composite primary key

### **✅ Scalability**
- **Lazy Loading**: FetchType.LAZY cho performance
- **Cascade Operations**: CascadeType.ALL cho ProductAttributeValue
- **JSON Fields**: Flexible data storage cho complex fields

## 🔄 **Data Flow Example**

```
1. Create ProductType (Phụ tùng ô tô)
   ↓
2. Create Product (Bộ lọc dầu Toyota)
   ├── Assign ProductType
   ├── Set Supplier
   └── Add Attributes (Trọng lượng: 0.5kg, Bảo hành: 12 tháng)
   ↓
3. Upload Media (Hình ảnh sản phẩm)
   ↓
4. Create Service (Thay dầu nhớt)
   └── Add Product (Bộ lọc dầu Toyota, quantity: 1, price: 150,000)
   ↓
5. Create Package (Gói bảo dưỡng)
   └── Include Product (Bộ lọc dầu Toyota)
   ↓
6. Setup Promotion (Mua 2 tặng 1)
   └── Target Product (Bộ lọc dầu Toyota)
```

## 🚀 **API Endpoints**

### **Product Management**
- `POST /api/products/create` - Tạo product mới
- `GET /api/products/{id}` - Lấy thông tin product
- `PUT /api/products/{id}` - Cập nhật product
- `DELETE /api/products/{id}` - Xóa product (soft delete)

### **Product Attributes**
- `POST /api/product-attributes/create` - Tạo attribute mới
- `GET /api/product-attributes/all` - Lấy danh sách attributes
- `POST /api/product-attribute-values/add` - Thêm attribute value cho product

### **Media Management**
- `POST /api/media/create` - Upload media cho product
- `GET /api/media/entity/{entityType}/{entityId}` - Lấy media của product

### **Business Integration**
- `POST /api/services/{id}/products` - Thêm product vào service
- `POST /api/packages/{id}/products` - Thêm product vào package
- `GET /api/products/by-supplier/{supplierId}` - Lấy products theo supplier

## 📈 **Statistics & Analytics**

| Metric | Description |
|--------|-------------|
| **Total Products** | Tổng số products trong hệ thống |
| **Products by Type** | Phân bố products theo ProductType |
| **Products by Supplier** | Phân bố products theo Supplier |
| **Products with Media** | Số products có media |
| **Products in Services** | Số products được sử dụng trong services |
| **Products in Packages** | Số products được bao gồm trong packages |
| **Products in Promotions** | Số products tham gia promotions |

**Sơ đồ này cung cấp cái nhìn tổng quan về cách Product tích hợp với toàn bộ hệ thống SCSMS!** 🎯
