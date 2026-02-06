-- ==========================================================
-- data-dev.sql (DEV)
-- Timezone: JST (+09)
-- ==========================================================

-- ==========================================================
-- PLAN MANAGEMENT BC
-- ==========================================================
TRUNCATE planmanagement.plan_category CASCADE;
TRUNCATE planmanagement.plan_meal CASCADE;
TRUNCATE planmanagement.plan CASCADE;
TRUNCATE planmanagement.category CASCADE;
TRUNCATE planmanagement.address CASCADE;

-- ----------------------------------------------------------
-- address (5)
-- ----------------------------------------------------------
INSERT INTO planmanagement.address
(id, building_name_room_no, chome_ban_go, district, postal_code, city, prefecture, location)
VALUES
    ('aaaaaaaa-0000-0000-0000-000000000001','NAMBA HEIGHTS 101','1-2-3','NAMBA','542-0076','OSAKA','OSAKA',ST_GeogFromText('POINT(135.5016 34.6687)')),
    ('aaaaaaaa-0000-0000-0000-000000000002','UMEDA SKY 1203','2-4-8','UMEDA','530-0001','OSAKA','OSAKA',ST_GeogFromText('POINT(135.4959 34.7053)')),
    ('aaaaaaaa-0000-0000-0000-000000000003','KYOTO CENTRAL 305','3-1-12','KAWARAMACHI','600-8001','KYOTO','KYOTO',ST_GeogFromText('POINT(135.7681 35.0045)')),
    ('aaaaaaaa-0000-0000-0000-000000000004','TOKYO BAY 808','5-1-4','SHINAGAWA','108-0075','TOKYO','TOKYO',ST_GeogFromText('POINT(139.7380 35.6285)')),
    ('aaaaaaaa-0000-0000-0000-000000000005','KOBE HARBOR 502','4-2-9','HARBORLAND','650-0044','KOBE','HYOGO',ST_GeogFromText('POINT(135.1806 34.6792)')),
    ('aaaaaaaa-0000-0000-0000-000000000006','SHINSAIBASHI TOWER 902','1-7-1','SHINSAIBASHI','542-0085','OSAKA','OSAKA',ST_GeogFromText('POINT(135.5023 34.6735)')),
    ('aaaaaaaa-0000-0000-0000-000000000007','TENNOJI PARKSIDE 1104','5-55-10','TENNOJI','543-0063','OSAKA','OSAKA',ST_GeogFromText('POINT(135.5168 34.6532)')),
    ('aaaaaaaa-0000-0000-0000-000000000008','SANNOMIYA CENTRAL 403','2-11-3','SANNOMIYA','650-0021','KOBE','HYOGO',ST_GeogFromText('POINT(135.1955 34.6940)')),
    ('aaaaaaaa-0000-0000-0000-000000000009','GINZA RESIDENCE 1501','4-6-16','GINZA','104-0061','TOKYO','TOKYO',ST_GeogFromText('POINT(139.7671 35.6717)')),
    ('aaaaaaaa-0000-0000-0000-000000000010','YOKOHAMA BAYFRONT 707','1-1-7','MINATOMIRAI','220-0012','YOKOHAMA','KANAGAWA',ST_GeogFromText('POINT(139.6368 35.4576)'));

-- ----------------------------------------------------------
-- category (7)  ※ ALL CAPS
-- ----------------------------------------------------------
INSERT INTO planmanagement.category (id, name)
VALUES
    ('cccccccc-0000-0000-0000-000000000001','JAPANESE'),
    ('cccccccc-0000-0000-0000-000000000002','HEALTHY'),
    ('cccccccc-0000-0000-0000-000000000003','SEAFOOD'),
    ('cccccccc-0000-0000-0000-000000000004','MEAT'),
    ('cccccccc-0000-0000-0000-000000000005','VEGETARIAN'),
    ('cccccccc-0000-0000-0000-000000000006','SPICY'),
    ('cccccccc-0000-0000-0000-000000000007','PREMIUM');

