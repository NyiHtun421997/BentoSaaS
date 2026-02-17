-- ==========================================================
-- Dev seed data for USER-INFO / AUTH BC
-- Target schema: "userinfo"
-- Rules:
--   - addresses = users (21 each)
--   - user 1/2/3 keep their own encrypted_password values
--   - all other users use a shared bcrypt string
-- ==========================================================

-- TRUNCATE TABLE "userinfo".user_role RESTART IDENTITY CASCADE;
-- TRUNCATE TABLE "userinfo"."user" RESTART IDENTITY CASCADE;
-- TRUNCATE TABLE "userinfo".address RESTART IDENTITY CASCADE;
-- TRUNCATE TABLE "userinfo".role RESTART IDENTITY CASCADE;

-- ==========================================================
-- role (3 rows)
-- ==========================================================
INSERT INTO "userinfo".role (id, name)
VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_USER'),
    (3, 'ROLE_PROVIDER')^^

-- ==========================================================
-- address (21 rows)  ※ unique per user
-- ==========================================================
INSERT INTO "userinfo".address
(id, building_name_room_no, chome_ban_go, district, postal_code, city, prefecture)
VALUES
    ('aaaaaaaa-0000-0000-0000-000000000001','SUNNY HEIGHTS 101','1-2-3','NAMBA','542-0076','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000002','RIVERSIDE TOWER 1203','2-4-8','UMEDA','530-0001','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000003','SKY GARDEN 903','1-1-2','SHIBUYA','150-0002','TOKYO','TOKYO'),

    ('aaaaaaaa-0000-0000-0000-000000000004','TENNOJI PARKSIDE 1104','5-55-10','TENNOJI','543-0063','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000005','SHINSAIBASHI TOWER 902','1-7-1','SHINSAIBASHI','542-0085','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000006','KOBE HARBOR 502','4-2-9','HARBORLAND','650-0044','KOBE','HYOGO'),
    ('aaaaaaaa-0000-0000-0000-000000000007','SANNOMIYA CENTRAL 403','2-11-3','SANNOMIYA','650-0021','KOBE','HYOGO'),
    ('aaaaaaaa-0000-0000-0000-000000000008','KYOTO CENTRAL 305','3-1-12','KAWARAMACHI','600-8001','KYOTO','KYOTO'),
    ('aaaaaaaa-0000-0000-0000-000000000009','GINZA RESIDENCE 1501','4-6-16','GINZA','104-0061','TOKYO','TOKYO'),
    ('aaaaaaaa-0000-0000-0000-000000000010','YOKOHAMA BAYFRONT 707','1-1-7','MINATOMIRAI','220-0012','YOKOHAMA','KANAGAWA'),

    ('aaaaaaaa-0000-0000-0000-000000000011','NAMBA HEIGHTS 201','1-2-3','NAMBA','542-0076','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000012','UMEDA SKY 1402','2-4-8','UMEDA','530-0001','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000013','TOKYO BAY 808','5-1-4','SHINAGAWA','108-0075','TOKYO','TOKYO'),
    ('aaaaaaaa-0000-0000-0000-000000000014','KOBE SANNOMIYA 601','2-11-3','SANNOMIYA','650-0021','KOBE','HYOGO'),
    ('aaaaaaaa-0000-0000-0000-000000000015','KYOTO GION 707','2-3-6','GION','605-0074','KYOTO','KYOTO'),
    ('aaaaaaaa-0000-0000-0000-000000000016','OSAKA UMEDA 2201','2-4-8','UMEDA','530-0001','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000017','OSAKA NAMBA 505','1-2-3','NAMBA','542-0076','OSAKA','OSAKA'),
    ('aaaaaaaa-0000-0000-0000-000000000018','TOKYO SHIBUYA 1202','1-1-2','SHIBUYA','150-0002','TOKYO','TOKYO'),
    ('aaaaaaaa-0000-0000-0000-000000000019','NAGOYA SAKAE 404','6-2-1','SAKAE','460-0008','NAGOYA','AICHI'),
    ('aaaaaaaa-0000-0000-0000-000000000020','KOBE HARBOR 808','4-2-9','HARBORLAND','650-0044','KOBE','HYOGO')^^

