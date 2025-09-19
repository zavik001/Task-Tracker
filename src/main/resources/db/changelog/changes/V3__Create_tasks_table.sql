CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    title VARCHAR(500) NOT NULL,
    deadline TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED')),
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reminder_sent BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_tasks_user_status_deadline ON tasks (user_id, status, deadline);
CREATE INDEX idx_tasks_user_created_at ON tasks (user_id, created_at);
CREATE INDEX idx_tasks_status_deadline ON tasks (status, deadline);