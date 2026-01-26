-- ==========================================================
-- Dev seed data for Plan Management BC
-- Target schema: "planmanagement"
-- ==========================================================

-- Clean (optional)
TRUNCATE TABLE "planmanagement".plan_category RESTART IDENTITY CASCADE;
TRUNCATE TABLE "planmanagement".plan_meal RESTART IDENTITY CASCADE;
TRUNCATE TABLE "planmanagement".plan RESTART IDENTITY CASCADE;
TRUNCATE TABLE "planmanagement".category RESTART IDENTITY CASCADE;
TRUNCATE TABLE "planmanagement".address RESTART IDENTITY CASCADE;
TRUNCATE TABLE "planmanagement".job_run RESTART IDENTITY CASCADE;

-- ==========================================================
-- address (10 rows)
-- ==========================================================
INSERT INTO "planmanagement".address
(id, building_name_room_no, chome_ban_go, district, postal_code, city, prefecture, location)
VALUES
    ('a0000000-0000-0000-0000-000000000001','SUNNY HEIGHTS 101','1-2-3','NAMBA','542-0076','OSAKA','OSAKA', ST_GeogFromText('POINT(135.5016 34.6687)')),
    ('a0000000-0000-0000-0000-000000000002','RIVERSIDE TOWER 1203','2-4-8','UMEDA','530-0001','OSAKA','OSAKA', ST_GeogFromText('POINT(135.4959 34.7053)')),
    ('a0000000-0000-0000-0000-000000000003','PARK SIDE 305','3-1-12','TENNOJI','543-0055','OSAKA','OSAKA', ST_GeogFromText('POINT(135.5196 34.6506)')),
    ('a0000000-0000-0000-0000-000000000004','MAPLE RESIDENCE 502','1-5-20','KOBE SANNOMIYA','650-0021','KOBE','HYOGO', ST_GeogFromText('POINT(135.1956 34.6901)')),
    ('a0000000-0000-0000-0000-000000000005','HARBOR VIEW 808','4-2-9','KOBE HARBORLAND','650-0044','KOBE','HYOGO', ST_GeogFromText('POINT(135.1806 34.6792)')),
    ('a0000000-0000-0000-0000-000000000006','CENTRAL COURT 210','1-9-2','KYOTO KAWARAMACHI','600-8001','KYOTO','KYOTO', ST_GeogFromText('POINT(135.7681 35.0045)')),
    ('a0000000-0000-0000-0000-000000000007','SILK HOUSE 707','2-3-6','KYOTO GION','605-0074','KYOTO','KYOTO', ST_GeogFromText('POINT(135.7788 35.0037)')),
    ('a0000000-0000-0000-0000-000000000008','BAYFRONT 1401','5-1-4','TOKYO SHINAGAWA','108-0075','TOKYO','TOKYO', ST_GeogFromText('POINT(139.7380 35.6285)')),
    ('a0000000-0000-0000-0000-000000000009','SKY GARDEN 903','1-1-2','TOKYO SHIBUYA','150-0002','TOKYO','TOKYO', ST_GeogFromText('POINT(139.7016 35.6595)')),
    ('a0000000-0000-0000-0000-000000000010','LAKE VIEW 404','6-2-1','NAGOYA SAKAE','460-0008','NAGOYA','AICHI', ST_GeogFromText('POINT(136.9066 35.1670)'));

-- ==========================================================
-- category (7 rows, ALL CAPS)
-- ==========================================================
INSERT INTO "planmanagement".category (id, name) VALUES
                                                     ('c0000000-0000-0000-0000-000000000001','JAPANESE'),
                                                     ('c0000000-0000-0000-0000-000000000002','HEALTHY'),
                                                     ('c0000000-0000-0000-0000-000000000003','SEAFOOD'),
                                                     ('c0000000-0000-0000-0000-000000000004','MEAT'),
                                                     ('c0000000-0000-0000-0000-000000000005','VEGETARIAN'),
                                                     ('c0000000-0000-0000-0000-000000000006','SPICY'),
                                                     ('c0000000-0000-0000-0000-000000000007','PREMIUM');

