CREATE OR REPLACE VIEW accessible_sites AS
WITH
-- Préparer les scopes d'accès
site_scope AS (
  SELECT assignment_id, site_id
  FROM assignment_site_scope
),
org_scope AS (
  SELECT assignment_id, brand_id, group_id
  FROM assignment_org_scope
),
geo_scope AS (
  SELECT assignment_id, country_code, region_id
  FROM assignment_geo_scope
)
SELECT DISTINCT
    ua.id AS assignment_id,
    u.id AS user_id,
    u.email AS user_email,
    r.name AS role_name,
    s.id AS site_id,
    s.name AS site_name,
    s.address,
    s.type,
    b.id AS brand_id,
    b.name AS brand_name,
    g.id AS group_id,
    g.name AS group_name,
    s.country_code,
    c.name AS country_name,
    rg.id AS region_id,
    rg.name AS region_name
FROM user_assignment ua
         JOIN users u ON u.id = ua.user_id
         JOIN roles r ON r.id = ua.role_id
         JOIN sites s ON TRUE
         JOIN brands b ON b.id = s.brand_id
         JOIN groups g ON g.id = b.group_id
         JOIN countries c ON c.code = s.country_code
         JOIN regions rg ON rg.id = c.region_id

-- Joindre les scopes de manière explicite
         LEFT JOIN site_scope ss ON ss.assignment_id = ua.id AND ss.site_id = s.id
         LEFT JOIN org_scope os ON os.assignment_id = ua.id
         LEFT JOIN geo_scope gs ON gs.assignment_id = ua.id

WHERE
   -- 1) Site explicitement accessible
    ss.site_id IS NOT NULL

   OR
   -- 2) Org + Geo présents et tous deux matchent
    (
        os.assignment_id IS NOT NULL
            AND gs.assignment_id IS NOT NULL
            AND (os.brand_id = s.brand_id OR os.group_id = b.group_id)
            AND (gs.country_code = s.country_code OR gs.region_id = c.region_id)
        )

   OR
   -- 3) Org only
    (
        os.assignment_id IS NOT NULL
            AND gs.assignment_id IS NULL
            AND (os.brand_id = s.brand_id OR os.group_id = b.group_id)
        )

   OR
   -- 4) Geo only
    (
        gs.assignment_id IS NOT NULL
            AND os.assignment_id IS NULL
            AND (gs.country_code = s.country_code OR gs.region_id = c.region_id)
        );
