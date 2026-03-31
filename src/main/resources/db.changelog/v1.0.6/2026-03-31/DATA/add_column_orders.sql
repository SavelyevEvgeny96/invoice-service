ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS url_pay_page_short VARCHAR(255);