-- ==========================================================
-- plan (10 rows)
-- user_id is random UUID for now (uuid_generate_v4())
-- skip_dates: max 2 elements
-- ==========================================================
INSERT INTO "planmanagement".plan
(id, code, title, description, plan_status, created_at, updated_at, user_id, skip_dates, address_id, display_subscription_fee, delete_flag, deleted_at)
VALUES
    ('10000000-0000-0000-0000-000000000001','AA00001','OSAKA CLASSIC BENTO','Balanced everyday bento with familiar favorites.','ACTIVE','2026-01-01 10:00:00','2026-01-15 09:00:00',uuid_generate_v4(),'[]'::jsonb,'a0000000-0000-0000-0000-000000000001',9800.00,false,NULL),
    ('10000000-0000-0000-0000-000000000002','AA00002','KYOTO HEALTHY BOWL','Lighter meals with veggies and clean proteins.','RECRUITING','2026-01-02 10:00:00','2026-01-12 09:00:00',uuid_generate_v4(),'["2026-01-25"]'::jsonb,'a0000000-0000-0000-0000-000000000006',9200.00,false,NULL),
    ('10000000-0000-0000-0000-000000000003','AA00003','SUSHI & TEMPURA SET','Seafood-forward plan with a premium feel.','ACTIVE','2026-01-03 10:00:00','2026-01-18 09:00:00',uuid_generate_v4(),'["2026-01-24","2026-01-25"]'::jsonb,'a0000000-0000-0000-0000-000000000008',12800.00,false,NULL),
    ('10000000-0000-0000-0000-000000000004','AA00004','KOBE MEAT LOVER','Hearty meat meals with rich flavors.','RECRUITING','2026-01-04 10:00:00','2026-01-14 09:00:00',uuid_generate_v4(),'[]'::jsonb,'a0000000-0000-0000-0000-000000000004',11500.00,false,NULL),
    ('10000000-0000-0000-0000-000000000005','AA00005','TOKYO QUICK CONBINI STYLE','Convenience-store inspired comfort bento.','ACTIVE','2026-01-05 10:00:00','2026-01-19 09:00:00',uuid_generate_v4(),'["2026-01-26"]'::jsonb,'a0000000-0000-0000-0000-000000000009',8900.00,false,NULL),
    ('10000000-0000-0000-0000-000000000006','AA00006','SPICY CURRY WEEK','Curry-focused plan with a spicy twist.','ACTIVE','2026-01-06 10:00:00','2026-01-20 09:00:00',uuid_generate_v4(),'["2026-01-28"]'::jsonb,'a0000000-0000-0000-0000-000000000002',9900.00,false,NULL),
    ('10000000-0000-0000-0000-000000000007','AA00007','VEGGIE JAPANESE','Plant-forward Japanese staples.','RECRUITING','2026-01-07 10:00:00','2026-01-17 09:00:00',uuid_generate_v4(),'["2026-01-23"]'::jsonb,'a0000000-0000-0000-0000-000000000003',9000.00,false,NULL),
    ('10000000-0000-0000-0000-000000000008','AA00008','EKIBEN TRIP','Station-bento inspired lineup.','ACTIVE','2026-01-08 10:00:00','2026-01-21 09:00:00',uuid_generate_v4(),'[]'::jsonb,'a0000000-0000-0000-0000-000000000010',10800.00,false,NULL),
    ('10000000-0000-0000-0000-000000000009','AA00009','TEMPURA & SUSHI PREMIUM','Crunchy tempura + sushi combo for weekends.','RECRUITING','2026-01-09 10:00:00','2026-01-16 09:00:00',uuid_generate_v4(),'["2026-01-31"]'::jsonb,'a0000000-0000-0000-0000-000000000005',13500.00,false,NULL),
    ('10000000-0000-0000-0000-000000000010','AA00010','GYUDON POWER','Beef-bowl focused plan for big appetite days.','ACTIVE','2026-01-10 10:00:00','2026-01-22 09:00:00',uuid_generate_v4(),'[]'::jsonb,'a0000000-0000-0000-0000-000000000007',9800.00,false,NULL);

