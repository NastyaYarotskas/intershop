-- DROP TABLE public.items;

CREATE TABLE IF NOT EXISTS public.items (
	price int4 NOT NULL,
	id uuid NOT NULL DEFAULT gen_random_uuid(),
	description varchar(255) NULL,
	title varchar(255) NULL,
	img text NULL,
	CONSTRAINT items_pkey PRIMARY KEY (id)
);

-- DROP TABLE public.orders;

CREATE TABLE IF NOT EXISTS public.orders (
	is_new bool NOT NULL,
	id uuid NOT NULL DEFAULT gen_random_uuid(),
	CONSTRAINT orders_pkey PRIMARY KEY (id)
);

-- DROP TABLE public.orders_items;

CREATE TABLE IF NOT EXISTS public.orders_items (
	count int4 NOT NULL,
	item_id uuid NOT NULL,
	order_id uuid NOT NULL,
	CONSTRAINT orders_items_pkey PRIMARY KEY (item_id, order_id)
);

-- DROP TABLE public.users;

CREATE TABLE IF NOT EXISTS public.users (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
	active boolean NOT NULL DEFAULT TRUE,
	roles varchar(255) NOT NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE public.orders_items DROP CONSTRAINT IF EXISTS fkc03a4t5e1xbn9g2qp2k2umr64;
ALTER TABLE public.orders_items DROP CONSTRAINT IF EXISTS fkij1wwgx6o198ubsx1oulpopem;

ALTER TABLE public.orders_items ADD CONSTRAINT fkc03a4t5e1xbn9g2qp2k2umr64 FOREIGN KEY (item_id) REFERENCES public.items(id) ON DELETE CASCADE;
ALTER TABLE public.orders_items ADD CONSTRAINT fkij1wwgx6o198ubsx1oulpopem FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;