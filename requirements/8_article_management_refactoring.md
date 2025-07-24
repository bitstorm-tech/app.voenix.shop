# Product Requirements Document: Article Management System Refactoring

## 1. Executive Summary
Transform the current Mug-specific admin page into a generic Article management system that can handle multiple product types (mugs, shirts, pillows, etc.) while maintaining the variant system for product variations like colors.

## 2. Current State Analysis

### Generic Features (applicable to all articles):
- Basic information: name, descriptions (short/long), image, price
- Status: active/inactive
- Timestamps: created_at, updated_at
- Variant system (colors with example images)
- CRUD operations
- Search and filtering capabilities

### Mug-Specific Features:
- Physical dimensions: height, diameter (in mm)
- Print template specifications: width, height (in mm)
- Filling quantity (e.g., "250ml")
- Dishwasher safe flag

### Missing Features:
- Category assignment (categories exist but aren't linked to products)
- Article type discrimination
- Type-specific attribute storage

## 3. Proposed Article Abstraction Model

```
Article (Base Table: articles)
├── Common Attributes
│   ├── id, name, description_short, description_long
│   ├── main_image, price, active
│   ├── category_id (NOT NULL - mandatory)
│   ├── subcategory_id (NULL - optional)
│   ├── article_type (enum: MUG, SHIRT, PILLOW, etc.)
│   ├── created_at, updated_at
│   └── variants[] (one-to-many relationship)
│
├── Type-Specific Tables (1:1 relationship)
│   ├── article_mug_details
│   │   ├── article_id (FK)
│   │   ├── height_mm, diameter_mm
│   │   ├── print_template_width_mm, print_template_height_mm
│   │   ├── filling_quantity, dishwasher_safe
│   │   └── created_at, updated_at
│   │
│   ├── article_shirt_details
│   │   ├── article_id (FK)
│   │   ├── material, care_instructions
│   │   ├── fit_type (enum: REGULAR, SLIM, LOOSE)
│   │   ├── available_sizes (array: S, M, L, XL, XXL)
│   │   └── created_at, updated_at
│   │
│   └── article_pillow_details
│       ├── article_id (FK)
│       ├── width_cm, height_cm, depth_cm
│       ├── material, filling_type
│       ├── cover_removable, washable
│       └── created_at, updated_at
│
└── Relationships
    ├── Category (many-to-one, mandatory)
    ├── Subcategory (many-to-one, optional)
    └── Variants (one-to-many)
```

## 4. Detailed Requirements

### 4.1 Data Model Requirements

**R1: Article Base Entity**
- Must support common attributes shared by all product types
- Must include article_type field to discriminate between different products
- **Category assignment is MANDATORY** - every article must belong to a category
- **Subcategory assignment is OPTIONAL**
- Must maintain backward compatibility with existing mug data

**R2: Type-Specific Attributes**
- **Use separate tables for each article type** (e.g., article_mug_details, article_shirt_details)
- One-to-one relationship between article and its type-specific details
- Foreign key constraint ensures data integrity
- Allows for proper indexing and query optimization
- Type-specific validation at database level

**R3: Variant System**
- **All variants of an article share the same price** (inherited from parent article)
- Variant types by article:
  - Mugs: Color variants only (current system maintained)
  - Shirts: Color + Size combinations (stored as separate variant records)
  - Pillows: Color + Material combinations
- Variant structure:
  ```
  article_variants
  ├── id
  ├── article_id (FK)
  ├── variant_type (COLOR, SIZE, MATERIAL)
  ├── variant_value (e.g., "Red", "XL", "Cotton")
  ├── sku (unique identifier for inventory)
  ├── example_image_filename
  └── created_at, updated_at
  ```

### 4.2 Frontend Requirements

**R4: Article List Page**
- Display all articles regardless of type
- Show article type as a column/badge
- Filter by article type
- **Filter by category (mandatory field)**
- Filter by subcategory (when applicable)
- Type column shows user-friendly names (Mug, T-Shirt, Pillow)

**R5: Article Create/Edit Page**
- **Category selection is required** (validation error if not selected)
- Subcategory selection appears after category is chosen (optional)
- Dynamic form based on selected article type
- Common tabs for all articles:
  - Description (includes category selection)
  - Costs (single price for all variants)
  - Supplier
  - Shipping
- Type-specific tabs:
  - Mugs: "Specifications" tab with dimensions
  - Shirts: "Materials & Sizes" tab
  - Pillows: "Dimensions & Materials" tab
- Variants tab shows appropriate options based on article type

**R6: Navigation & Routing**
- Change `/admin/mugs/*` to `/admin/articles/*`
- Add article type filter in URL: `/admin/articles?type=mug`
- Maintain deep links for editing: `/admin/articles/{id}/edit`

### 4.3 Backend Requirements

**R7: API Structure**
- Base endpoint: `/api/admin/articles`
- Endpoints:
  - `GET /api/admin/articles` - List all (with type, category filters)
  - `GET /api/admin/articles/{id}` - Get with type-specific details
  - `POST /api/admin/articles` - Create (type specified in body)
  - `PUT /api/admin/articles/{id}` - Update
  - `DELETE /api/admin/articles/{id}` - Delete (cascades to details table)
- Category validation on create/update operations

**R8: Service Layer**
- ArticleService as base service with type-specific strategies
- Separate detail services:
  - MugDetailService
  - ShirtDetailService  
  - PillowDetailService
- Transaction management for article + details operations
- Validation includes mandatory category check

**R9: Database Schema**
```sql
-- Base article table
CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description_short TEXT NOT NULL,
    description_long TEXT NOT NULL,
    main_image VARCHAR(500) NOT NULL,
    price INTEGER NOT NULL,
    active BOOLEAN DEFAULT true,
    article_type VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES article_categories(id),
    subcategory_id BIGINT REFERENCES article_subcategories(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Mug-specific details
CREATE TABLE article_mug_details (
    article_id BIGINT PRIMARY KEY REFERENCES articles(id) ON DELETE CASCADE,
    height_mm INTEGER NOT NULL,
    diameter_mm INTEGER NOT NULL,
    print_template_width_mm INTEGER NOT NULL,
    print_template_height_mm INTEGER NOT NULL,
    filling_quantity VARCHAR(50),
    dishwasher_safe BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Similar tables for shirt_details, pillow_details, etc.
```

## 5. Edge Cases & Considerations

### E1: Data Migration
- No production data to migrate (development/empty database)
- Only structural changes needed
- Existing seed data can be recreated after migration

### E2: Variant Complexity
- **Uniform pricing**: Price changes apply to all variants
- Shirt variants: Create separate variant record for each color/size combo
- Example: Red T-Shirt in sizes S,M,L = 3 variant records
- Stock management per variant (future consideration)

### E3: Search & Filtering
- Global search across all article types
- Type-specific search fields joined from detail tables
- **Category filter always available** (since it's mandatory)
- Performance: Use proper indexes on article_type and category_id

### E4: Validation Rules
- **Category is required** for all articles
- Type-specific validation:
  - Mugs: dimensions required
  - Shirts: at least one size required
  - Pillows: dimensions required
- Variant validation based on article type

## 6. Migration Strategy

### Phase 1: Database Changes
1. Drop existing mug-related tables (mugs, mug_variants)
2. Create new article tables structure
3. Update category tables if needed

### Phase 2: Backend Implementation
1. Implement Article entity and type-specific detail entities
2. Create ArticleService with type strategies
3. Implement new `/api/admin/articles` endpoints
4. Remove old mug-specific code

### Phase 3: Frontend Update
1. Replace mug pages with article pages
2. Update routes from `/admin/mugs` to `/admin/articles`
3. Implement dynamic form based on article type
4. Update navigation and menu items

## 7. Technical Decisions Summary

1. **Separate tables for type-specific attributes** ✓
   - Better performance and data integrity
   - Clear schema definition
   - Type-safe queries

2. **Uniform variant pricing** ✓
   - Simplifies pricing logic
   - Price stored only on article level

3. **Mandatory categories** ✓
   - Every article must have a category
   - Subcategories remain optional

4. **No templates/presets** ✓
   - Reduces initial complexity

5. **No bulk import** ✓
   - Manual entry only for now

## 8. Success Criteria

1. All existing mug functionality preserved
2. Ability to add new article types with just:
   - New detail table
   - New service implementation
   - New form section
3. Unified admin interface for all article types
4. Proper category organization for all articles
5. Clean separation between common and type-specific logic
6. No performance degradation
7. Maintain data integrity with proper constraints