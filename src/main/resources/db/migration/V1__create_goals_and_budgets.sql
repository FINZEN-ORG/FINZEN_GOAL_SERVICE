CREATE TABLE IF NOT EXISTS goals (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     name VARCHAR(255) NOT NULL,
    description TEXT,
    target_amount NUMERIC(18,2) NOT NULL,
    saved_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    due_date DATE,
    category VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
    );


CREATE TABLE IF NOT EXISTS budgets (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       goal_id BIGINT NULL,
                                       category VARCHAR(100) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    initial_amount NUMERIC(18,2) NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_goal FOREIGN KEY(goal_id) REFERENCES goals(id) ON DELETE SET NULL
    );