-- ==========================================================
-- plan_category (>= 5 rows; we do more)
-- ==========================================================
INSERT INTO "planmanagement".plan_category (plan_id, category_id) VALUES
                                                                      ('10000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000001'),
                                                                      ('10000000-0000-0000-0000-000000000001','c0000000-0000-0000-0000-000000000004'),

                                                                      ('10000000-0000-0000-0000-000000000002','c0000000-0000-0000-0000-000000000002'),
                                                                      ('10000000-0000-0000-0000-000000000002','c0000000-0000-0000-0000-000000000005'),

                                                                      ('10000000-0000-0000-0000-000000000003','c0000000-0000-0000-0000-000000000003'),
                                                                      ('10000000-0000-0000-0000-000000000003','c0000000-0000-0000-0000-000000000007'),

                                                                      ('10000000-0000-0000-0000-000000000004','c0000000-0000-0000-0000-000000000004'),
                                                                      ('10000000-0000-0000-0000-000000000004','c0000000-0000-0000-0000-000000000007'),

                                                                      ('10000000-0000-0000-0000-000000000005','c0000000-0000-0000-0000-000000000001'),
                                                                      ('10000000-0000-0000-0000-000000000005','c0000000-0000-0000-0000-000000000002'),

                                                                      ('10000000-0000-0000-0000-000000000006','c0000000-0000-0000-0000-000000000006'),
                                                                      ('10000000-0000-0000-0000-000000000006','c0000000-0000-0000-0000-000000000001'),

                                                                      ('10000000-0000-0000-0000-000000000007','c0000000-0000-0000-0000-000000000005'),
                                                                      ('10000000-0000-0000-0000-000000000007','c0000000-0000-0000-0000-000000000002'),

                                                                      ('10000000-0000-0000-0000-000000000008','c0000000-0000-0000-0000-000000000001'),
                                                                      ('10000000-0000-0000-0000-000000000008','c0000000-0000-0000-0000-000000000003'),

                                                                      ('10000000-0000-0000-0000-000000000009','c0000000-0000-0000-0000-000000000007'),
                                                                      ('10000000-0000-0000-0000-000000000009','c0000000-0000-0000-0000-000000000003'),

                                                                      ('10000000-0000-0000-0000-000000000010','c0000000-0000-0000-0000-000000000004');

-- ==========================================================
-- plan_meal (30 rows; each plan has 3 meals)
-- Rules satisfied:
--  - Each plan has at least 1 primary
--  - Some plans have 2 primary (PLN0002, PLN0005, PLN0008, PLN0010)
--  - Some plans have 3 primary (PLN0003, PLN0006, PLN0009)
--  - Some current_sub_count surpass min_sub_count (multiple rows)
--
-- Image URLs (internet):
--  Sushi bento: https://upload.wikimedia.org/wikipedia/commons/archive/3/37/20080214211658%21Sushi_bento.jpg
--  Bento set:   https://upload.wikimedia.org/wikipedia/commons/2/2e/Sushi_bento_set_%283914404545%29.jpg
--  Bento mix:   https://upload.wikimedia.org/wikipedia/commons/5/50/Bento_-_sushi_-_sashimi_-_ravioli.jpg
--  Curry rice:  https://upload.wikimedia.org/wikipedia/commons/4/44/Jiyuken_curry_rice_20100320.jpg
--  Tempura:     https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg
--  Onigiri:     https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg
--  Ekiben:      https://upload.wikimedia.org/wikipedia/commons/4/47/Ekiben.jpg
--  Salmon bento:https://upload.wikimedia.org/wikipedia/commons/b/b8/Grilled_salmon_and_mentaiko_on_rice_bento_of_7-Eleven_in_Japan.jpg
--  Gyudon:      https://upload.wikimedia.org/wikipedia/commons/9/9a/Gyuu-don_003.jpg
-- ==========================================================
INSERT INTO "planmanagement".plan_meal
(id, plan_id, name, description, price_per_month, is_primary, min_sub_count, current_sub_count, image_url, created_at, updated_at, delete_flag, deleted_at)
VALUES
-- PLN0001 (1 primary)
('20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','TERIYAKI SALMON','Classic salmon bento.','4200.00',true, 3, 5,'https://upload.wikimedia.org/wikipedia/commons/b/b8/Grilled_salmon_and_mentaiko_on_rice_bento_of_7-Eleven_in_Japan.jpg','2026-01-01 10:05:00','2026-01-15 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000001','CHICKEN KARAAGE','Crispy fried chicken.','3800.00',false,2, 1,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-01 10:05:00','2026-01-15 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000001','ONIGIRI SET','Rice balls + sides.','2800.00',false,1, 0,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg','2026-01-01 10:05:00','2026-01-15 09:05:00',false,NULL),