-- ----------------------------------------------------------
-- plan (10)
-- user_id = provider (hard-coded UUIDs)
-- ----------------------------------------------------------
INSERT INTO planmanagement.plan
(id, code, title, description, plan_status, created_at, updated_at, user_id, skip_dates, address_id, display_subscription_fee, image_url, delete_flag, deleted_at)
VALUES
    ('10000000-0000-0000-0000-000000000001','AA00001','OSAKA CLASSIC','Balanced bento','ACTIVE','2026-01-01 10:00:00+09','2026-01-15 09:00:00+09','11111111-0000-0000-0000-000000000001','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000001',9800.00,'https://images.unsplash.com/photo-1546069901-ba9599a7e63c',false,NULL),
    ('10000000-0000-0000-0000-000000000002','AA00002','KYOTO HEALTHY','Light meals','RECRUITING','2026-01-02 10:00:00+09','2026-01-12 09:00:00+09','11111111-0000-0000-0000-000000000002','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000002',9200.00,'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe',false,NULL),
    ('10000000-0000-0000-0000-000000000003','AA00003','TOKYO SEAFOOD','Premium seafood','ACTIVE','2026-01-03 10:00:00+09','2026-01-18 09:00:00+09','11111111-0000-0000-0000-000000000003','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000003',12800.00,'https://upload.wikimedia.org/wikipedia/commons/3/37/Sushi_bento.jpg',false,NULL),
    ('10000000-0000-0000-0000-000000000004','AA00004','KOBE MEAT','Meat lovers','RECRUITING','2026-01-04 10:00:00+09','2026-01-14 09:00:00+09','11111111-0000-0000-0000-000000000004','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000004',11500.00,'https://upload.wikimedia.org/wikipedia/commons/9/9a/Gyuu-don_003.jpg',false,NULL),
    ('10000000-0000-0000-0000-000000000005','AA00005','TOKYO QUICK','Fast meals','ACTIVE','2026-01-05 10:00:00+09','2026-01-19 09:00:00+09','11111111-0000-0000-0000-000000000005','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000005',8900.00,'https://images.unsplash.com/photo-1555939594-58d7cb561ad1',false,NULL),
    ('10000000-0000-0000-0000-000000000006','AA00006','SPICY CURRY','Curry week','ACTIVE','2026-01-06 10:00:00+09','2026-01-20 09:00:00+09','11111111-0000-0000-0000-000000000006','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000006',9900.00,'https://upload.wikimedia.org/wikipedia/commons/4/44/Jiyuken_curry_rice_20100320.jpg',false,NULL),
    ('10000000-0000-0000-0000-000000000007','AA00007','VEGGIE JAPAN','Plant based','RECRUITING','2026-01-07 10:00:00+09','2026-01-17 09:00:00+09','11111111-0000-0000-0000-000000000007','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000007',9000.00,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg',false,NULL),
    ('10000000-0000-0000-0000-000000000008','AA00008','EKIBEN TRIP','Station bento','ACTIVE','2026-01-08 10:00:00+09','2026-01-21 09:00:00+09','11111111-0000-0000-0000-000000000008','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000008',10800.00,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg',false,NULL),
    ('10000000-0000-0000-0000-000000000009','AA00009','TEMPURA PREMIUM','Weekend tempura','RECRUITING','2026-01-09 10:00:00+09','2026-01-16 09:00:00+09','11111111-0000-0000-0000-000000000009','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000009',13500.00,'https://upload.wikimedia.org/wikipedia/commons/5/50/Bento_-_sushi_-_sashimi_-_ravioli.jpg',false,NULL),
    ('10000000-0000-0000-0000-000000000010','AA00010','GYUDON POWER','Beef bowl','ACTIVE','2026-01-10 10:00:00+09','2026-01-22 09:00:00+09','11111111-0000-0000-0000-000000000010','[]'::jsonb,'aaaaaaaa-0000-0000-0000-000000000010',9800.00,'https://upload.wikimedia.org/wikipedia/commons/9/9a/Gyuu-don_003.jpg',false,NULL);

-- ----------------------------------------------------------
-- plan_category (>=5, here 20)
-- ----------------------------------------------------------
INSERT INTO planmanagement.plan_category (plan_id, category_id)
VALUES
    ('10000000-0000-0000-0000-000000000001','cccccccc-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000001','cccccccc-0000-0000-0000-000000000004'),

    ('10000000-0000-0000-0000-000000000002','cccccccc-0000-0000-0000-000000000002'),
    ('10000000-0000-0000-0000-000000000002','cccccccc-0000-0000-0000-000000000005'),

    ('10000000-0000-0000-0000-000000000003','cccccccc-0000-0000-0000-000000000003'),
    ('10000000-0000-0000-0000-000000000003','cccccccc-0000-0000-0000-000000000007'),

    ('10000000-0000-0000-0000-000000000004','cccccccc-0000-0000-0000-000000000004'),
    ('10000000-0000-0000-0000-000000000004','cccccccc-0000-0000-0000-000000000007'),

    ('10000000-0000-0000-0000-000000000005','cccccccc-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000005','cccccccc-0000-0000-0000-000000000002'),

    ('10000000-0000-0000-0000-000000000006','cccccccc-0000-0000-0000-000000000006'),
    ('10000000-0000-0000-0000-000000000006','cccccccc-0000-0000-0000-000000000001'),

    ('10000000-0000-0000-0000-000000000007','cccccccc-0000-0000-0000-000000000005'),
    ('10000000-0000-0000-0000-000000000007','cccccccc-0000-0000-0000-000000000002'),

    ('10000000-0000-0000-0000-000000000008','cccccccc-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000008','cccccccc-0000-0000-0000-000000000003'),

    ('10000000-0000-0000-0000-000000000009','cccccccc-0000-0000-0000-000000000007'),
    ('10000000-0000-0000-0000-000000000009','cccccccc-0000-0000-0000-000000000003'),

    ('10000000-0000-0000-0000-000000000010','cccccccc-0000-0000-0000-000000000004'),
    ('10000000-0000-0000-0000-000000000010','cccccccc-0000-0000-0000-000000000001');

-- ----------------------------------------------------------
-- plan_meal (30)
-- Each plan has 3 meals
-- Some plans: 2 primary, some 3 primary
-- Some meals: current_sub_count > min_sub_count
-- ----------------------------------------------------------
INSERT INTO planmanagement.plan_meal
(id, plan_id, name, description, price_per_month, is_primary, min_sub_count, current_sub_count, image_url, created_at, updated_at, delete_flag, deleted_at)
VALUES
-- PLAN 1 (1 primary)
('20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','SALMON TERIYAKI','Grilled salmon + rice',4900.00,true, 3, 6,'https://images.unsplash.com/photo-1546069901-ba9599a7e63c','2026-01-01 10:05:00+09','2026-01-15 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000001','CHICKEN KARAAGE','Crispy fried chicken',4500.00,false,2, 1,'https://images.unsplash.com/photo-1555939594-58d7cb561ad1','2026-01-01 10:05:00+09','2026-01-15 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000001','ONIGIRI SET','Rice balls + sides',2800.00,false,1, 0,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg','2026-01-01 10:05:00+09','2026-01-15 09:05:00+09',false,NULL),

-- PLAN 2 (2 primary)
('20000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000002','TOFU SALAD','Tofu + greens',3600.00,true, 2, 3,'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe','2026-01-02 10:05:00+09','2026-01-12 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000002','GRILLED FISH','Light grilled fish',4100.00,true, 3, 1,'https://images.unsplash.com/photo-1553621042-f6e147245754','2026-01-02 10:05:00+09','2026-01-12 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000006','10000000-0000-0000-0000-000000000002','VEGGIE RICE','Veggie rice bowl',3000.00,false,1, 0,'https://images.unsplash.com/photo-1512621776951-a57141f2eefd','2026-01-02 10:05:00+09','2026-01-12 09:05:00+09',false,NULL),

-- PLAN 3 (3 primary)
('20000000-0000-0000-0000-000000000007','10000000-0000-0000-0000-000000000003','SUSHI BOX','Assorted sushi',5200.00,true, 5, 8,'https://upload.wikimedia.org/wikipedia/commons/3/37/Sushi_bento.jpg','2026-01-03 10:05:00+09','2026-01-18 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000008','10000000-0000-0000-0000-000000000003','TEMPURA MIX','Shrimp + veggie tempura',4800.00,true, 4, 4,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-03 10:05:00+09','2026-01-18 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000009','10000000-0000-0000-0000-000000000003','SASHIMI SET','Fresh sashimi',5400.00,true, 6, 6,'https://upload.wikimedia.org/wikipedia/commons/5/50/Bento_-_sushi_-_sashimi_-_ravioli.jpg','2026-01-03 10:05:00+09','2026-01-18 09:05:00+09',false,NULL),

-- PLAN 4 (1 primary)
('20000000-0000-0000-0000-000000000010','10000000-0000-0000-0000-000000000004','BEEF YAKINIKU','Sweet-savory beef',5200.00,true, 4, 5,'https://upload.wikimedia.org/wikipedia/commons/9/9a/Gyuu-don_003.jpg','2026-01-04 10:05:00+09','2026-01-14 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000011','10000000-0000-0000-0000-000000000004','PORK TONKATSU','Crispy cutlet',4900.00,false,3, 1,'https://images.unsplash.com/photo-1604908177522-937a6c8fb6a8','2026-01-04 10:05:00+09','2026-01-14 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000012','10000000-0000-0000-0000-000000000004','MISO VEG SIDES','Soup + veggies',2200.00,false,1, 0,'https://images.unsplash.com/photo-1548940740-204726a19be3','2026-01-04 10:05:00+09','2026-01-14 09:05:00+09',false,NULL),

-- PLAN 5 (2 primary)
('20000000-0000-0000-0000-000000000013','10000000-0000-0000-0000-000000000005','GRILLED SALMON RICE','Quick salmon meal',4200.00,true, 2, 6,'https://images.unsplash.com/photo-1546069901-ba9599a7e63c','2026-01-05 10:05:00+09','2026-01-19 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000014','10000000-0000-0000-0000-000000000005','EGG BENTO','Egg + rice',3900.00,true, 3, 3,'https://images.unsplash.com/photo-1525351484163-7529414344d8','2026-01-05 10:05:00+09','2026-01-19 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000015','10000000-0000-0000-0000-000000000005','ONIGIRI SNACK','Rice ball add-on',1900.00,false,1, 10,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg','2026-01-05 10:05:00+09','2026-01-19 09:05:00+09',false,NULL),

-- PLAN 6 (3 primary)
('20000000-0000-0000-0000-000000000016','10000000-0000-0000-0000-000000000006','OSAKA CURRY','Rich curry rice',4100.00,true, 4, 7,'https://upload.wikimedia.org/wikipedia/commons/4/44/Jiyuken_curry_rice_20100320.jpg','2026-01-06 10:05:00+09','2026-01-20 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000017','10000000-0000-0000-0000-000000000006','SPICY CURRY','Extra heat curry',4200.00,true, 5, 6,'https://upload.wikimedia.org/wikipedia/commons/4/44/Jiyuken_curry_rice_20100320.jpg','2026-01-06 10:05:00+09','2026-01-20 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000018','10000000-0000-0000-0000-000000000006','KATSU CURRY','Curry + cutlet',4700.00,true, 6, 6,'https://images.unsplash.com/photo-1604908177522-937a6c8fb6a8','2026-01-06 10:05:00+09','2026-01-20 09:05:00+09',false,NULL),

-- PLAN 7 (1 primary)
('20000000-0000-0000-0000-000000000019','10000000-0000-0000-0000-000000000007','VEGGIE TEMPURA','Vegetable tempura',3600.00,true, 2, 1,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-07 10:05:00+09','2026-01-17 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000020','10000000-0000-0000-0000-000000000007','SEASONAL VEG PLATE','Seasonal veggies',3300.00,false,1, 2,'https://images.unsplash.com/photo-1512621776951-a57141f2eefd','2026-01-07 10:05:00+09','2026-01-17 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000021','10000000-0000-0000-0000-000000000007','MISO SOUP SET','Soup + small sides',2400.00,false,2, 0,'https://images.unsplash.com/photo-1548940740-204726a19be3','2026-01-07 10:05:00+09','2026-01-17 09:05:00+09',false,NULL),

-- PLAN 8 (2 primary)
('20000000-0000-0000-0000-000000000022','10000000-0000-0000-0000-000000000008','EKIBEN SALMON','Station-style salmon',4200.00,true, 3, 3,'https://images.unsplash.com/photo-1546069901-ba9599a7e63c','2026-01-08 10:05:00+09','2026-01-21 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000023','10000000-0000-0000-0000-000000000008','EKIBEN EEL','Eel-inspired bento',4600.00,true, 5, 2,'https://images.unsplash.com/photo-1604908177522-937a6c8fb6a8','2026-01-08 10:05:00+09','2026-01-21 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000024','10000000-0000-0000-0000-000000000008','SIDE SUSHI','Small sushi add-on',2600.00,false,2, 7,'https://upload.wikimedia.org/wikipedia/commons/3/37/Sushi_bento.jpg','2026-01-08 10:05:00+09','2026-01-21 09:05:00+09',false,NULL),

-- PLAN 9 (3 primary)
('20000000-0000-0000-0000-000000000025','10000000-0000-0000-0000-000000000009','TEMPURA WEEKEND','Big tempura set',5200.00,true, 6, 9,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-09 10:05:00+09','2026-01-16 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000026','10000000-0000-0000-0000-000000000009','SUSHI WEEKEND','Sushi assortment',5400.00,true, 5, 5,'https://upload.wikimedia.org/wikipedia/commons/3/37/Sushi_bento.jpg','2026-01-09 10:05:00+09','2026-01-16 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000027','10000000-0000-0000-0000-000000000009','SASHIMI BONUS','Extra sashimi',5600.00,true, 7, 6,'https://upload.wikimedia.org/wikipedia/commons/5/50/Bento_-_sushi_-_sashimi_-_ravioli.jpg','2026-01-09 10:05:00+09','2026-01-16 09:05:00+09',false,NULL),

-- PLAN 10 (2 primary)
('20000000-0000-0000-0000-000000000028','10000000-0000-0000-0000-000000000010','GYUDON CLASSIC','Beef bowl',4200.00,true, 3, 4,'https://upload.wikimedia.org/wikipedia/commons/9/9a/Gyuu-don_003.jpg','2026-01-10 10:05:00+09','2026-01-22 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000029','10000000-0000-0000-0000-000000000010','GYUDON + ONIGIRI','Beef bowl + rice ball',4500.00,true, 4, 1,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg','2026-01-10 10:05:00+09','2026-01-22 09:05:00+09',false,NULL),
('20000000-0000-0000-0000-000000000030','10000000-0000-0000-0000-000000000010','MISO SIDE SET','Soup + small sides',1100.00,false,1, 12,'https://images.unsplash.com/photo-1548940740-204726a19be3','2026-01-10 10:05:00+09','2026-01-22 09:05:00+09',false,NULL);