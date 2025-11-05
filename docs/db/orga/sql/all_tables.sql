CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- BUSINESS MODEL
CREATE TABLE groups (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE brands (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                        name VARCHAR(255) NOT NULL,
                        segment VARCHAR(100),
                        UNIQUE (group_id, name)
);

CREATE TABLE regions (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         name VARCHAR(255) UNIQUE NOT NULL,
                         code VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE countries (
                           code CHAR(2) PRIMARY KEY,
                           region_id UUID NOT NULL REFERENCES regions(id) ON DELETE CASCADE,
                           name VARCHAR(255) NOT NULL
);

CREATE TABLE sites (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       brand_id UUID NOT NULL REFERENCES brands(id) ON DELETE CASCADE,
                       country_code CHAR(2) NOT NULL REFERENCES countries(code),
                       name VARCHAR(255) NOT NULL,
                       address VARCHAR(255),
                       type VARCHAR(50),
                       latitude DECIMAL,
                       longitude DECIMAL
);

-- SECURITY MODEL
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255),
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description TEXT
);

CREATE TABLE user_assignment (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                                 assigned_at TIMESTAMP DEFAULT NOW(),
                                 assigned_by UUID REFERENCES users(id)
);

CREATE TABLE assignment_org_scope (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      assignment_id UUID NOT NULL REFERENCES user_assignment(id) ON DELETE CASCADE,
                                      group_id UUID REFERENCES groups(id),
                                      brand_id UUID REFERENCES brands(id)
);

CREATE TABLE assignment_geo_scope (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      assignment_id UUID NOT NULL REFERENCES user_assignment(id) ON DELETE CASCADE,
                                      region_id UUID REFERENCES regions(id),
                                      country_code CHAR(2) REFERENCES countries(code)
);

CREATE TABLE assignment_site_scope (
                                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                       assignment_id UUID NOT NULL REFERENCES user_assignment(id) ON DELETE CASCADE,
                                       site_id UUID REFERENCES sites(id)
);
