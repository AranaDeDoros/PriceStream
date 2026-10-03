
-- This migration is to fix the discrepancies between the schema manually created
-- and the one created by my agent for the B1 migration

-- Remove orphaned tracking URLs.
DELETE FROM public.tracking_urls tu
WHERE NOT EXISTS (
    SELECT 1
    FROM public.tracking_requests tr
    WHERE tr.id = tu.tracking_request_id
);

-- tracking_requests.id must be unique because it is the parent key.
ALTER TABLE public.tracking_requests
    ADD CONSTRAINT tracking_requests_pkey PRIMARY KEY (id);

-- Convert tracking_urls into the dependent 1:1 table.
ALTER TABLE public.tracking_urls
DROP COLUMN id;

ALTER TABLE public.tracking_urls
    ALTER COLUMN tracking_request_id SET NOT NULL;

ALTER TABLE public.tracking_urls
    ADD CONSTRAINT tracking_urls_pkey
        PRIMARY KEY (tracking_request_id);

-- Enforce the relationship.
ALTER TABLE public.tracking_urls
    ADD CONSTRAINT tracking_urls_tracking_request_id_fkey
        FOREIGN KEY (tracking_request_id)
            REFERENCES public.tracking_requests(id)
            ON DELETE CASCADE;