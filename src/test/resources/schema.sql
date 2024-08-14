create table if not exists customers (
  id BigInt PRIMARY KEY,
  name varchar(255) not null
);

INSERT INTO customers(id, name)
VALUES(1, 'Alex'),
(2, 'Kirsten');