-- ==========================================================
-- user (21 rows)
-- Users 1/2/3 keep your custom bcrypt strings.
-- Others use the shared bcrypt string you provided.
-- ==========================================================
INSERT INTO "userinfo"."user"
(user_id, email, encrypted_password, first_name, last_name, address_id, ph_no, created_at, updated_at, description, image_key)
VALUES
-- (1) end-user admin
('22222222-0000-0000-0000-000000000001','admin@bento.dev',
 '$2a$10$tDtl.NzIKQ.j7ENhGIPj4OfBRouIQ3FLlQCXaHhKH6e1L8GvOcSM.',
 'Admin','User','aaaaaaaa-0000-0000-0000-000000000001','090-1111-1111',
 '2026-01-01 09:00:00','2026-01-01 09:00:00','System administrator', 'user-admin.jpg'),

-- (2) end-user
('22222222-0000-0000-0000-000000000002','user1@bento.dev',
 '$2a$10$5JpyCBetWxOABgqFI5nYzeDU8L2ivdIy/ZJifCB4GnOKUWNfNSiya',
 'Taro','Yamada','aaaaaaaa-0000-0000-0000-000000000002','090-2222-2222',
 '2026-01-05 10:30:00','2026-01-10 12:00:00','End user', 'user-taro.jpg'),

-- (3) end-user
('22222222-0000-0000-0000-000000000003','user2@bento.dev',
 '$2a$10$Kkn3s4X5XIRqJvj2Pkp/PeTz6eoLB/t5mWKp6wv9DIDtkihVF74.G',
 'Hanako','Suzuki','aaaaaaaa-0000-0000-0000-000000000003','090-3333-3333',
 '2026-01-07 14:00:00','2026-01-15 16:20:00','End user', 'user-hanako.jpg'),

-- (4) .. (10) end-users for subscription
('22222222-0000-0000-0000-000000000004','user4@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Ken','Tanaka','aaaaaaaa-0000-0000-0000-000000000004','090-4444-4444',
 '2026-01-04 09:00:00','2026-01-04 09:00:00','End user', 'user-ken.jpg'),

('22222222-0000-0000-0000-000000000005','user5@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Yui','Sato','aaaaaaaa-0000-0000-0000-000000000005','090-5555-5555',
 '2026-01-05 09:00:00','2026-01-05 09:00:00','End user', 'user-yui.jpg'),

('22222222-0000-0000-0000-000000000006','user6@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Daichi','Ito','aaaaaaaa-0000-0000-0000-000000000006','090-6666-6666',
 '2026-01-06 09:00:00','2026-01-06 09:00:00','End user', 'user-daichi.jpg'),

('22222222-0000-0000-0000-000000000007','user7@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Miki','Kato','aaaaaaaa-0000-0000-0000-000000000007','090-7777-7777',
 '2026-01-07 09:00:00','2026-01-07 09:00:00','End user', 'user-miki.jpg'),

('22222222-0000-0000-0000-000000000008','user8@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Sota','Nakamura','aaaaaaaa-0000-0000-0000-000000000008','090-8888-8888',
 '2026-01-08 09:00:00','2026-01-08 09:00:00','End user', 'user-sota.jpg'),

('22222222-0000-0000-0000-000000000009','user9@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Aoi','Kobayashi','aaaaaaaa-0000-0000-0000-000000000009','090-9999-9999',
 '2026-01-09 09:00:00','2026-01-09 09:00:00','End user', 'user-aoi.jpg'),

