-- Preserve the strongest existing role when migrating users with multiple roles.
ALTER TABLE users ADD COLUMN role_id UUID;

UPDATE users u
SET role_id = COALESCE(
    (SELECT r.id
     FROM user_roles ur
     JOIN roles r ON r.id = ur.role_id
     WHERE ur.user_id = u.id
     ORDER BY CASE r.name WHEN 'ADMIN' THEN 0 ELSE 1 END, r.name
     LIMIT 1),
    (SELECT id FROM roles WHERE name = 'USER')
);

UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
WHERE LOWER(username) = 'plozdev';

ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_role_id_fkey
    FOREIGN KEY (role_id) REFERENCES roles(id);
CREATE INDEX idx_users_role_id ON users(role_id);

DROP TABLE user_roles;
