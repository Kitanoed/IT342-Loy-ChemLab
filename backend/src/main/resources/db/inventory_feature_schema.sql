CREATE TABLE IF NOT EXISTS inventory_items (
	id BIGSERIAL PRIMARY KEY,
	item_code VARCHAR(50) UNIQUE NOT NULL,
	item_name VARCHAR(255) NOT NULL,
	item_type VARCHAR(20) NOT NULL,
	category VARCHAR(100),
	cas_number VARCHAR(32),
	quantity NUMERIC(14,3) NOT NULL DEFAULT 0,
	unit VARCHAR(20) NOT NULL,
	min_threshold NUMERIC(14,3) NOT NULL DEFAULT 0,
	status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
	lot_number VARCHAR(100),
	description TEXT,
	safety_notes TEXT,
	pubchem_cid INTEGER,
	molecular_formula TEXT,
	molecular_weight TEXT,
	iupac_name TEXT,
	expiry_date DATE,
	received_date DATE,
	lab_id BIGINT NOT NULL,
	storage_location VARCHAR(120),
	supplier_name VARCHAR(255),
	backorder_allowed BOOLEAN NOT NULL DEFAULT FALSE,
	version BIGINT NOT NULL DEFAULT 0,
	archived BOOLEAN NOT NULL DEFAULT FALSE,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP NOT NULL,
	created_by BIGINT NOT NULL,
	updated_by BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory_bulk_jobs (
	id UUID PRIMARY KEY,
	operation_type VARCHAR(30) NOT NULL,
	status VARCHAR(30) NOT NULL,
	lab_id BIGINT NOT NULL,
	created_by BIGINT NOT NULL,
	approved_by BIGINT,
	reason_code VARCHAR(50) NOT NULL,
	note TEXT,
	source_type VARCHAR(20) NOT NULL,
	payload_json TEXT NOT NULL,
	idempotency_key VARCHAR(80) UNIQUE,
	total_rows INT NOT NULL DEFAULT 0,
	valid_rows INT NOT NULL DEFAULT 0,
	success_rows INT NOT NULL DEFAULT 0,
	failed_rows INT NOT NULL DEFAULT 0,
	started_at TIMESTAMP,
	completed_at TIMESTAMP,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory_bulk_job_rows (
	id BIGSERIAL PRIMARY KEY,
	job_id UUID NOT NULL REFERENCES inventory_bulk_jobs(id) ON DELETE CASCADE,
	row_no INT NOT NULL,
	item_id BIGINT,
	row_status VARCHAR(20) NOT NULL,
	requested_change TEXT NOT NULL,
	before_snapshot TEXT,
	after_snapshot TEXT,
	error_code VARCHAR(80),
	error_message TEXT,
	warning_messages TEXT,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP NOT NULL,
	UNIQUE(job_id, row_no)
);

CREATE TABLE IF NOT EXISTS inventory_audit_logs (
	id BIGSERIAL PRIMARY KEY,
	entity_type VARCHAR(30) NOT NULL,
	entity_id VARCHAR(64) NOT NULL,
	action VARCHAR(50) NOT NULL,
	actor_user_id BIGINT NOT NULL,
	actor_role VARCHAR(20) NOT NULL,
	lab_id BIGINT,
	reason_code VARCHAR(50),
	correlation_id UUID,
	ip_address VARCHAR(64),
	user_agent VARCHAR(255),
	before_data TEXT,
	after_data TEXT,
	metadata TEXT,
	created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_inventory_items_lab ON inventory_items(lab_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_type_status ON inventory_items(item_type, status);
CREATE INDEX IF NOT EXISTS idx_inventory_items_pubchem_cid ON inventory_items(pubchem_cid);
CREATE INDEX IF NOT EXISTS idx_inventory_bulk_jobs_status_lab ON inventory_bulk_jobs(status, lab_id);
CREATE INDEX IF NOT EXISTS idx_inventory_bulk_job_rows_job_status ON inventory_bulk_job_rows(job_id, row_status);
CREATE INDEX IF NOT EXISTS idx_inventory_audit_entity ON inventory_audit_logs(entity_type, entity_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_inventory_audit_correlation ON inventory_audit_logs(correlation_id);

ALTER TABLE IF EXISTS users
	ADD COLUMN IF NOT EXISTS lab_id BIGINT;