('22222222-0000-0000-0000-000000000010','user10@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Rina','Yoshida','aaaaaaaa-0000-0000-0000-000000000010','090-1010-1010',
 '2026-01-10 09:00:00','2026-01-10 09:00:00','End user', 'user-rina.jpg'),

-- (11) .. (20) providers for planmanagement/subscription.provided_user_id
('11111111-0000-0000-0000-000000000001','provider1@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','One','aaaaaaaa-0000-0000-0000-000000000011','080-1111-1111',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider1.jpg'),

('11111111-0000-0000-0000-000000000002','provider2@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Two','aaaaaaaa-0000-0000-0000-000000000012','080-2222-2222',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider2.jpg'),

('11111111-0000-0000-0000-000000000003','provider3@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Three','aaaaaaaa-0000-0000-0000-000000000013','080-3333-3333',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider3.jpg'),

('11111111-0000-0000-0000-000000000004','provider4@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Four','aaaaaaaa-0000-0000-0000-000000000014','080-4444-4444',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider4.jpg'),

('11111111-0000-0000-0000-000000000005','provider5@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Five','aaaaaaaa-0000-0000-0000-000000000015','080-5555-5555',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider5.jpg'),

('11111111-0000-0000-0000-000000000006','provider6@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Six','aaaaaaaa-0000-0000-0000-000000000016','080-6666-6666',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider6.jpg'),

('11111111-0000-0000-0000-000000000007','provider7@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Seven','aaaaaaaa-0000-0000-0000-000000000017','080-7777-7777',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider7.jpg'),

('11111111-0000-0000-0000-000000000008','provider8@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Eight','aaaaaaaa-0000-0000-0000-000000000018','080-8888-8888',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider8.jpg'),

('11111111-0000-0000-0000-000000000009','provider9@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Nine','aaaaaaaa-0000-0000-0000-000000000019','080-9999-9999',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider9.jpg'),

('11111111-0000-0000-0000-000000000010','provider10@bento.dev',
 '$2a$10$b.Ro7SO7qVgOxIBjd5U12eZnJZrshpiqDHQAGDXLDWfA0vGWzmZea',
 'Provider','Ten','aaaaaaaa-0000-0000-0000-000000000020','080-1010-1010',
 '2025-12-01 09:00:00','2026-01-01 09:00:00','Provider', 'user-provider10.jpg')^^

-- ==========================================================
-- user_role
--   - admin: ADMIN + USER + PROVIDER
--   - end-users: USER
--   - providers: PROVIDER
-- ==========================================================
INSERT INTO "userinfo".user_role (user_id, role_id)
VALUES
-- admin
('22222222-0000-0000-0000-000000000001', 1),
('22222222-0000-0000-0000-000000000001', 2),
('22222222-0000-0000-0000-000000000001', 3),

-- end-users (subscription.user_id)
('22222222-0000-0000-0000-000000000002', 2),
('22222222-0000-0000-0000-000000000003', 2),
('22222222-0000-0000-0000-000000000004', 2),
('22222222-0000-0000-0000-000000000005', 2),
('22222222-0000-0000-0000-000000000006', 2),
('22222222-0000-0000-0000-000000000007', 2),
('22222222-0000-0000-0000-000000000008', 2),
('22222222-0000-0000-0000-000000000009', 2),
('22222222-0000-0000-0000-000000000010', 2),

-- providers (planmanagement.plan.user_id / subscription.provided_user_id)
('11111111-0000-0000-0000-000000000001', 3),
('11111111-0000-0000-0000-000000000002', 3),
('11111111-0000-0000-0000-000000000003', 3),
('11111111-0000-0000-0000-000000000004', 3),
('11111111-0000-0000-0000-000000000005', 3),
('11111111-0000-0000-0000-000000000006', 3),
('11111111-0000-0000-0000-000000000007', 3),
('11111111-0000-0000-0000-000000000008', 3),
('11111111-0000-0000-0000-000000000009', 3),
('11111111-0000-0000-0000-000000000010', 3)^^