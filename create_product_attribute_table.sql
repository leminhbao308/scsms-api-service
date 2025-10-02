-- Create product_attribute table
CREATE TABLE IF NOT EXISTS dev.product_attribute (
    attribute_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attribute_name VARCHAR(100) NOT NULL,
    attribute_code VARCHAR(50) NOT NULL UNIQUE,
    unit VARCHAR(20),
    is_required BOOLEAN DEFAULT FALSE,
    data_type VARCHAR(20) DEFAULT 'STRING',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    modified_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_product_attribute_code ON dev.product_attribute(attribute_code);
CREATE INDEX IF NOT EXISTS idx_product_attribute_name ON dev.product_attribute(attribute_name);
CREATE INDEX IF NOT EXISTS idx_product_attribute_data_type ON dev.product_attribute(data_type);
CREATE INDEX IF NOT EXISTS idx_product_attribute_is_required ON dev.product_attribute(is_required);
CREATE INDEX IF NOT EXISTS idx_product_attribute_active ON dev.product_attribute(is_active);
CREATE INDEX IF NOT EXISTS idx_product_attribute_deleted ON dev.product_attribute(is_deleted);

-- Add check constraints
ALTER TABLE dev.product_attribute ADD CONSTRAINT chk_product_attribute_data_type 
    CHECK (data_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'DATE', 'DECIMAL', 'INTEGER', 'TEXT'));

-- Add comments
COMMENT ON TABLE dev.product_attribute IS 'Product attributes table for defining product characteristics';
COMMENT ON COLUMN dev.product_attribute.attribute_id IS 'Primary key - UUID';
COMMENT ON COLUMN dev.product_attribute.attribute_name IS 'Name of the attribute (e.g., Chiều dài, Chiều rộng, Trọng lượng)';
COMMENT ON COLUMN dev.product_attribute.attribute_code IS 'Unique code for the attribute (e.g., length, width, weight)';
COMMENT ON COLUMN dev.product_attribute.unit IS 'Unit of measurement (e.g., cm, kg, g, ml)';
COMMENT ON COLUMN dev.product_attribute.is_required IS 'Whether this attribute is required for products';
COMMENT ON COLUMN dev.product_attribute.data_type IS 'Data type of the attribute value';
COMMENT ON COLUMN dev.product_attribute.is_active IS 'Whether the attribute is active';
COMMENT ON COLUMN dev.product_attribute.is_deleted IS 'Soft delete flag';
COMMENT ON COLUMN dev.product_attribute.created_date IS 'Creation timestamp';
COMMENT ON COLUMN dev.product_attribute.modified_date IS 'Last modification timestamp';
COMMENT ON COLUMN dev.product_attribute.created_by IS 'User who created the record';
COMMENT ON COLUMN dev.product_attribute.modified_by IS 'User who last modified the record';
COMMENT ON COLUMN dev.product_attribute.version IS 'Optimistic locking version';

-- Insert sample data
INSERT INTO dev.product_attribute (attribute_name, attribute_code, unit, is_required, data_type) VALUES
('Chiều dài', 'length', 'cm', true, 'NUMBER'),
('Chiều rộng', 'width', 'cm', true, 'NUMBER'),
('Chiều cao', 'height', 'cm', false, 'NUMBER'),
('Trọng lượng', 'weight', 'kg', true, 'NUMBER'),
('Màu sắc', 'color', NULL, true, 'STRING'),
('Chất liệu', 'material', NULL, false, 'STRING'),
('Xuất xứ', 'origin', NULL, false, 'STRING'),
('Thương hiệu', 'brand', NULL, false, 'STRING'),
('Model', 'model', NULL, false, 'STRING'),
('Bảo hành', 'warranty', 'tháng', false, 'NUMBER'),
('Có sẵn', 'in_stock', NULL, false, 'BOOLEAN'),
('Ngày sản xuất', 'manufacturing_date', NULL, false, 'DATE'),
('Mô tả', 'description', NULL, false, 'TEXT')
ON CONFLICT (attribute_code) DO NOTHING;