-- PLN0002 (2 primary)
('20000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000002','SALAD & TOFU','Fresh salad with tofu.','3400.00',true, 2, 2,'https://upload.wikimedia.org/wikipedia/commons/2/2e/Sushi_bento_set_%283914404545%29.jpg','2026-01-02 10:05:00','2026-01-12 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000005','10000000-0000-0000-0000-000000000002','GRILLED FISH','Lightly grilled fish.','3600.00',true, 3, 1,'https://upload.wikimedia.org/wikipedia/commons/b/b8/Grilled_salmon_and_mentaiko_on_rice_bento_of_7-Eleven_in_Japan.jpg','2026-01-02 10:05:00','2026-01-12 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000006','10000000-0000-0000-0000-000000000002','VEGGIE RICE','Seasoned rice with vegetables.','3200.00',false,1, 4,'https://upload.wikimedia.org/wikipedia/commons/4/47/Ekiben.jpg','2026-01-02 10:05:00','2026-01-12 09:05:00',false,NULL),

-- PLN0003 (3 primary)
('20000000-0000-0000-0000-000000000007','10000000-0000-0000-0000-000000000003','SUSHI BOX','Assorted sushi.','5200.00',true, 5, 8,'https://upload.wikimedia.org/wikipedia/commons/archive/3/37/20080214211658%21Sushi_bento.jpg','2026-01-03 10:05:00','2026-01-18 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000008','10000000-0000-0000-0000-000000000003','TEMPURA MIX','Shrimp + veggie tempura.','4800.00',true, 4, 2,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-03 10:05:00','2026-01-18 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000009','10000000-0000-0000-0000-000000000003','SASHIMI SET','Fresh sashimi selection.','5400.00',true, 6, 6,'https://upload.wikimedia.org/wikipedia/commons/5/50/Bento_-_sushi_-_sashimi_-_ravioli.jpg','2026-01-03 10:05:00','2026-01-18 09:05:00',false,NULL),

-- PLN0004 (1 primary)
('20000000-0000-0000-0000-000000000010','10000000-0000-0000-0000-000000000004','BEEF YAKINIKU','Sweet-savory beef.','4800.00',true, 4, 3,'https://upload.wikimedia.org/wikipedia/commons/9/9a/Gyuu-don_003.jpg','2026-01-04 10:05:00','2026-01-14 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000011','10000000-0000-0000-0000-000000000004','PORK TONKATSU','Crispy cutlet.','4500.00',false,3, 1,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-04 10:05:00','2026-01-14 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000012','10000000-0000-0000-0000-000000000004','EGG & VEG SIDES','Simple sides set.','2200.00',false,1, 0,'https://upload.wikimedia.org/wikipedia/commons/4/47/Ekiben.jpg','2026-01-04 10:05:00','2026-01-14 09:05:00',false,NULL),

-- PLN0005 (2 primary)
('20000000-0000-0000-0000-000000000013','10000000-0000-0000-0000-000000000005','GRILLED SALMON RICE','Convenience-style salmon.','3900.00',true, 2, 6,'https://upload.wikimedia.org/wikipedia/commons/b/b8/Grilled_salmon_and_mentaiko_on_rice_bento_of_7-Eleven_in_Japan.jpg','2026-01-05 10:05:00','2026-01-19 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000014','10000000-0000-0000-0000-000000000005','MENTAIKO ON RICE','Spicy cod roe rice.','3600.00',true, 3, 3,'https://upload.wikimedia.org/wikipedia/commons/b/b8/Grilled_salmon_and_mentaiko_on_rice_bento_of_7-Eleven_in_Japan.jpg','2026-01-05 10:05:00','2026-01-19 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000015','10000000-0000-0000-0000-000000000005','ONIGIRI SNACK','Quick rice-ball add-on.','1900.00',false,1, 10,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg','2026-01-05 10:05:00','2026-01-19 09:05:00',false,NULL),

-- PLN0006 (3 primary)
('20000000-0000-0000-0000-000000000016','10000000-0000-0000-0000-000000000006','OSAKA CURRY','Rich curry rice.','4100.00',true, 4, 7,'https://upload.wikimedia.org/wikipedia/commons/4/44/Jiyuken_curry_rice_20100320.jpg','2026-01-06 10:05:00','2026-01-20 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000017','10000000-0000-0000-0000-000000000006','SPICY CURRY','Extra heat version.','4200.00',true, 5, 3,'https://upload.wikimedia.org/wikipedia/commons/4/44/Jiyuken_curry_rice_20100320.jpg','2026-01-06 10:05:00','2026-01-20 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000018','10000000-0000-0000-0000-000000000006','KATSU CURRY','Curry with crispy cutlet.','4700.00',true, 6, 6,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-06 10:05:00','2026-01-20 09:05:00',false,NULL),

-- PLN0007 (1 primary)
('20000000-0000-0000-0000-000000000019','10000000-0000-0000-0000-000000000007','VEGGIE TEMPURA','Vegetable tempura focus.','3600.00',true, 2, 1,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-07 10:05:00','2026-01-17 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000020','10000000-0000-0000-0000-000000000007','SEASONAL VEG PLATE','Seasonal vegetables.','3300.00',false,1, 2,'https://upload.wikimedia.org/wikipedia/commons/4/47/Ekiben.jpg','2026-01-07 10:05:00','2026-01-17 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000021','10000000-0000-0000-0000-000000000007','ONIGIRI & MISO','Simple and filling.','2400.00',false,2, 0,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg','2026-01-07 10:05:00','2026-01-17 09:05:00',false,NULL),

-- PLN0008 (2 primary)
('20000000-0000-0000-0000-000000000022','10000000-0000-0000-0000-000000000008','EKIBEN SALMON','Station-bento style salmon.','4200.00',true, 3, 3,'https://upload.wikimedia.org/wikipedia/commons/4/47/Ekiben.jpg','2026-01-08 10:05:00','2026-01-21 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000023','10000000-0000-0000-0000-000000000008','EKIBEN EEL','Eel on rice (inspired).','4600.00',true, 5, 2,'https://upload.wikimedia.org/wikipedia/commons/4/47/Ekiben.jpg','2026-01-08 10:05:00','2026-01-21 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000024','10000000-0000-0000-0000-000000000008','SIDE SUSHI','Small sushi add-on.','2600.00',false,2, 7,'https://upload.wikimedia.org/wikipedia/commons/archive/3/37/20080214211658%21Sushi_bento.jpg','2026-01-08 10:05:00','2026-01-21 09:05:00',false,NULL),

-- PLN0009 (3 primary)
('20000000-0000-0000-0000-000000000025','10000000-0000-0000-0000-000000000009','TEMPURA WEEKEND','Big tempura set.','5200.00',true, 6, 9,'https://upload.wikimedia.org/wikipedia/commons/8/8e/Tempura_bento_-_Boston%2C_MA.jpg','2026-01-09 10:05:00','2026-01-16 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000026','10000000-0000-0000-0000-000000000009','SUSHI WEEKEND','Sushi assortment.','5400.00',true, 5, 5,'https://upload.wikimedia.org/wikipedia/commons/2/2e/Sushi_bento_set_%283914404545%29.jpg','2026-01-09 10:05:00','2026-01-16 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000027','10000000-0000-0000-0000-000000000009','SASHIMI BONUS','Extra sashimi add-on.','5600.00',true, 7, 6,'https://upload.wikimedia.org/wikipedia/commons/5/50/Bento_-_sushi_-_sashimi_-_ravioli.jpg','2026-01-09 10:05:00','2026-01-16 09:05:00',false,NULL),

-- PLN0010 (2 primary)
('20000000-0000-0000-0000-000000000028','10000000-0000-0000-0000-000000000010','GYUDON CLASSIC','Beef bowl.','4200.00',true, 3, 4,'https://upload.wikimedia.org/wikipedia/commons/9/9a/Gyuu-don_003.jpg','2026-01-10 10:05:00','2026-01-22 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000029','10000000-0000-0000-0000-000000000010','GYUDON W/ ONIGIRI','Beef bowl + rice ball.','4500.00',true, 4, 1,'https://upload.wikimedia.org/wikipedia/commons/c/c2/Onigiri_001.jpg','2026-01-10 10:05:00','2026-01-22 09:05:00',false,NULL),
('20000000-0000-0000-0000-000000000030','10000000-0000-0000-0000-000000000010','MISO SIDE SET','Soup + small sides.','1100.00',false,1, 12,'https://upload.wikimedia.org/wikipedia/commons/4/47/Ekiben.jpg','2026-01-10 10:05:00','2026-01-22 09:05:00',false,NULL);

-- ==========================================================
-- job_run (5 rows)
-- ==========================================================
-- INSERT INTO "planmanagement".job_run
-- (id, job_type, period_start, period_end, status, started_at, finished_at,
--  total_targets, success_count, failure_count, results, error, created_at)
-- VALUES
--     ('50000000-0000-0000-0000-000000000001','PLAN_STATUS_SYNC','2026-01-01','2026-01-07','SUCCESS','2026-01-08 01:00:00','2026-01-08 01:00:10',10,10,0,'[]'::jsonb,NULL,'2026-01-08 01:00:10'),
--     ('50000000-0000-0000-0000-000000000002','SCHEDULE_GEN','2026-01-08','2026-01-14','PARTIAL_SUCCESS','2026-01-15 01:10:00','2026-01-15 01:10:30',10,9,1,'[]'::jsonb,'{"message":"1 plan skipped due to validation"}'::jsonb,'2026-01-15 01:10:30'),
--     ('50000000-0000-0000-0000-000000000003','SUB_COUNT_REFRESH','2026-01-15','2026-01-21','SUCCESS','2026-01-22 02:00:00','2026-01-22 02:00:12',30,30,0,'[]'::jsonb,NULL,'2026-01-22 02:00:12'),
--     ('50000000-0000-0000-0000-000000000004','PLAN_CLEANUP','2026-01-01','2026-01-31','FAILED','2026-02-01 03:00:00','2026-02-01 03:00:05',10,0,10,'[]'::jsonb,'{"message":"DB lock timeout"}'::jsonb,'2026-02-01 03:00:05'),
--     ('50000000-0000-0000-0000-000000000005','PLAN_STATUS_SYNC','2026-01-22','2026-01-28','SUCCESS','2026-01-29 01:00:00','2026-01-29 01:00:09',10,10,0,'[]'::jsonb,NULL,'2026-01-29 01:00:09');

-- Note: delivery_schedule / delivery_schedule_detail are intentionally not seeded.