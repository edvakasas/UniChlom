-- Two workshops (tenants). Every price-list row names the workshop it belongs to.
-- "Autoservisas Alfa" uses the prices from the Stage I design document; "Autoservisas Beta" has its own.

INSERT INTO workshop (id, name, hourly_rate, parts_markup_percent, regular_customer_discount_percent, vat_percent,
                      coefficient_lengvasis, coefficient_visureigis, coefficient_mikroautobusas)
VALUES ('alfa', 'Autoservisas Alfa', 40.00, 20, 5, 21, 1.00, 1.20, 1.30),
       ('beta', 'Autoservisas Beta', 45.00, 15, 10, 21, 1.00, 1.15, 1.25);

INSERT INTO work_item (id, tenant_id, code, name, norm_hours, shared_operation, shared_operation_hours, promotion_percent)
VALUES (1, 'alfa', 'D01', 'Priekinių stabdžių kaladėlių keitimas', 1.0, 'PRIEKINIAI_RATAI', 0.4, NULL),
       (2, 'alfa', 'D02', 'Priekinių stabdžių diskų keitimas', 1.6, 'PRIEKINIAI_RATAI', 0.4, NULL),
       (3, 'alfa', 'D03', 'Variklio alyvos ir filtro keitimas', 0.5, NULL, NULL, NULL),
       (4, 'alfa', 'D05', 'Kompiuterinė diagnostika', 0.5, NULL, NULL, 20),
       (5, 'beta', 'B01', 'Ratų balansavimas (4 vnt.)', 0.8, NULL, NULL, NULL),
       (6, 'beta', 'B02', 'Padangų montavimas (4 vnt.)', 1.0, NULL, NULL, 10),
       (7, 'beta', 'B03', 'Pakabos diagnostika', 0.5, NULL, NULL, NULL),
       (8, 'beta', 'B04', 'Galinių stabdžių kaladėlių keitimas', 1.2, 'GALINIAI_RATAI', 0.4, NULL),
       (9, 'beta', 'B05', 'Galinių stabdžių diskų keitimas', 1.8, 'GALINIAI_RATAI', 0.4, NULL);
