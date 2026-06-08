ALTER TABLE tb_usuario DROP CONSTRAINT IF EXISTS uk_usuario_email;
ALTER TABLE tb_usuario DROP CONSTRAINT IF EXISTS uk_usuario_cpf;

ALTER TABLE tb_usuario RENAME COLUMN cpf TO cpf_encrypted;
ALTER TABLE tb_usuario RENAME COLUMN email TO email_encrypted;
ALTER TABLE tb_usuario RENAME COLUMN telefone TO telefone_encrypted;

ALTER TABLE tb_usuario ALTER COLUMN cpf_encrypted TYPE VARCHAR(512);
ALTER TABLE tb_usuario ALTER COLUMN email_encrypted TYPE VARCHAR(512);
ALTER TABLE tb_usuario ALTER COLUMN telefone_encrypted TYPE VARCHAR(512);

ALTER TABLE tb_usuario ADD COLUMN IF NOT EXISTS cpf_hash VARCHAR(64);
ALTER TABLE tb_usuario ADD COLUMN IF NOT EXISTS email_hash VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_email_hash
    ON tb_usuario (email_hash) WHERE email_hash IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_cpf_hash
    ON tb_usuario (cpf_hash) WHERE cpf_hash IS NOT NULL;

ALTER TABLE tb_endereco RENAME COLUMN rua TO rua_encrypted;
ALTER TABLE tb_endereco RENAME COLUMN numero TO numero_encrypted;
ALTER TABLE tb_endereco RENAME COLUMN complemento TO complemento_encrypted;
ALTER TABLE tb_endereco RENAME COLUMN bairro TO bairro_encrypted;
ALTER TABLE tb_endereco RENAME COLUMN cidade TO cidade_encrypted;
ALTER TABLE tb_endereco RENAME COLUMN uf TO uf_encrypted;
ALTER TABLE tb_endereco RENAME COLUMN cep TO cep_encrypted;

ALTER TABLE tb_endereco ALTER COLUMN rua_encrypted TYPE VARCHAR(512);
ALTER TABLE tb_endereco ALTER COLUMN numero_encrypted TYPE VARCHAR(512);
ALTER TABLE tb_endereco ALTER COLUMN complemento_encrypted TYPE VARCHAR(512);
ALTER TABLE tb_endereco ALTER COLUMN bairro_encrypted TYPE VARCHAR(512);
ALTER TABLE tb_endereco ALTER COLUMN cidade_encrypted TYPE VARCHAR(512);
ALTER TABLE tb_endereco ALTER COLUMN uf_encrypted TYPE VARCHAR(16);
ALTER TABLE tb_endereco ALTER COLUMN cep_encrypted TYPE VARCHAR(512);
