CREATE TABLE company_details_qr (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    personal_acc VARCHAR(20) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    bic VARCHAR(9) NOT NULL,
    corres_acc VARCHAR(20) NOT NULL,
    payee_inn VARCHAR(12) NOT NULL,
    kpp VARCHAR(9) NOT NULL,
    bank VARCHAR(3) NOT NULL UNIQUE,
    create_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    update_date TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO company_details_qr (id, name, personal_acc, bank_name, bic, corres_acc, payee_inn, kpp, bank)
VALUES (
    '5fe4bb4d-8d97-4772-9cb7-48d7ac1a3a25',
    'общество с ограниченной ответственностью «страховая компания СОГАЗ-ЖИЗНЬ» (ООО «СК СОГАЗ жизнь»)',
    '40701810500000331710',
    'Банк ГПБ (АО) г. Москва',
    '044525823',
    '30101810200000000823',
    '7729503816',
    '770801001',
    'GPB'
);

INSERT INTO company_details_qr (id, name, personal_acc, bank_name, bic, corres_acc, payee_inn, kpp, bank)
VALUES (
    '50dc96bc-3341-4d82-9d51-1345b0ea6c98',
    'ООО «СОГАЗ ЖИЗНЬ»',
    '40701810003800000158',
    'Банк ВТБ (ПАО) г.Москва',
    '044525187',
    '30101810700000000187',
    '7729503816',
    '770801001',
    'VTB'
);