CREATE TABLE artists
(
    id        UUID         PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE genres
(
    id        UUID         PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE user_purposes
(
    id   UUID        PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE cities
(
    id   UUID         PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE users
(
    id               UUID                     PRIMARY KEY,
    email            VARCHAR(254)             NOT NULL UNIQUE,
    hashed_password  TEXT                     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    account_name     VARCHAR(40) UNIQUE,
    image_url        VARCHAR(1000),
    about            VARCHAR(1500),
    city             UUID                     REFERENCES cities(id),

    CONSTRAINT       user_email_ext_key UNIQUE (email)
);

CREATE TABLE potential_interlocutors_users
(
    potential_interlocutor_id UUID          NOT NULL REFERENCES users(id),
    user_id                   UUID          NOT NULL REFERENCES users(id),
    match_percent             DECIMAL(5, 2) NOT NULL,

    PRIMARY KEY(potential_interlocutor_id, user_id)
);

CREATE TABLE chats
(
    id                     UUID                     PRIMARY KEY,
    initiator_id           UUID                     NOT NULL REFERENCES users(id),
    interlocutor_id        UUID                     NOT NULL REFERENCES users(id),
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    last_sent_message      VARCHAR(3500),
    last_time_message_sent TIMESTAMP WITH TIME ZONE,

    UNIQUE (initiator_id, interlocutor_id)
);

CREATE TABLE messages
(
    id         UUID                     PRIMARY KEY,
    chat_id    UUID                     NOT NULL REFERENCES chats(id),
    sender_id  UUID                     NOT NULL REFERENCES users(id),
    text       VARCHAR(3500)            NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE favorite_artists_users
(
    favorite_artist_id UUID NOT NULL REFERENCES artists(id),
    user_id            UUID NOT NULL REFERENCES users(id),

    PRIMARY KEY(favorite_artist_id, user_id)
);

CREATE TABLE user_purposes_users
(
    user_purpose_id UUID NOT NULL REFERENCES user_purposes(id),
    user_id         UUID NOT NULL REFERENCES users(id),

    PRIMARY KEY(user_purpose_id, user_id)
);

CREATE TABLE favorite_genres_users
(
    favorite_genre_id UUID NOT NULL REFERENCES genres(id),
    user_id           UUID NOT NULL REFERENCES users(id),

    PRIMARY KEY(favorite_genre_id, user_id)
);

CREATE TABLE genres_artists
(
    genre_id  UUID NOT NULL REFERENCES genres(id),
    artist_id UUID NOT NULL REFERENCES artists(id),

    PRIMARY KEY(genre_id, artist_id)
);

--CREATE TABLE user_matches
--(
--    user_id          UUID NOT NULL REFERENCES users(id),
--    matched_user_id  UUID NOT NULL REFERENCES users(id),
--    match_percent    DECIMAL(5, 2) NOT NULL,
--
--    PRIMARY KEY(user_id, matched_user_id),
--    CHECK (match_percent >= 0.00 AND match_percent <= 100.00)
--);

INSERT INTO artists VALUES
    ('f7436977-6b4e-4389-acc1-63b406a2442d', 'The Beatles', 'https://i.discogs.com/298Ybei60A5z3JHm2qMTI6x3rCjUDwMO-fkz-lNhVHE/rs:fit/g:sm/q:90/h:523/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTgyNzMw/LTE3MDY4MjY3NTEt/Nzg4OS5qcGVn.jpeg'),
    ('b640640a-040f-4134-a520-10f7b9cf0511', 'Nirvana', 'https://i.discogs.com/BS3W-WDYXuQMwoS8pwYBAvXIxT1mNKeLeg7W0A9l5sk/rs:fit/g:sm/q:90/h:450/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyNTI0/Ni0xNTI5MTk4Mjg4/LTY0MzAuanBlZw.jpeg'),
    ('aac7e0b2-0444-4ee3-bf87-9c82865ade70', 'Led Zeppelin', 'https://i.discogs.com/Kz3a4rLnJSgJRLMu_mug-PecWrbud7368sQmVy9L2ls/rs:fit/g:sm/q:90/h:429/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTM0Mjc4/LTE2MTg4ODEyMjkt/NDI4MC5qcGVn.jpeg'),
    ('32f14d9d-72b2-4a5a-b14b-31396102270e', 'Дайте Танк (!)', 'https://i.discogs.com/zoItq1aVVRRLP8Pe6KhbNYSIKsDC-11gHKYH4hhg8S8/rs:fit/g:sm/q:90/h:600/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTUzNDc0/ODItMTYxNjA2NzM4/OC0yNDczLmpwZWc.jpeg'),
    ('9e22ca12-c794-4f33-ad0d-db9214ed519f', 'Кино', 'https://i.discogs.com/M2bdOxuH2mY_f9KxXEC7lcOy0cpt-gtpwJZYFsQlGfo/rs:fit/g:sm/q:90/h:337/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTM0NTI3/OC0xNzExMjU2MTE4/LTM0NjcuanBlZw.jpeg'),
    ('771849d3-741c-447c-8b4d-422cfc0e7d28', 'Браво', 'https://i.discogs.com/xdqgca3Ymzwznvg9nCspdnN5jcjb_Gm7m0bDfPzOwlI/rs:fit/g:sm/q:90/h:400/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTQ4NDQy/NS0xNjgzNzkxNTI3/LTcyMjkuanBlZw.jpeg'),
    ('f493a268-bec1-4ef1-ba93-e75ae8f692f9', 'Секрет', 'https://i.discogs.com/QhQTCCM1AGhZBJzzBwttMreFsNsQqBqpW5e21N8HVrQ/rs:fit/g:sm/q:90/h:212/w:290/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTgwODM4/Ni0xMjQ3NjQ1NTA4/LmpwZWc.jpeg'),
    ('a9f23cec-6f44-4710-aa64-1a6eb789a395', 'Король и Шут', 'https://i.discogs.com/MHm1N3HwUDRG1v_kIQN2k66H8RebnJKUlYUvhmlAzys/rs:fit/g:sm/q:90/h:333/w:500/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTM4NjQ2/OC0xMzQwNTQzNzkw/LTE0MjIuanBlZw.jpeg'),
    ('4769881f-76d8-42e6-9972-6a1c4ead555a', 'a-ha', 'https://i.discogs.com/PIj7YL2KNMtqkXKS_XEurIBEpUCb7M1QlfPs7MpFPrk/rs:fit/g:sm/q:90/h:300/w:475/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTM3NzU3/LTE0NDczMjQ1NjEt/NDYwNy5qcGVn.jpeg'),
    ('b0a6381a-219a-40cd-9bac-c407ed87cdc3', 'The Police', 'https://i.discogs.com/Fl52eWHQzgOgiTwFYAUpSPE9-fiPQADAnX0fz2pQDs8/rs:fit/g:sm/q:90/h:596/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTc5ODct/MTUxMDQwOTc4NS03/MDg4LmpwZWc.jpeg'),
    ('8c9b5b66-a60b-4168-b492-f2e281f6457c', 'Taylor Swift', 'https://i.discogs.com/uBmXY2YMkaSmZLJZ9VUAon4AaSL_z_yjL_vCfrnRCDo/rs:fit/g:sm/q:90/h:800/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTExMjQ2/NDUtMTcxMzYxODEz/Ny02MTkwLmpwZWc.jpeg'),
    ('cff0ae86-1259-484a-b21a-b87ecf218f05', 'Ранетки', 'https://i.discogs.com/Wsv6A1GwDD9GNm4TsTbGl0eaM5HfAz-lyS10qHxLUfc/rs:fit/g:sm/q:90/h:400/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEwNTYw/NzMtMTQzMTI5NjY0/NC00Nzk0LmpwZWc.jpeg'),
    ('7556728f-c18d-4ebe-8698-996d750f5369', 'Макс Корж', 'https://i.discogs.com/6Bz3ydLjLgbv-3dGTp6XJBrIkFLFZccSHCkGVWrAVJA/rs:fit/g:sm/q:90/h:399/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTIyODY5/MTUtMTU4NDg5MDEy/NS03ODAyLmpwZWc.jpeg'),
    ('02b2c217-1537-4967-b046-5cc9375798d7', 'Антонио Вивальди', 'https://i.discogs.com/8_DvXtrHVj24Jy2KFXIULYOUUKXFJE0lN_rBIwAObvI/rs:fit/g:sm/q:90/h:486/w:404/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEwODU2/Ni0xMDgzODMxMjMx/LmpwZw.jpeg'),
    ('7859f371-8a8d-4135-b267-746bbdf5c92a', 'Эрик Сати', 'https://i.discogs.com/XB4OJUsnfzGEdXbqEjRO3CN1efWHn96SEElvumKwbtk/rs:fit/g:sm/q:90/h:340/w:204/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTk1NTM5/LTExMjYzODEwNTIu/anBlZw.jpeg'),
    ('07745f6e-0b41-4f6f-afe3-30ad32e3ace5', 'WILLOW', 'https://i.discogs.com/vPU6egk-NBUtgp21zpBUmxwyUTqSBeg2cPESyRs1XZ0/rs:fit/g:sm/q:90/h:294/w:400/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTY0MTA4/LTE1OTA3NzExMTEt/NTgwMC5qcGVn.jpeg'),
    ('23ba4902-d4c6-4e59-8010-a8498cb00588', 'Daft Punk', 'https://i.discogs.com/sP_wDoC5MsG9lZUfb9thLbpmMmL__nuVnGMNpwgjirE/rs:fit/g:sm/q:90/h:438/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyODkt/MTYxNTQ4NDUyOS00/Mjg0LmpwZWc.jpeg'),
    ('7b78adf1-ea46-4b0e-9514-24336e9bbc63', 'Crystal Castles', 'https://i.discogs.com/rFfGpGyowu0ki4qnG8ZeW2lRnc_k0udcwGauCGMA6Fk/rs:fit/g:sm/q:90/h:397/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTY1OTY0/Ni0xNDY4MzUwMTE0/LTcyNjAuanBlZw.jpeg'),
    ('fff548a8-c624-49e3-8eb8-fff0daf95cee', 'Metallica', 'https://i.discogs.com/ypZclL3aRCjEtOU8NTTljmZA2yDB3zl9TiG5SrMUkFg/rs:fit/g:sm/q:90/h:435/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTE4ODM5/LTE1NTU4NTQxNjQt/MjM4Ny5qcGVn.jpeg'),
    ('5d9fecca-ff6a-4b42-8086-4c6d1bbc8fcf', 'Mastodon', 'https://i.discogs.com/N4BBdR1QbAp6mjZkNdgUHSljeM7Y54miwSmv0Hnlpn0/rs:fit/g:sm/q:90/h:807/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTI1MjE2/MS0xNjM1NDM1NTU0/LTc3MTQucG5n.jpeg')
;

INSERT INTO genres VALUES
    ('9d7cdbd1-60ec-43e6-9f4c-59fc0305d1ad', 'Поп', 'https://www.billboard.com/wp-content/uploads/2023/04/taylor-swift-atlanta-eras-tour-all-too-well-2023-billboard-1548.jpg?w=942&h=623&crop=1'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'Рок', 'https://blog.ticketmaster.com/wp-content/uploads/RedHotChiliPeppers_450631878-1024x640.jpg'),
    ('d13037b9-9ba7-435d-99ca-eb3a2a9525d1', 'Инди', 'https://www.gainesville.com/gcdn/authoring/2011/06/30/NTGS/ghows-LK-a099d0b8-14ba-4565-aefa-a4311de8cdb6-cc85016b.jpeg?width=660&height=495&fit=crop&format=pjpg&auto=webp'),
    ('bc7bfcfb-7c49-4182-802c-f11b94d8d358', 'Метал', 'https://flypaper.soundfly.com/wp-content/uploads/2016/10/metal-covers-header.png'),
    ('6e401ce8-31f5-405a-8994-a8970a4290b1', 'Электроника', 'https://i.discogs.com/sP_wDoC5MsG9lZUfb9thLbpmMmL__nuVnGMNpwgjirE/rs:fit/g:sm/q:90/h:438/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyODkt/MTYxNTQ4NDUyOS00/Mjg0LmpwZWc.jpeg'),
    ('6703b13a-e284-4068-8238-df497fb61796', 'Рэп и хип-хоп', 'https://img.redbull.com/images/c_crop,w_3200,h_1600,x_0,y_162,f_auto,q_auto/c_scale,w_1200/redbullcom/2019/12/17/ld52yhb5lp63snkkwtsf/tory-lanez-toronto'),
    ('5bec1307-897d-4bf3-9902-d799f77bdf37', 'Эстрада', 'https://avatars.dzeninfra.ru/get-zen_doc/3362051/pub_632c9303ea67882b4f462ba2_632c9475bf3e9757549c3a5d/scale_1200'),
    ('8737deec-47fc-4373-903f-9598e14f62e7', 'Классическая музыка', 'https://www.washingtonpost.com/resizer/fJ0ImqDpAlIw5kADKMwvOuzcnL0=/arc-anglerfish-washpost-prod-washpost/public/ODVSX5B5GII6VL7CBEHLG63AWE.jpg')
;

INSERT INTO user_purposes VALUES
    ('aa0afcc6-9259-4f43-a45a-7ed090dcd1f4', 'Обмен музыкой'),
    ('432bb7c1-a523-48c7-b0d0-27029323a20b', 'Собрать группу'),
    ('15b2af85-1ef3-4900-ac34-dbe2a7a6cdc2', 'Пойти на концерт'),
    ('0ef3de78-0c1b-43ee-af35-491b49bbb743', 'Создавать музыку'),
    ('445052c7-933d-4c66-8a14-96f1951a51a9', 'Обучение музыке')
;

INSERT INTO cities VALUES
    ('beb48d4f-7ec2-4184-a508-58684be36e22', 'Москва'),
    ('26f3e9bf-881b-497a-b479-eabe7905593c', 'Санкт-Петербург'),
    ('c32b1874-3c65-44fa-baf1-ac0adbfe82de', 'Новосибирск'),
    ('95d977e2-166a-49f3-a093-6233f4456c20', 'Екатеринбург'),
    ('56942ba3-faf2-41b7-a95e-2f80c59f9acc', 'Казань'),
    ('00fd5c98-23ff-43c5-b86f-b0c11b8308bd', 'Нижний Новгород'),
    ('c171a6f4-ae21-407b-bbc9-771f7f424f86', 'Челябинск'),
    ('f3cd77f9-9751-48d1-a0a9-379a53a1cb02', 'Омск'),
    ('6bdb4106-b065-45c0-aca0-0c3cd0009447', 'Самара'),
    ('2a5e6a06-de0b-48ec-af08-4df954b740d4', 'Ростов-на-Дону'),
    ('dbfbf69d-74d7-4270-9576-d046d94d2bc0', 'Уфа'),
    ('1bffec62-234b-42c1-b9ba-3e94a86bd6e7', 'Красноярск'),
    ('a7513ba7-9560-4cc0-b092-c465e84a92ea', 'Воронеж'),
    ('fcbe8a88-9469-4434-bc79-8aac2da58606', 'Пермь'),
    ('c07a42a9-281a-448d-accf-4f92a1d068f6', 'Волгоград'),
    ('27f2cdac-5b63-4ec8-a896-7c7eda203fab', 'Краснодар'),
    ('374743bc-8a0f-492d-923b-171ee59d30fb', 'Тюмень'),
    ('b0f39187-b196-42c3-a738-37621c8fdb00', 'Саратов'),
    ('b179e729-b5c1-43c3-a497-6127942c01c4', 'Тольятти'),
    ('86a5f3fa-25f6-41a5-82dd-701b92a151f0', 'Махачкала'),
    ('8fb385b5-f310-499c-8c40-69f598f48ee9', 'Ижевск'),
    ('e16ea372-1c2d-4745-bacd-91470fee4e4f', 'Барнаул'),
    ('fae16d91-6715-4364-baf5-42364b6b418d', 'Ульяновск'),
    ('9519e8f1-13e5-4dfe-b6b9-eeffbb418109', 'Иркутск'),
    ('0a011fc0-239d-4a73-81b6-502e5cc8e83c', 'Хабаровск'),
    ('42eaac2a-7c74-40fd-a7fa-577d3c5798c7', 'Ярославль'),
    ('89eae92e-9682-4c38-8941-62549c3f44c2', 'Владивосток'),
    ('4e5fbe39-dc42-4a5f-9ae8-cfd730eccc43', 'Томск'),
    ('29a8013d-0b7b-4590-824e-ff478e7ea532', 'Оренбург'),
    ('0ae9dd0f-af78-4109-abd3-8518867e178a', 'Кемерово'),
    ('11d7545d-db1f-4ab4-8326-71123740d5e8', 'Новокузнецк'),
    ('cb25ec6e-5be7-4d2d-bd79-59a779ea60b4', 'Рязань'),
    ('a131384f-6f52-465c-9b29-9df436bad6e5', 'Набережные Челны'),
    ('a96f9770-4091-4108-bc33-48edd217c0a5', 'Астрахань'),
    ('f662cff9-a9eb-4568-ab16-37cb9567df91', 'Пенза'),
    ('88a428c6-b661-4392-855a-33c3f5a05ef7', 'Липецк'),
    ('1d500e1b-415e-441a-a182-002df798a6ae', 'Киров'),
    ('6a043aef-704e-4e2e-90a7-adb0493b8bb3', 'Калининград'),
    ('7b37e8fd-38ed-4e8f-9472-70209dc58d53', 'Чебоксары'),
    ('9de8b80a-4d5c-4c5a-964f-1f067abfe544', 'Балашиха'),
    ('42680c81-31ca-4f20-92b6-c3641cbda48b', 'Тула'),
    ('c28ef0fb-b1ea-4598-8afc-078755cfbc33', 'Курск'),
    ('0eded5fc-08ee-44ac-81d2-3fd4a6e90d38', 'Ставрополь'),
    ('98f95680-c9ab-45b0-97c8-1d302a48db46', 'Улан-Удэ'),
    ('4c2c0f34-8017-4969-a0ef-0ca394205b83', 'Тверь'),
    ('97f79529-788d-4e69-9990-18252d3bd08b', 'Магнитогорск'),
    ('a5704104-bfc9-4dec-83a4-96333c9567a8', 'Сочи'),
    ('4f1037fc-17f0-4bcf-b727-e10e562e8c70', 'Иваново'),
    ('5bc0da91-466f-4df6-8c5b-217bfc22b4a7', 'Брянск'),
    ('c8d9e037-a9ef-40e3-8543-4136ed363cda', 'Сургут'),
    ('72f8ab27-13a3-4682-87bc-57a4f3dfa3a6', 'Нижний Тагил'),
    ('a4c9ff1a-c9ce-49de-a50e-5bd381895aa4', 'Владимир'),
    ('b9a299fd-05ff-44f8-8211-8308c9016f9e', 'Архангельск'),
    ('4d6accea-59db-4fe2-9dc2-396ca117ffc1', 'Чита'),
    ('042d49e5-c5cd-4416-9d9e-313308f3a32e', 'Белгород'),
    ('08f6d90b-94c1-419a-884d-710ce486158e', 'Калуга'),
    ('4ccf9d04-7c0f-4fd0-82b3-d890add8e14f', 'Волжский'),
    ('07ca445b-83e6-4b6b-a48f-634a1af82ad7', 'Смоленск'),
    ('65d6f88f-eaa7-4ffa-a8e1-044dc41f53c4', 'Саранск'),
    ('2b53b943-6232-4ab0-8a97-7d69511441be', 'Череповец'),
    ('07a69bce-d7b1-4743-bf9b-9e7774651755', 'Вологда'),
    ('7e2f6df5-fa59-4764-b7df-d4291b8a2bbb', 'Якутск'),
    ('1eae79ec-c88c-45d7-9c22-6cb0ae87457a', 'Курган'),
    ('fe42f919-7358-4dd3-bf3c-7a53e675c8aa', 'Владикавказ'),
    ('343e9a61-f279-4594-ae39-d776b4c75554', 'Орёл'),
    ('875da7a9-4281-4d33-9886-f89725561340', 'Подольск'),
    ('093fd810-a1a2-4099-9b92-d1ce7ac853f3', 'Грозный'),
    ('32c01356-453f-4d20-a9c2-52ceaaa5aaa9', 'Тамбов'),
    ('9c7331e4-5d38-4929-923a-37dad2e14136', 'Стерлитамак'),
    ('b09424ba-53eb-400f-aa98-6ac81fe35d8a', 'Нижневартовск'),
    ('d19ced61-3493-4ac2-b440-3c1823e12253', 'Петрозаводск'),
    ('a4087522-29d1-48ec-9e8d-925c5581d909', 'Кострома'),
    ('0d7ba5a0-479f-4bb2-8eb6-ec6daea6eab1', 'Новороссийск'),
    ('4d9b7f09-ea46-4f38-8e16-3133913a6a51', 'Мурманск'),
    ('64ca3dc1-8cba-4d28-8b44-526d52902180', 'Йошкар-Ола'),
    ('22df7613-0029-420d-b994-4efe8d4f9825', 'Нальчик'),
    ('2ba3928d-86cb-4d51-86d5-119af0d71f0c', 'Энгельс'),
    ('f0a02197-d017-4703-a2b1-2fd30c4f7de2', 'Химки'),
    ('e6d95565-21ad-41d4-8473-80b5fd7d7325', 'Таганрог'),
    ('99e6a186-405e-4b32-9d28-539199bc1391', 'Комсомольск-на-Амуре'),
    ('e4fcc3ec-c780-46df-9b12-40c5754b14ae', 'Сыктывкар'),
    ('2de1c9dc-bc9b-41fe-8ffa-42a820077cfc', 'Нижнекамск'),
    ('b4bdba03-0156-4376-a74f-669a1cdf9384', 'Шахты'),
    ('8720247f-b302-4594-8afc-de83099264d4', 'Братск'),
    ('9a8784d5-ab52-4661-a59b-355872de4e52', 'Дзержинск'),
    ('766be829-4c32-48cc-9b80-dec3a785f703', 'Ногинск'),
    ('9d67de40-d933-4487-b205-17f1c3c17904', 'Орск'),
    ('7576066d-099a-4594-b8a3-0e8f31805bea', 'Колпино'),
    ('6866a2e6-b11b-4b69-a2d2-71e53f6bd60d', 'Благовещенск'),
    ('44b25701-49ca-4819-85ad-60198af1c2f6', 'Старый Оскол'),
    ('123ff505-2694-4722-8db0-b747359b01f3', 'Ангарск'),
    ('1ee6f7e3-f0f5-4531-a757-c85bb6672f2e', 'Великий Новгород'),
    ('cd4d38ff-395f-48f6-bfdd-28b9e84216ac', 'Королёв'),
    ('54149dde-340a-437d-9452-6d087599ac74', 'Мытищи'),
    ('b36b39c0-3120-4bb0-a5a4-649d55be9071', 'Псков'),
    ('ee8f6e8d-09ee-4140-befc-189079010004', 'Люберцы'),
    ('d98a8c76-047f-4eb0-8b9f-01d4f613b7ff', 'Бийск'),
    ('1c5a40f6-fbe1-4d70-ae20-771b3774166b', 'Южно-Сахалинск'),
    ('949de237-138f-477f-93f1-5abd9c21c112', 'Армавир'),
    ('6b3bca63-7f27-41b7-9230-92dbab245fc6', 'Рыбинск'),
    ('cdec64fe-a52d-4062-b4d7-e4bfd25376b3', 'Прокопьевск'),
    ('127e16ca-81a9-485e-a019-0e3a27b1cab9', 'Балаково'),
    ('68f6eac5-723d-44b2-b1f9-c660f4751f48', 'Абакан'),
    ('6bc408aa-76bb-4e9f-a10c-8dcf5fabc413', 'Норильск'),
    ('ed68c4e9-819a-4ad7-82c9-a6d5e14931a5', 'Красногорск'),
    ('4dff1c83-7a1d-45d3-9161-49ca27e04e91', 'Сызрань'),
    ('a3d76a6a-f7b3-4705-93d8-b5d362550409', 'Волгодонск'),
    ('06ccce68-52f7-4e48-a09a-eacde38540aa', 'Уссурийск'),
    ('92c54059-9c30-42d5-964e-f3cfeee51397', 'Каменск-Уральский'),
    ('83578d14-2e73-435f-b83a-32f94776f6ec', 'Новочеркасск'),
    ('fb0d0620-6a0d-4f63-8558-6639a5047d67', 'Златоуст'),
    ('3b2b4fd4-489b-4ee9-a167-ffb2a4b93c13', 'Петропавловск-Камчатский'),
    ('08bb8b60-180d-4308-a231-604e597f1534', 'Электросталь'),
    ('33188973-1aa8-43a7-85f6-3a7c6bbeb37b', 'Северодвинск'),
    ('7a01dd39-7ced-44a6-8500-18f679bc97fe', 'Альметьевск'),
    ('5797c821-738a-4cad-bbb0-6547cacf10bf', 'Салават'),
    ('64dce5a5-896c-47ae-9f2b-cdf14927ef89', 'Миасс'),
    ('35b0f234-cdb8-4985-a2c7-af62edc15178', 'Орехово-Борисово Южное'),
    ('77e41b7e-e16c-431b-85ea-290293b46f9d', 'Копейск'),
    ('2a18c3cf-0784-444c-aa54-925a0b8807f8', 'Одинцово'),
    ('da94bc66-be64-4979-8451-cf7aa4dcab42', 'Пятигорск'),
    ('d0a89482-799d-47d7-ad81-fc645fe227ae', 'Коломна'),
    ('2441c313-45a4-40c1-9095-9e11c046a8d8', 'Находка'),
    ('1bac372a-e883-46f4-825b-4dec2c6918ca', 'Березники'),
    ('f7b57508-258f-4151-8dc9-1895331a442a', 'Хасавюрт'),
    ('69920407-a028-451f-a186-52259596721f', 'Рубцовск'),
    ('1c7307d1-408c-4458-ae97-b73619f9e5d4', 'Майкоп'),
    ('00e9e987-7af6-4dd6-8add-e384c915cbdd', 'Ковров'),
    ('a717bab8-3689-4127-b1ae-0731cf37b6b4', 'Кисловодск'),
    ('217a4ac2-3a4f-47c5-b320-d41a6e90796c', 'Нефтеюганск'),
    ('9c038c5a-5877-43e9-bcb9-507182674a3a', 'Домодедово'),
    ('2c44ce58-5714-4dff-bb5a-8b6b5da558d7', 'Нефтекамск'),
    ('75d89136-1ee9-4488-8036-079aa957978f', 'Батайск'),
    ('8cf0804d-760a-48d0-8718-63cf2de9cdb3', 'Новочебоксарск'),
    ('41b6468d-aa46-4793-81b6-353563cba743', 'Серпухов'),
    ('917901e1-bb59-4b05-8160-49c5328af013', 'Щелково'),
    ('f1244f82-14bd-4301-aa46-88d70d230f18', 'Новомосковск'),
    ('3c61dd63-64c2-4150-9e7d-c0dd14064070', 'Каспийск'),
    ('2ca6fec0-0533-475b-b0fd-3c22adbea5f1', 'Дербент'),
    ('d5e6c21f-0529-4e55-8af6-4c775c9cab13', 'Первоуральск'),
    ('ac78521b-dd40-49c5-b131-e4054fa3c7bf', 'Черкесск'),
    ('ef8cafff-fb76-4b98-8e15-414dcb377a3f', 'Орехово-Зуево'),
    ('59c9d31a-4901-4a44-812b-ba73b99912fa', 'Назрань'),
    ('07cb484c-37ac-4df8-8553-3d8b72cbde79', 'Невинномысск'),
    ('6885e0d9-d189-4917-a852-08986c513a7c', 'Раменское'),
    ('8a14dd4e-5628-46f0-aeec-fd037df82fba', 'Димитровград'),
    ('6be6396e-d427-47aa-9d47-f1b8502b0e98', 'Кызыл'),
    ('281200dc-4076-425b-a2d5-f9f3b7e0ac55', 'Обнинск'),
    ('1b2eeaa0-0907-4b04-b607-d9d205de13aa', 'Октябрьский'),
    ('b15b722e-411b-435b-a5d3-06fe0051cab1', 'Новый Уренгой'),
    ('401f3944-1696-4140-bf91-ac75475c319f', 'Ессентуки'),
    ('fc9d8212-449c-4673-890d-c8b2f83253a8', 'Камышин'),
    ('169c1b8c-7f07-4d59-9f4b-b3520e31fb47', 'Муром'),
    ('27c1500c-442c-40f5-b157-59df9a068290', 'Долгопрудный'),
    ('141eb1ac-9d12-4c53-9748-bccd12262502', 'Новошахтинск'),
    ('1beeb054-4d9b-4043-86de-effe696f32a6', 'Жуковский'),
    ('98e6364c-d10f-453a-a4df-0a3e8175f366', 'Северск'),
    ('54aa45d2-2c1f-4b3a-ab8c-6e5bde32af27', 'Реутов'),
    ('616582f4-a84f-44cf-88be-e4014e854a7c', 'Ноябрьск'),
    ('81db3fc5-e62d-4e0f-8b3d-df180a4be33c', 'Артём'),
    ('26c2e6fe-b419-449f-aaa5-9dc714de97e5', 'Ханты-Мансийск'),
    ('25d04f9f-eb8a-4795-b0e3-4a22e72b113d', 'Пушкино'),
    ('b693ea4c-3a0f-4b70-aa58-035bb9fecdb8', 'Ачинск'),
    ('056b4b73-23a3-44a1-9e1a-3ca3c541893d', 'Сергиев Посад'),
    ('fadef1da-c557-4f32-ba86-c55dcdbe08e9', 'Елец'),
    ('98c9d56e-0e25-4ce5-b1f8-372e615387f9', 'Арзамас'),
    ('cf03ff93-9eb9-40e7-9d78-236dc226a52b', 'Бердск'),
    ('aac89428-087a-4d54-b6ff-8a812915b8c7', 'Элиста'),
    ('786801bc-8ebd-4a61-a324-f167f686be9a', 'Новокуйбышевск'),
    ('22f689db-a7ad-4031-ab9d-f8ce0ae94f62', 'Железногорск'),
    ('5cd3a4a6-4e15-43fa-be3b-7382051dae3a', 'Зеленодольск'),
    ('91890df9-e72b-4f2a-8fb4-6036de0163da', 'Гатчина'),
    ('e5cf6958-a919-499d-a9bc-73410f558a30', 'Магадан'),
    ('bb9d9ee3-fc78-4bb5-9202-5fc0695558d6', 'Великие Луки'),
    ('6f36bf47-348a-4115-bb6e-a4309e3515ca', 'Лобня'),
    ('57a55ece-28ec-4e76-a840-404cf9685986', 'Бузулук'),
    ('ff976345-e936-4241-8839-d58f2c27b19b', 'Кинешма'),
    ('e7403b28-48f5-4083-bad0-0aef50d015cc', 'Кузнецк'),
    ('db63c179-f836-4df9-be48-8336ce0a6fc5', 'Юрга'),
    ('45e3086e-c1e1-4e38-b72b-6ac1dac6b9c6', 'Ивантеевка'),
    ('8ab9d26a-164f-4530-8ec8-d9cbe688171f', 'Черногорск'),
    ('4a7424a2-6d0f-44ff-ac00-53dae3f06405', 'Биробиджан'),
    ('cc6be26b-d847-4a4f-a32a-a54093c14511', 'Кирово-Чепецк'),
    ('7d1630cc-e27f-4e79-9adc-60f73227cc16', 'Георгиевск'),
    ('b4d416bc-fadc-4653-aead-ba9597c825ca', 'Ишим'),
    ('e7f43db5-5260-4901-adc2-3e6412852689', 'Буйнакск'),
    ('c418f77d-6e28-460e-b11a-11e6addef08b', 'Гуково'),
    ('379fec05-f148-409d-9c13-733374bae465', 'Горно-Алтайск'),
    ('20949a34-4f84-4124-b484-16f0fb6e5005', 'Фрязино'),
    ('5be395ed-be74-4f7d-b5e7-9acedb857632', 'Лыткарино'),
    ('170ba748-d474-4344-b311-e9eae860f6c7', 'Прохладный'),
    ('23febef9-8e16-4122-8c7e-92d241db642b', 'Шуя'),
    ('82205993-350a-47fa-800a-5bc49434bb47', 'Искитим'),
    ('04d244b2-9a8e-4072-b75e-a7a3af4a7df7', 'Климовск'),
    ('155e8acf-f576-48fb-8bdd-5d6e6c642012', 'Дзержинский'),
    ('42197f24-5a03-4695-a1bc-9cef77c6ccdd', 'Волжск'),
    ('9d3a4ec8-84d6-4365-96ef-3a01540d73b4', 'Салехард'),
    ('89762138-628e-404f-b0c9-57d437cea378', 'Московский'),
    ('1fc716bd-3076-4e2c-8a26-7ad2d866e055', 'Нововятск'),
    ('3190f357-ff69-436f-b8e3-88ff9c5d17b7', 'Можга'),
    ('74b1068b-e9e3-449f-9a7f-70e89c516af0', 'Кизляр'),
    ('24216c4f-27f8-4073-90ab-cfea2afb3307', 'Котельники'),
    ('60b67f92-a3a4-4f83-98d1-862afd8e8297', 'Канаш'),
    ('020472a7-afa5-4abe-a3df-cf5385f8d030', 'Краснознаменск'),
    ('f8706a50-9e26-4bb5-9647-4dddcdb38a77', 'Сосновоборск'),
    ('4845fbd3-97d4-4a21-8bfc-4ec9720359db', 'Моршанск'),
    ('ea22609d-21b7-4a93-ad9e-65d314825016', 'Переславль-Залесский'),
    ('a3c7af56-c9aa-4cbb-8b98-efd0e88d34dc', 'Мценск'),
    ('39ecc3b8-2348-4ef3-82ef-54df766049d4', 'Баксан'),
    ('0387f261-d800-4798-8992-ae987ba2677c', 'Протвино'),
    ('69b3ef4c-96e0-4e29-b257-e0cbf6650ffb', 'Касимов'),
    ('110dff21-f21a-4b87-bf51-f3e4012feefd', 'Кохма'),
    ('b926d689-b6df-4ebe-92a2-f8f5b1cd2994', 'Котовск'),
    ('1f1a08e6-94c8-4861-9e9e-8798ed22c166', 'Дагестанские Огни'),
    ('09d3086b-f13a-4aac-af2f-b3a9e2b58188', 'Шумерля'),
    ('c7bf0ec5-e180-4091-8617-3ada5d11ce2c', 'Удомля'),
    ('13daefc6-af89-445b-be86-887aa74c2cc7', 'Десногорск'),
    ('a805e251-d365-465d-99b4-6e4f6933e3b9', 'Лосино-Петровский'),
    ('3b285ca8-c837-4924-8f10-1e20320f1274', 'Нарьян-Мар'),
    ('1844059a-ef68-4446-a2e0-2291f6d1c2a1', 'Красково'),
    ('977c0c1f-a73b-471f-bd01-6ded99bf3f54', 'Карачаевск'),
    ('c40a0285-b50d-4ebc-9115-0fe8844ff33b', 'Козьмодемьянск'),
    ('11abed64-4a29-454e-acce-0b4e4ee23751', 'Кирсанов'),
    ('b7d3f42d-c2bb-4da2-8298-eb52ed06d3d2', 'Анадырь'),
    ('06c19b96-aa14-49bc-95df-b018ac579c50', 'Удельная'),
    ('66b65724-e416-44e8-aead-1e7f58c839f7', 'Кудрово'),
    ('985a3694-a34f-4ef4-8f43-d3224d89e4c1', 'Пионерский'),
    ('cd285240-964e-4b26-876a-aeb4c66706c1', 'Хасанья'),
    ('700bae63-8fb7-4db4-8207-7835560bbac5', 'Магас'),
    ('3fc1be75-b2ac-4e7d-bac4-544e58281d37', 'Кенже'),
    ('2f256ac6-58fe-4afe-afb7-b05b8d63c168', 'Молочное'),
    ('db4efa18-dffe-4c7e-bcfc-b5f3c07a8962', 'Солянка'),
    ('f2541128-bb99-47b9-8aea-96d132efd1bf', 'Тимофеевка'),
    ('3b562bf1-5a7d-4ba1-a87b-a4ec26b84785', 'Белая Речка'),
    ('df07c8f6-101d-406e-a2bc-3ad8469dc627', 'Дзержинское'),
    ('d56dac36-8195-4328-8f16-f8a9b9d433c1', 'Власиха')
;

INSERT INTO users VALUES
    (
        'faf0700d-87a9-498c-8e09-001e634c0516',
        'user@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'admin',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQSIxDkzQ6RLhabW04lvT2r_rqSyTB598tCdg&s',
        '',
        'beb48d4f-7ec2-4184-a508-58684be36e22'
    ),
    (
        'a317bcfb-faba-4052-b8f0-110196ac0577',
        'user1@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'nicky',
        'https://tty-art.com/file/2020/01/Generated-Faces-by-AI-Young-Men-V1-001.jpg',
        '',
        'beb48d4f-7ec2-4184-a508-58684be36e22'
    ),
    (
        'd47108dd-d830-48d5-b5cc-44bfa4f533b2',
        'user2@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'Testy',
        'https://static.vecteezy.com/system/resources/previews/036/442/721/non_2x/ai-generated-portrait-of-a-young-man-no-facial-expression-facing-the-camera-isolated-white-background-ai-generative-photo.jpg',
        '',
        'd56dac36-8195-4328-8f16-f8a9b9d433c1'
    ),
    (
        'de6c4f80-00e4-4f6c-bbbd-ae39ab5948df',
        'user3@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'Vector',
        'https://img.freepik.com/premium-photo/cute-hot-beauty-female-model-face-photo-ai-generated_980993-1536.jpg',
        '',
        'beb48d4f-7ec2-4184-a508-58684be36e22'
    ),
    (
        '148086e7-dbd9-4632-ac98-122b34fa6481',
        'user4@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'Artyo',
        'https://www.socialeurope.eu/wp-content/uploads/2019/11/Juliane-Bir.jpg',
        '',
        'beb48d4f-7ec2-4184-a508-58684be36e22'
    ),
    (
        '82a3ecd2-7d61-4cd5-90a4-1f863315085a',
        'user5@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'Jonnnn',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS1cz4TQW5KNc_H4DKBjyzFuqDBglS646EpOA&s',
        '',
        'beb48d4f-7ec2-4184-a508-58684be36e22'
    ),
    (
        '114e8ced-cbb7-4cb2-9b7b-3c03b175cfd2',
        'user6@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'Akak',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJUI4Au13gpNsq2oYnKAqMMkr1t1IucvcAGmgLGeZIJeWC6lKF4N1Ik-Qbyf4UfX16n-E&usqp=CAU',
        '',
        'beb48d4f-7ec2-4184-a508-58684be36e22'
    ),
    (
        'b2d8f823-a3f9-4688-91c3-9ee89dc80169',
        'user7@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'Iki',
        'https://images.generated.photos/ESVGljXXYFyUPgeMIfass2y1SjtNmTNZz95C4pUScoo/rs:fit:256:256/czM6Ly9pY29uczgu/Z3Bob3Rvcy1wcm9k/LnBob3Rvcy8wMjY5/OTQzLmpwZw.jpg',
        '',
        'beb48d4f-7ec2-4184-a508-58684be36e22'
    ),
    (
        '8746e4ff-1cce-427f-9c92-5e9757bdf2a8',
        'user8@gmail.com',
        '$argon2id$v=19$m=65536,t=50,p=8$atkjhzk92vN8hXbbCND9UA$IwmbQfBRSpQdWXUT0VoS3gF7raNiUholWjGmrXiTSXk',
        NOW(),
        NOW(),
        'Metzi',
        'https://images.generated.photos/dlIY7lCmpllkdbweLtkPpaLGHLlySuYxM_oWRkhIZr0/rs:fit:256:256/czM6Ly9pY29uczgu/Z3Bob3Rvcy1wcm9k/LnBob3Rvcy92M18w/NTgxNzk3LmpwZw.jpg',
        '',
        '11d7545d-db1f-4ab4-8326-71123740d5e8'
    )
;

INSERT INTO chats VALUES
    (
        'e9a1a537-6cab-4a28-8794-cb08317f9ce5',
        'faf0700d-87a9-498c-8e09-001e634c0516',
        'a317bcfb-faba-4052-b8f0-110196ac0577',
        NOW(),
        NOW(),
        'Есть 5 минут?',
        NOW()
    ),
    (
        'd5c343e9-621b-4a0f-a8e5-a391178a1f25',
        'faf0700d-87a9-498c-8e09-001e634c0516',
        'd47108dd-d830-48d5-b5cc-44bfa4f533b2',
        NOW(),
        NOW(),
        NULL,
        NULL
    ),
    (
        '78877c81-1447-4df8-aa06-70f135337f2c',
        'faf0700d-87a9-498c-8e09-001e634c0516',
        'de6c4f80-00e4-4f6c-bbbd-ae39ab5948df',
        NOW(),
        NOW(),
        NULL,
        NULL
    )
;

INSERT INTO messages VALUES
    (
        'a8d3d926-975b-4af3-ad46-cb57431808af',
        'e9a1a537-6cab-4a28-8794-cb08317f9ce5',
        'faf0700d-87a9-498c-8e09-001e634c0516',
        'Привет!',
        NOW()
    ),
    (
        '8affd759-4df7-4648-846d-bf48d67d08e8',
        'e9a1a537-6cab-4a28-8794-cb08317f9ce5',
        'a317bcfb-faba-4052-b8f0-110196ac0577',
        'привет',
        NOW()
    ),
    (
        'f4f81096-7daf-4409-af54-165f203b2e2b',
        'e9a1a537-6cab-4a28-8794-cb08317f9ce5',
        'faf0700d-87a9-498c-8e09-001e634c0516',
        'Вопрос',
        NOW()
    ),
    (
        '3fa8b30d-0f91-4f2a-80b2-10ca3601e2c7',
        'e9a1a537-6cab-4a28-8794-cb08317f9ce5',
        'faf0700d-87a9-498c-8e09-001e634c0516',
        'Есть 5 минут?',
        NOW()
    )
;

INSERT INTO favorite_artists_users VALUES
    ('f7436977-6b4e-4389-acc1-63b406a2442d', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('b640640a-040f-4134-a520-10f7b9cf0511', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('aac7e0b2-0444-4ee3-bf87-9c82865ade70', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('7556728f-c18d-4ebe-8698-996d750f5369', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('f7436977-6b4e-4389-acc1-63b406a2442d', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('b640640a-040f-4134-a520-10f7b9cf0511', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('aac7e0b2-0444-4ee3-bf87-9c82865ade70', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('7556728f-c18d-4ebe-8698-996d750f5369', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('7556728f-c18d-4ebe-8698-996d750f5369', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2'),
    ('b640640a-040f-4134-a520-10f7b9cf0511', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2'),
    ('aac7e0b2-0444-4ee3-bf87-9c82865ade70', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2'),
    ('02b2c217-1537-4967-b046-5cc9375798d7', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2')
;

INSERT INTO user_purposes_users VALUES
    ('aa0afcc6-9259-4f43-a45a-7ed090dcd1f4', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('0ef3de78-0c1b-43ee-af35-491b49bbb743', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('aa0afcc6-9259-4f43-a45a-7ed090dcd1f4', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('0ef3de78-0c1b-43ee-af35-491b49bbb743', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('aa0afcc6-9259-4f43-a45a-7ed090dcd1f4', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2'),
    ('0ef3de78-0c1b-43ee-af35-491b49bbb743', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2')
;

INSERT INTO favorite_genres_users VALUES
    ('9d7cdbd1-60ec-43e6-9f4c-59fc0305d1ad', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'faf0700d-87a9-498c-8e09-001e634c0516'),
    ('9d7cdbd1-60ec-43e6-9f4c-59fc0305d1ad', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'a317bcfb-faba-4052-b8f0-110196ac0577'),
    ('6e401ce8-31f5-405a-8994-a8970a4290b1', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'd47108dd-d830-48d5-b5cc-44bfa4f533b2')
;

INSERT INTO genres_artists VALUES
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'f7436977-6b4e-4389-acc1-63b406a2442d'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'b640640a-040f-4134-a520-10f7b9cf0511'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'aac7e0b2-0444-4ee3-bf87-9c82865ade70'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', '32f14d9d-72b2-4a5a-b14b-31396102270e'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', '9e22ca12-c794-4f33-ad0d-db9214ed519f'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', '771849d3-741c-447c-8b4d-422cfc0e7d28'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'f493a268-bec1-4ef1-ba93-e75ae8f692f9'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'a9f23cec-6f44-4710-aa64-1a6eb789a395'),
    ('9d7cdbd1-60ec-43e6-9f4c-59fc0305d1ad', '4769881f-76d8-42e6-9972-6a1c4ead555a'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'b0a6381a-219a-40cd-9bac-c407ed87cdc3'),
    ('9d7cdbd1-60ec-43e6-9f4c-59fc0305d1ad', '8c9b5b66-a60b-4168-b492-f2e281f6457c'),
    ('20349b94-136f-421c-b7ee-ef50ade9cc91', 'cff0ae86-1259-484a-b21a-b87ecf218f05'),
    ('6703b13a-e284-4068-8238-df497fb61796', '7556728f-c18d-4ebe-8698-996d750f5369'),
    ('8737deec-47fc-4373-903f-9598e14f62e7', '02b2c217-1537-4967-b046-5cc9375798d7'),
    ('8737deec-47fc-4373-903f-9598e14f62e7', '7859f371-8a8d-4135-b267-746bbdf5c92a'),
    ('6703b13a-e284-4068-8238-df497fb61796', '07745f6e-0b41-4f6f-afe3-30ad32e3ace5'),
    ('6e401ce8-31f5-405a-8994-a8970a4290b1', '23ba4902-d4c6-4e59-8010-a8498cb00588'),
    ('6e401ce8-31f5-405a-8994-a8970a4290b1', '7b78adf1-ea46-4b0e-9514-24336e9bbc63'),
    ('bc7bfcfb-7c49-4182-802c-f11b94d8d358', 'fff548a8-c624-49e3-8eb8-fff0daf95cee'),
    ('bc7bfcfb-7c49-4182-802c-f11b94d8d358', '5d9fecca-ff6a-4b42-8086-4c6d1bbc8fcf')
;